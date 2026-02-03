package es.caib.comanda.ms.log.helper;

import es.caib.comanda.model.server.monitoring.FitxerContingut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(fi.getDataCreacio()).hasSize(19); // dd/MM/yyyy HH:mm:ss
            assertThat(fi.getMida()).isNotNull().isGreaterThanOrEqualTo(0L);
        });
    }

    @Test
    void getFitxerByNom_returnsEmptyWhenPathInvalid() {
        FitxerContingut fc = LogHelper.getFitxerByNom(null, "any.txt");
        assertThat(fc.getContingut()).isNull();
        assertThat(fc.getMimeType()).isNull();
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
        assertThat(fc.getNom()).isEqualTo(name);
        assertThat(fc.getMimeType()).isNotNull();
        assertThat(fc.getMimeType()).startsWith("text/");
        assertThat(new String(fc.getContingut(), StandardCharsets.UTF_8)).isEqualTo(content);
    }

    @Test
    void getFitxerByNom_binaryFilesAreReturnedAsIsWithDetectedMime() throws Exception {
        // given - create a small binary file (not text/plain)
        String name = "data.bin";
        Path f = tempDir.resolve(name);
        byte[] bytes = new byte[]{0x00, (byte)0xFF, 0x10, 0x20, 0x30};
        Files.write(f, bytes);

        // when
        FitxerContingut fc = LogHelper.getFitxerByNom(tempDir.toString(), name);

        // then
        assertThat(fc.getNom()).isEqualTo(name);
        assertThat(fc.getMimeType()).isNotNull();
        assertThat(fc.getMimeType()).isNotEqualTo("text/plain");
        assertThat(fc.getMimeType()).isNotEqualTo("application/zip");
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
}
