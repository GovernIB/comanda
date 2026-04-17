package es.caib.comanda.ms.log.helper;

import es.caib.comanda.ms.exception.ComandaApiException;
import es.caib.comanda.ms.log.model.FitxerContingut;
import es.caib.comanda.ms.log.model.FitxerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogHelper {

    private static final Logger LOG = LoggerFactory.getLogger(LogHelper.class);
    private static final Long MAX_N_LINIES = Long.valueOf(10000L);
    private static final Long MIN_N_LINIES = Long.valueOf(20L);
    private static final Map<String, String> MIME_TYPES;
    private static final Set<String> COMPRESSED_MIME_TYPES;
    private static String appNom = "";

    static {
        Map<String, String> mimeTypes = new HashMap<String, String>();
        mimeTypes.put("log", "text/plain");
        mimeTypes.put("0", "text/plain");
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("htm", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("xml", "application/xml");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("svg", "image/svg+xml");
        mimeTypes.put("zip", "application/zip");
        mimeTypes.put("rar", "application/vnd.rar");
        mimeTypes.put("7z", "application/x-7z-compressed");
        mimeTypes.put("gz", "application/gzip");
        mimeTypes.put("gzip", "application/gzip");
        mimeTypes.put("tar", "application/x-tar");
        mimeTypes.put("tgz", "application/gzip");
        mimeTypes.put("bz2", "application/x-bzip2");
        MIME_TYPES = Collections.unmodifiableMap(mimeTypes);

        Set<String> compressedTypes = new HashSet<String>();
        compressedTypes.add("application/zip");
        compressedTypes.add("application/x-zip-compressed");
        compressedTypes.add("application/x-7z-compressed");
        compressedTypes.add("application/x-rar-compressed");
        compressedTypes.add("application/vnd.rar");
        compressedTypes.add("application/gzip");
        compressedTypes.add("application/x-gzip");
        compressedTypes.add("application/x-tar");
        COMPRESSED_MIME_TYPES = Collections.unmodifiableSet(compressedTypes);
    }

    public static void setAppNom(String appNom) {
        LogHelper.appNom = appNom != null ? appNom : "";
    }

    public static List<FitxerInfo> llistarFitxers(String directoriPath, String appNom) {
        Path logDirPath = resolveLogDirectoryPath(directoriPath);
        setAppNom(appNom);
        List<FitxerInfo> fitxers = new ArrayList<FitxerInfo>();
        DirectoryStream<Path> paths = null;
        try {
            paths = Files.newDirectoryStream(logDirPath);
            for (Path path : paths) {
                if (Files.isRegularFile(path)) {
                    FitxerInfo fitxer = getFitxerInfo(path);
                    if (fitxer != null) {
                        fitxers.add(fitxer);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Error generant la info dels fitxers pel directori " + directoriPath, ex);
            throw new ComandaApiException("Error generant la informació dels fitxers pel directori " + directoriPath, ex);
        } finally {
            if (paths != null) {
                try {
                    paths.close();
                } catch (IOException ignored) {
                }
            }
        }
        return fitxers;
    }

    public static FitxerContingut getFitxerByNom(String directoriPath, String nom) {
        Path filePath = getFilePath(directoriPath, nom);
        try {
            FitxerInfo fitxer = getFitxerInfo(filePath);
            byte[] contingut = getFitxerContingut(filePath, fitxer);
            return new FitxerContingut()
                    .setContingut(contingut)
                    .setMimeType(fitxer.getMimeType())
                    .setNom(fitxer.getNom())
                    .setDataCreacio(fitxer.getDataCreacio())
                    .setDataModificacio(fitxer.getDataModificacio())
                    .setMida(Long.valueOf(contingut.length));
        } catch (Exception ex) {
            LOG.error("Error reading file content for " + nom, ex);
            throw new ComandaApiException("Error llegint el contingut del fitxer " + nom + ". Error: " + ex.getMessage(), ex);
        }
    }

    public static LogFileStream getFileStreamByNom(String directoriPath, String nom) {
        Path filePath = getFilePath(directoriPath, nom);
        try {
            String mime = getMimeTypeByExtension(nom);
            if (!isCompressedFile(mime)) {
                String zipNom = changeExtensionToZip(nom);
                Path tempFile = Files.createTempFile("log-", "-" + zipNom);
                ZipOutputStream zos = null;
                try {
                    zos = new ZipOutputStream(Files.newOutputStream(tempFile));
                    zos.putNextEntry(new ZipEntry(nom));
                    Files.copy(filePath, zos);
                    zos.closeEntry();
                } finally {
                    if (zos != null) {
                        zos.close();
                    }
                }
                return new LogFileStream(Files.newInputStream(tempFile), zipNom, Files.size(tempFile), "application/zip", tempFile);
            }
            return new LogFileStream(Files.newInputStream(filePath), nom, Files.size(filePath), mime);
        } catch (IOException e) {
            throw new ComandaApiException("Error llegint fitxer " + nom, e);
        }
    }

    public static List<String> readLastNLines(String directoriPath, String nomFitxer, Long nLinies) {
        Path filePath = getFilePath(directoriPath, nomFitxer);
        LinkedList<String> lines = new LinkedList<String>();
        Long lineCount = getValidLineCount(nLinies);
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath.toFile(), "r");
            long pointer = file.length() - 1;
            StringBuilder currentLine = new StringBuilder();
            while (pointer >= 0 && lines.size() < lineCount.longValue()) {
                file.seek(pointer);
                int readByte = file.read();
                if (readByte == '\n') {
                    if (currentLine.length() > 0) {
                        lines.addFirst(currentLine.reverse().toString());
                        currentLine.setLength(0);
                    }
                } else if (readByte != '\r') {
                    currentLine.append((char) readByte);
                }
                pointer--;
            }
            if (currentLine.length() > 0) {
                lines.addFirst(currentLine.reverse().toString());
            }
            return lines;
        } catch (Exception ex) {
            LOG.error("[LogHelper.readLastNLines] Error no controlat", ex);
            throw new ComandaApiException("Error llegint últimes línia del fitxer de log", ex);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static Path getFilePath(String directoriPath, String nom) {
        String lowerNom = nom != null ? nom.toLowerCase() : "";
        String lowerAppNom = appNom != null ? appNom.toLowerCase() : "";
        if (!(lowerNom.contains(".log") || (lowerAppNom.length() > 0 && lowerNom.contains(lowerAppNom)))) {
            throw new ComandaApiException("El fitxer ha de contenir o bé l'extensió .log, o bé el nom de l'aplicació (" + nom + ")");
        }
        Path logDirPath = resolveLogDirectoryPath(directoriPath);
        Path filePath = logDirPath.resolve(nom);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ComandaApiException("El fitxer de log no existeix: " + nom);
        }
        return filePath;
    }

    private static Long getValidLineCount(Long nLinies) {
        if (nLinies == null) {
            return MIN_N_LINIES;
        }
        if (nLinies.longValue() > MAX_N_LINIES.longValue()) {
            return MAX_N_LINIES;
        }
        if (nLinies.longValue() < MIN_N_LINIES.longValue()) {
            return MIN_N_LINIES;
        }
        return nLinies;
    }

    public static String getDirectoryLogsFromJbossServerProperties() {
        String jbossLogDir = System.getProperty("jboss.server.log.dir");
        if (jbossLogDir != null && jbossLogDir.length() > 0) {
            return jbossLogDir;
        }
        String jbossBaseDir = System.getProperty("jboss.server.base.dir");
        if (jbossBaseDir != null && jbossBaseDir.length() > 0) {
            return jbossBaseDir + File.separator + "log";
        }
        return null;
    }

    protected static Path resolveLogDirectoryPath(String directoriPath) {
        if (directoriPath == null || directoriPath.length() == 0) {
            directoriPath = getDirectoryLogsFromJbossServerProperties();
            if (directoriPath == null) {
                throw new ComandaApiException("No s'ha pogut determinar el directori de logs");
            }
        }
        Path filesPath = Paths.get(directoriPath);
        if (!Files.exists(filesPath) || !Files.isDirectory(filesPath)) {
            throw new ComandaApiException("El directori de logs no existeix: " + directoriPath);
        }
        return filesPath;
    }

    protected static FitxerInfo getFitxerInfo(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            String fileName = path.getFileName().toString();
            return new FitxerInfo()
                    .setNom(fileName)
                    .setMida(Long.valueOf(attrs.size()))
                    .setMimeType(getMimeTypeByExtension(fileName))
                    .setDataCreacio(new Date(attrs.creationTime().toMillis()))
                    .setDataModificacio(new Date(attrs.lastModifiedTime().toMillis()));
        } catch (Exception ex) {
            LOG.warn("No s'ha pogut obtenir la informació del fitxer {}", path, ex);
            return null;
        }
    }

    protected static byte[] getFitxerContingut(Path filePath, FitxerInfo fitxer) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream baos = null;
        try {
            in = Files.newInputStream(filePath);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                baos.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    protected static String getMimeTypeByExtension(String nom) {
        if (nom == null) {
            return "application/octet-stream";
        }
        int idx = nom.lastIndexOf('.');
        if (idx < 0 || idx == nom.length() - 1) {
            return "application/octet-stream";
        }
        String extension = nom.substring(idx + 1).toLowerCase();
        String mime = MIME_TYPES.get(extension);
        return mime != null ? mime : "application/octet-stream";
    }

    protected static boolean isCompressedFile(String mime) {
        return mime != null && COMPRESSED_MIME_TYPES.contains(mime);
    }

    protected static String changeExtensionToZip(String nom) {
        int idx = nom.lastIndexOf('.');
        if (idx < 0) {
            return nom + ".zip";
        }
        return nom.substring(0, idx) + ".zip";
    }
}
