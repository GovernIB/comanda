package es.caib.comanda.ms.salut.helper;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

public class SalutHelper {

    private static final DateTimeFormatter FORMAT_WITH_COLON = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter FORMAT_WITHOUT_COLON = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private static BuildInfo buildInfo;

    public static BuildInfo getBuildInfo() {

        if (buildInfo == null) {
            buildInfo = generateBuildInfo();
        }

        return buildInfo;
    }

    private static BuildInfo generateBuildInfo() {

        String commitId = null;
        OffsetDateTime buildDate = null;
        String version = null;
        MonitorHelper.SystemInfo systemInfo = MonitorHelper.getSystemInfo();

        try (InputStream in = SalutHelper.class.getClassLoader().getResourceAsStream("git.properties")) {
            Properties props = new Properties();
            props.load(in);
            commitId = props.getProperty("git.commit.id");
            String buildTime = props.getProperty("git.build.time");
            version = props.getProperty("git.build.version");
            if (buildTime != null && !buildTime.isEmpty()) {
                buildDate = parseGitBuildTime(buildTime);
            }
        } catch (Exception e) {}

        return BuildInfo.builder()
                .buildDate(buildDate)
                .buildJDK(systemInfo.getJdkVersion())
                .commitId(commitId)
                .version(version)
                .build();
    }

    private static OffsetDateTime getDate(String isoDate) {

        try {
            Instant instant = Instant.parse(isoDate);
            return instant.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        } catch (DateTimeParseException e) {
            System.out.println("El format de la data és incorrecte: " + e.getMessage());
            return null;
        }
    }

    public static OffsetDateTime parseGitBuildTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            // Prova format ISO normal (+01:00)
            return OffsetDateTime.parse(value, FORMAT_WITH_COLON);
        } catch (DateTimeParseException e) {
            // Prova format sense dos punts (+0100)
            return OffsetDateTime.parse(value, FORMAT_WITHOUT_COLON);
        }
    }

    @Builder
    @Getter
    public static class BuildInfo {

        private final OffsetDateTime buildDate;
        private final String buildJDK;
        private final String commitId;
        private final String version;
    }

}
