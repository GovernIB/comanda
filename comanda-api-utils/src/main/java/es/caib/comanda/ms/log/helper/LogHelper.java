package es.caib.comanda.ms.log.helper;

import es.caib.comanda.model.v1.log.FitxerContingut;
import es.caib.comanda.model.v1.log.FitxerInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class LogHelper {

    private static final Long maxNLinies = 10000L;
    private static final Long minNLinies = 100L;

    public static List<FitxerInfo> llistarFitxers(String directoriPath, String appNom) {

        if (directoriPath == null || directoriPath.isEmpty()) {
            return new ArrayList<>();
        }
        List<FitxerInfo> fitxers = new ArrayList<>();
        var sdf = new SimpleDateFormat("dd/MM/yyyy");
        try (Stream<Path> paths = Files.list(Paths.get(directoriPath))) {
            paths.filter(Files::isRegularFile).forEach(f -> {
                var file = f.toFile();
                var nom = file.getName();
                if (!(nom.toLowerCase().startsWith("server.log") || nom.toLowerCase().contains(appNom.toLowerCase()))) {
                    return;
                }
                try {
                    var attr = Files.readAttributes(f, BasicFileAttributes.class);
                    var dataCreacio = sdf.format(new Date(attr.creationTime().toMillis()));
                    var dataModificacio = sdf.format(new Date(attr.lastModifiedTime().toMillis()));
                    var mida = file.length();
                    var fitxer = FitxerInfo.builder().nom(nom)
                            .mida(mida)
                            .mimeType(getMimeTypeByExtension(nom))
                            .dataCreacio(dataCreacio)
                            .dataModificacio(dataModificacio).build();
                    fitxers.add(fitxer);
                } catch (Exception ex) {
                    log.error("Errror obtenint la info del fitxer " + f.getFileName(), ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error generant la info dels fitxers pel directori " + directoriPath, ex);
        }
        return fitxers;
    }

    public static FitxerContingut getFitxerByNom(String directoriPath, String nom) {

        try {
            if (directoriPath == null || directoriPath.isEmpty()) {
                return FitxerContingut.builder().build();
            }
            var filePath = Paths.get(directoriPath, nom);
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return FitxerContingut.builder().build();
            }
            var file = filePath.toFile();
            var attr = Files.readAttributes(filePath, BasicFileAttributes.class);
            var sdf = new SimpleDateFormat("dd/MM/yyyy");
            var dataCreacio = sdf.format(new Date(attr.creationTime().toMillis()));
            var dataModificacio = sdf.format(new Date(attr.lastModifiedTime().toMillis()));
            var contingut = Files.readAllBytes(filePath);
            var mime = getMimeType(file);
            if (mime == null || isTextPlain(mime)) {
                contingut = compressFile(contingut, nom + ".zip");
                mime = "application/zip";
            }
            return FitxerContingut.builder().contingut(contingut)
                    .mimeType(mime)
                    .nom(file.getName())
                    .dataCreacio(dataCreacio)
                    .dataModificacio(dataModificacio)
                    .mida(contingut.length).build();
        } catch (IOException ex) {
            log.error("Error reading file content for " + nom, ex);
            return FitxerContingut.builder().build();
        }
    }

    public static List<String> readLastNLines(String directoriPath, String nomFitxer, Long nLinies) {

        try {
            if (nomFitxer == null || nomFitxer.isEmpty() || nLinies == null) {
                log.error("[LogService.readLastNLines] Parametres incorrectes, nomFitxer " + nomFitxer + " nLinies" + nLinies);
                return new ArrayList<>();
            }
            if (directoriPath == null || directoriPath.isEmpty()) {
                log.error("[LogService.nomFitxer] No s'ha especificat valor a la propietat \"es.caib.notib.plugin.fitxer.logs.path\"");
                return new ArrayList<>();
            }
            if (nLinies > maxNLinies) {
                nLinies = maxNLinies;
            } else if (nLinies < minNLinies) {
                nLinies = minNLinies;
            }
            var path = Paths.get(directoriPath, nomFitxer);
            try (var file = new RandomAccessFile(path.toFile(), "r")) {
                var fileLength = file.length();
                LinkedList<String> lines = new LinkedList<>();
                var pointer = fileLength - 1;
                var currentLine = new StringBuilder();
                char ch;
                while (pointer >= 0 && lines.size() < nLinies) {
                    file.seek(pointer);
                    ch = (char) file.readByte();
                    if (ch == '\n') {
                        if (currentLine.length() > 0) {
                            lines.addFirst(currentLine.reverse().toString());
                            currentLine.setLength(0);
                        }
                    } else {
                        currentLine.append(ch);
                    }
                    pointer--;
                }
                // Add the last line if present
                if (currentLine.length() > 0) {
                    lines.addFirst(currentLine.reverse().toString());
                }
                return lines;
            }
        } catch (Exception ex) {
            log.error("[LogService.readLastNLines] Error no controlat", ex);
            return new ArrayList<>();
        }
    }

    public static String getMimeType(File file) {
        try {
            Tika tika = new Tika();
            return tika.detect(file);
        } catch (Exception e) {
            log.error("No ha estat possible calcular el mimeType del fitxer " + file.getPath(), e);
        }
        return null;
    }

    public static boolean isTextPlain(String mime) {
        return "text/plain".equals(mime);
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

    private static String getMimeTypeByExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "log": case "txt": return "text/plain";
            case "zip": return "application/zip";
            case "0":  // Cas estrany detectat a DEV
            case "gz": return "application/gzip";
            case "html": case "htm": return "text/html";
            case "css": return "text/css";
            case "js": return "application/javascript";
            case "json": return "application/json";
            case "xml": return "application/xml";
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "mp3": return "audio/mpeg";
            case "mp4": return "video/mp4";
            // Afegeix més extensions segons necessitis
            default: return "application/octet-stream";
        }
    }
}
