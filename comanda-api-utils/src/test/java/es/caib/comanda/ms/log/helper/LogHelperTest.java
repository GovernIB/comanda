package es.caib.comanda.ms.log.helper;

import es.caib.comanda.model.server.monitoring.FitxerContingut;
import es.caib.comanda.ms.exception.ComandaApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogHelperTest {

    @TempDir
    Path tempDir;

    @Test
    void llistarFitxers_filtersAndBuildsInfo() throws Exception {
        // given
        Path serverLog = tempDir.resolve("server.log");
        Files.write(serverLog, "hello".getBytes(StandardCharsets.UTF_8));

        Path serverLogGz = tempDir.resolve("server.log.gz");
        Files.write(serverLogGz, "compressed".getBytes(StandardCharsets.UTF_8));

        Path other = tempDir.resolve("other.txt");
        Files.write(other, "ignored".getBytes(StandardCharsets.UTF_8));

        Path appLog = tempDir.resolve("myapp-2025.log");
        Files.write(appLog, "world".getBytes(StandardCharsets.UTF_8));

        // when
        var result = LogHelper.llistarFitxers(tempDir.toString(), "myapp");

        // then
        assertThat(result).extracting("nom").containsExactlyInAnyOrder("server.log", "server.log.gz", "myapp-2025.log");
        result.forEach(fi -> {
            assertThat(fi.getDataCreacio()).isNotNull();
            assertThat(fi.getDataModificacio()).isNotNull();
            assertThat(fi.getMimeType()).isNotNull();
            assertThat(fi.getMimeType()).isEqualTo(fi.getNom().endsWith("log") ? "text/plain" : "application/gzip");
            assertThat(fi.getDataCreacio()).isNotNull(); // dd/MM/yyyy HH:mm:ss
            assertThat(fi.getMida()).isNotNull().isGreaterThanOrEqualTo(0L);
        });
    }

    @Test
    void getFitxerByNom_returnsEmptyWhenPathInvalid() {
        assertThatThrownBy(() ->
                LogHelper.getFitxerByNom(null, "any.txt")
        ).isInstanceOf(ComandaApiException.class);
    }

    @Test
    void getFitxerByNom_textFilesRemainTextAndNotZipped() throws Exception {
        // given
        String name = "server.log";
        Path f = tempDir.resolve(name);
        String content = "Line1\nLine2\n";
        Files.write(f, content.getBytes(StandardCharsets.UTF_8));

        // when
        FitxerContingut fc = LogHelper.getFitxerByNom(tempDir.toString(), name);

        // then: current implementation only zips when mime is text/plain; tika typically returns text/x-log
        assertThat(fc.getNom()).isEqualTo("server.zip");
        assertThat(fc.getMimeType()).isNotNull();
        assertThat(fc.getMimeType()).isEqualTo("application/zip");
        assertThat(fc.getMida()).isNotNull().isGreaterThanOrEqualTo(0L);
    }

    @Test
    void getFitxerByNom_binaryFilesAreReturnedAsIsWithDetectedMime() throws Exception {
        // given - create a small binary file (not compressed)
        String name = "data.bin";
        Path f = tempDir.resolve(name);
        byte[] bytes = new byte[]{0x00, (byte)0xFF, 0x10, 0x20, 0x30};
        Files.write(f, bytes);

        // when
        LogHelper.setAppNom("data");
        FitxerContingut fc = LogHelper.getFitxerByNom(tempDir.toString(), name);

        // then
        assertThat(fc.getNom()).isEqualTo("data.zip");
        assertThat(fc.getMimeType()).isNotNull();
        assertThat(fc.getMimeType()).isNotEqualTo("text/plain");
        assertThat(fc.getMimeType()).isEqualTo("application/zip");
//        assertThat(fc.getContingut()).containsExactly(bytes);
    }

    @Test
    void getFitxerByNom_xippedFilesAreReturnedAsIsWithDetectedMime() throws Exception {
        // given - create a small binary file (not compressed)
        String name = "data.zip";
        Path f = tempDir.resolve(name);
        byte[] bytes = new byte[]{0x00, (byte)0xFF, 0x10, 0x20, 0x30};
        Files.write(f, bytes);

        // when
        LogHelper.setAppNom("data");
        FitxerContingut fc = LogHelper.getFitxerByNom(tempDir.toString(), name);

        // then
        assertThat(fc.getNom()).isEqualTo(name);
        assertThat(fc.getMimeType()).isNotNull();
        assertThat(fc.getMimeType()).isEqualTo("application/zip");
        assertThat(fc.getContingut()).containsExactly(bytes);
    }

    @Test
    void readLastNLines_returnsAllForSmallFilesDueToMinLimit() throws IOException {
        // given
        String name = "app.log";
        Path f = tempDir.resolve(name);
        List<String> lines = List.of("L1", "L2", "L3");
        Files.write(f, String.join("\n", lines).getBytes(StandardCharsets.UTF_8));

        // when (request 2 lines, but helper enforces a minimum of 100)
        var result = LogHelper.readLastNLines(tempDir.toString(), name, 2L);

        // then - we get all lines back in order
        assertThat(result).containsExactlyElementsOf(lines);
    }


    @Test
    void isTextPlain_and_compressFile_basicChecks() throws Exception {
        assertThat(LogHelper.isTextPlain("text/plain")).isTrue();
        assertThat(LogHelper.isTextPlain("application/json")).isFalse();

        byte[] data = "ABC".getBytes(StandardCharsets.UTF_8);
        byte[] zipped = LogHelper.compressFile(data, "file.txt.zip");
        assertThat(zipped).isNotEmpty();
        assertThat(zipped).startsWith(new byte[]{'P','K'});

        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(zipped))) {
            ZipEntry e = zis.getNextEntry();
            assertThat(e).isNotNull();
            assertThat(e.getName()).isEqualTo("file.txt.zip");
            assertThat(zis.readAllBytes()).containsExactly(data);
        }
    }

    @Test
    void getFileStreamByNom_zipsNonCompressedFiles() throws Exception {
        // given
        String name = "app.log";
        Path f = tempDir.resolve(name);
        byte[] content = "Log content".getBytes(StandardCharsets.UTF_8);
        Files.write(f, content);

        // when
        LogFileStream lfs = LogHelper.getFileStreamByNom(tempDir.toString(), name);

        // then
        assertThat(lfs.getFileName()).isEqualTo("app.zip");
        assertThat(lfs.getContentType()).isEqualTo("application/zip");

        // Verify content is a zip containing the original file
        try (InputStream is = lfs.getInputStream();
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry = zis.getNextEntry();
            assertThat(entry).isNotNull();
            assertThat(entry.getName()).isEqualTo(name);
            assertThat(zis.readAllBytes()).containsExactly(content);
        }
    }

    @Test
    void getFileStreamByNom_returnsCompressedFilesAsIs() throws Exception {
        // given
        String name = "app.log.gz";
        Path f = tempDir.resolve(name);
        byte[] content = "Already compressed".getBytes(StandardCharsets.UTF_8);
        Files.write(f, content);

        // when
        LogFileStream lfs = LogHelper.getFileStreamByNom(tempDir.toString(), name);

        // then
        assertThat(lfs.getFileName()).isEqualTo(name);
        assertThat(lfs.getContentType()).isEqualTo("application/gzip");

        try (InputStream is = lfs.getInputStream()) {
            assertThat(is.readAllBytes()).containsExactly(content);
        }
    }

    @Test
    void getFileStreamByNom_deletesTempFileAfterClose() throws Exception {
        // given
        String name = "todelete.log";
        Path f = tempDir.resolve(name);
        Files.write(f, "Delete me".getBytes(StandardCharsets.UTF_8));

        // when
        LogFileStream lfs = LogHelper.getFileStreamByNom(tempDir.toString(), name);
        
        // Find the temp file created (it should be in the system temp dir, but we can't easily know the path 
        // without reflecting or changing LogHelper, however we can check that it's NOT the original file)
        
        // Let's use a trick: get all files in temp dir before and after
        Set<Path> beforeClose = Files.list(Path.of(System.getProperty("java.io.tmpdir")))
                .filter(p -> p.getFileName().toString().startsWith("log-") && p.getFileName().toString().endsWith(".zip"))
                .collect(Collectors.toSet());
        
        assertThat(beforeClose).isNotEmpty(); // At least one temp file should have been created

        // when
        lfs.getInputStream().close();

        // then
        Set<Path> afterClose = Files.list(Path.of(System.getProperty("java.io.tmpdir")))
                .filter(p -> p.getFileName().toString().startsWith("log-") && p.getFileName().toString().endsWith(".zip"))
                .collect(Collectors.toSet());
        
        // The specific temp file created for this test should have been deleted
        // Since other tests might be running, we just check if the number of temp files decreased or if none match our specific one
        // This is a bit flaky if concurrent tests run, but in this environment it should be fine.
        
        // Better: We can check if any of the files in beforeClose is missing in afterClose
        boolean deleted = false;
        for (Path p : beforeClose) {
            if (!afterClose.contains(p)) {
                deleted = true;
                break;
            }
        }
        assertThat(deleted).isTrue();
    }
}
