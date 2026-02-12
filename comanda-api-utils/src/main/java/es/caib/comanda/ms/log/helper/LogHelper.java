package es.caib.comanda.ms.log.helper;

import es.caib.comanda.model.server.monitoring.FitxerContingut;
import es.caib.comanda.model.server.monitoring.FitxerInfo;
import es.caib.comanda.ms.exception.ComandaApiException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class LogHelper {

    private static final Long maxNLinies = 10000L;
    private static final Long minNLinies = 20L;

    @Setter
    private static String appNom = null;

    public static List<FitxerInfo> llistarFitxers(String directoriPath, String appNom) {

        var logDirPath = resolveLogDirectoryPath(directoriPath);
        LogHelper.appNom = appNom;

        List<FitxerInfo> fitxers = new ArrayList<>();
        try (Stream<Path> paths = Files.list(logDirPath)) {
            paths.filter(Files::isRegularFile).forEach(filePath -> {
                var fitxer = getFitxerInfo(filePath);
                if (fitxer != null) {
                    fitxers.add(fitxer);
                }
            });
        } catch (Exception ex) {
            log.error("Error generant la info dels fitxers pel directori " + directoriPath, ex);
            throw new ComandaApiException("Error generant la informació dels fitxers pel directori " + directoriPath);
        }
        return fitxers;
    }

    public static FitxerContingut getFitxerByNom(String directoriPath, String nom) {

        var logDirPath = resolveLogDirectoryPath(directoriPath);

        var filePath = logDirPath.resolve(nom);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ComandaApiException("El fitxer de log no existeix: " + nom);
        }

        try {
            var fitxer = getFitxerInfo(filePath);
            var contingut = getFitxerContingut(filePath, fitxer);
            return new FitxerContingut()
                    .contingut(contingut)
                    .mimeType(fitxer.getMimeType())
                    .nom(fitxer.getNom())
                    .dataCreacio(fitxer.getDataCreacio())
                    .dataModificacio(fitxer.getDataModificacio())
                    .mida((long) contingut.length);
        } catch (Exception ex) {
            log.error("Error reading file content for " + nom, ex);
            throw new ComandaApiException("Error llegint el contingut del fitxer " + nom + ". Error: " + ex.getMessage(), ex);
        }
    }

    public static LogFileStream getFileStreamByNom(String directoriPath, String nom) {

        var logDirPath = resolveLogDirectoryPath(directoriPath);

        var filePath = logDirPath.resolve(nom);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ComandaApiException("El fitxer de log no existeix: " + nom);
        }

        try {
            var mime = getMimeTypeByExtension(nom);
            if (mime == null || !isCompressedFile(mime)) {
                // Si no està comprimit, el comprimim a un fitxer temporal
                String zipNom = changeExtensionToZip(nom);
                Path tempFile = Files.createTempFile("log-", "-" + zipNom);

                try (var out = Files.newOutputStream(tempFile);
                     var zos = new ZipOutputStream(out)) {
                    var zipEntry = new ZipEntry(nom);
                    zos.putNextEntry(zipEntry);
                    Files.copy(filePath, zos);
                    zos.closeEntry();
                }

                return new LogFileStream(
                        Files.newInputStream(tempFile),
                        zipNom,
                        Files.size(tempFile),
                        "application/zip",
                        tempFile
                );
            }

            InputStream in = Files.newInputStream(filePath);
            return new LogFileStream(
                    in,
                    nom,
                    Files.size(filePath),
                    mime
            );

        } catch (IOException e) {
            throw new RuntimeException("Error llegint fitxer", e);
        }
    }

    public static List<String> readLastNLines(String directoriPath, String nomFitxer, Long nLinies) {

        var logDirPath = resolveLogDirectoryPath(directoriPath);

        var filePath = logDirPath.resolve(nomFitxer);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ComandaApiException("El fitxer de log no existeix: " + nomFitxer);
        }

        LinkedList<String> lines = new LinkedList<>();
        nLinies = getValidLineCount(nLinies);

        try (var file = new RandomAccessFile(filePath.toFile(), "r")) {
            var fileLength = file.length();
            var pointer = fileLength - 1;
            var currentLine = new StringBuilder();

            while (pointer >= 0 && lines.size() < nLinies) {
                file.seek(pointer);
                int readByte = file.read();
                if (readByte == '\n') {
                    if (currentLine.length() > 0) {
                        lines.addFirst(currentLine.reverse().toString());
                        currentLine.setLength(0);
                    }
                } else {
                    currentLine.append((char) readByte);
                }
                pointer--;
            }
            // Add the last line if present
            if (currentLine.length() > 0) {
                lines.addFirst(currentLine.reverse().toString());
            }
            return lines;
        } catch (Exception ex) {
            log.error("[LogService.readLastNLines] Error no controlat", ex);
            throw new ComandaApiException("Error llegint últimes línia del fitxer de log", ex);
        }
    }

    private static Long getValidLineCount(Long nLinies) {
        if (nLinies == null) {
            nLinies = minNLinies;
        } else if (nLinies > maxNLinies) {
            nLinies = maxNLinies;
        } else if (nLinies < minNLinies) {
            nLinies = minNLinies;
        }
        return nLinies;
    }

    public static String getDirectoryLogsFromJbossServerProperties() {
        // Intentar obtenir el directori de logs de JBoss des de propietats del sistema
        String jbossLogDir = System.getProperty("jboss.server.log.dir");

        if (jbossLogDir != null && !jbossLogDir.isEmpty()) {
            return jbossLogDir;
        }

        // Alternativa: usar jboss.server.base.dir + /log
        String jbossBaseDir = System.getProperty("jboss.server.base.dir");
        if (jbossBaseDir != null && !jbossBaseDir.isEmpty()) {
            return jbossBaseDir + File.separator + "log";
        }

        return null;

    }

    @Nonnull
    protected static Path resolveLogDirectoryPath(String directoriPath) throws ComandaApiException {
        if (directoriPath == null || directoriPath.isEmpty()) {
            directoriPath = getDirectoryLogsFromJbossServerProperties();
            if (directoriPath == null) {
                throw new ComandaApiException("No s'ha pogut determinar el directori de logs");
            }
        }

        var filesPath = Paths.get(directoriPath);
        if (!Files.exists(filesPath) || !Files.isDirectory(filesPath)) {
            throw new ComandaApiException("El directori de logs no existeix: " + directoriPath);
        }

        return filesPath;
    }

    protected static FitxerInfo getFitxerInfo(Path f) {
        var sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        FitxerInfo fitxer = null;
        try {
            var file = f.toFile();
            var nom = file.getName();
            if (!(nom.toLowerCase().startsWith("server.log") || nom.toLowerCase().contains(appNom.toLowerCase()))) {
                return null;
            }
            var attr = Files.readAttributes(f, BasicFileAttributes.class);
            var dataCreacio = attr.creationTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            var dataModificacio = attr.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            var mida = file.length();
            fitxer = new FitxerInfo().nom(nom)
                    .mida(mida)
                    .mimeType(getMimeTypeByExtension(nom))
                    .dataCreacio(dataCreacio)
                    .dataModificacio(dataModificacio);
        } catch (Exception ex) {
            log.error("Error obtenint la info del fitxer " + f.getFileName(), ex);
        }
        return fitxer;
    }

    protected static byte[] getFitxerContingut(Path filePath, FitxerInfo fitxer) throws IOException {
        byte[] contingut = Files.readAllBytes(filePath);
        var mime = fitxer.getMimeType();
        if (mime == null || !isCompressedFile(mime)) {
            contingut = compressFile(contingut, fitxer.getNom());
            fitxer.setMimeType("application/zip");
            fitxer.setNom(changeExtensionToZip(fitxer.getNom()));
        }
        return contingut;
    }

    public static byte[] compressFile(byte[] fileData, String fileName) throws IOException {

        var byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            var zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(fileData);
            zipOutputStream.closeEntry();
        }
        // Return the compressed byte array
        return byteArrayOutputStream.toByteArray();
    }

    public static String changeExtensionToZip(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot == -1)
                ? fileName + ".zip"
                : fileName.substring(0, dot) + ".zip";
    }


    // Obtenir mimeType d'un fitxer

    public static String getMimeType(File file) {
        try {
            Tika tika = new Tika();
            return tika.detect(file);
        } catch (Exception e) {
            log.error("No ha estat possible calcular el mimeType del fitxer " + file.getPath(), e);
        }
        return null;
    }

    public static String getMimeTypeByExtension(Path path) {
        return getMimeTypeByExtension(path.getFileName().toString());
    }

    public static String getMimeTypeByExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "application/octet-stream";
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
    }

    public static boolean isTextPlain(String mime) {
        return "text/plain".equals(mime);
    }

    public static boolean isCompressedFile(String mime) {
        if (mime == null || mime.isBlank()) {
            return false;
        }
        return COMPRESSED_MIME_TYPES.contains(mime.toLowerCase());
    }


    protected static final Map<String, String> MIME_TYPES = Map.ofEntries(
            // Text / web
            Map.entry("log", "text/plain"),
            Map.entry("0", "text/plain"),   // Cas estrany detectat a DEV
            Map.entry("txt", "text/plain"),
            Map.entry("html", "text/html"),
            Map.entry("htm", "text/html"),
            Map.entry("css", "text/css"),
            Map.entry("js", "application/javascript"),
            Map.entry("json", "application/json"),
            Map.entry("xml", "application/xml"),

            // Documents
            Map.entry("pdf", "application/pdf"),

            // Imatges
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("svg", "image/svg+xml"),

            // Compressió
            Map.entry("zip", "application/zip"),
            Map.entry("rar", "application/vnd.rar"),
            Map.entry("7z", "application/x-7z-compressed"),
            Map.entry("gz", "application/gzip"),
            Map.entry("gzip", "application/gzip"),
            Map.entry("tar", "application/x-tar"),
            Map.entry("tgz", "application/gzip"),
            Map.entry("bz2", "application/x-bzip2"),

            // Àudio / vídeo
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("mp4", "video/mp4")
    );

    protected static final Set<String> COMPRESSED_MIME_TYPES = Set.of(
            "application/zip",
            "application/x-zip-compressed",
            "application/x-7z-compressed",
            "application/x-rar-compressed",
            "application/vnd.rar",
            "application/gzip",
            "application/x-gzip",
            "application/x-tar"
    );
}
