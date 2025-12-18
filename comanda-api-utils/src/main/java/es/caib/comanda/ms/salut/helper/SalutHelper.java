package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.v1.salut.AppInfo;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Properties;

public class SalutHelper {

    private static BuildInfo buildInfo;

    public static AppInfo generateBasicAppInfo(String codiAplicacio, String nomAplicacio, String versio) {
        BuildInfo buildInfo = getBuildInfo();
        return AppInfo.builder()
                .codi(codiAplicacio)
                .nom(nomAplicacio)
                .versio(versio)
                .revisio(buildInfo.getCommitId())
                .jdkVersion(buildInfo.getBuildJDK())
                .data(buildInfo.getBuildDate())
                .build();
    }


    private static BuildInfo getBuildInfo() {

        if (buildInfo == null) {
            buildInfo = generateBuildInfo();
        }

        return buildInfo;
    }

    private static BuildInfo generateBuildInfo() {

        String commitId = null;
        OffsetDateTime buildDate = null;
        MonitorHelper.SystemInfo systemInfo = MonitorHelper.getSystemInfo();

        try (InputStream in = SalutHelper.class.getClassLoader().getResourceAsStream("git.properties")) {
            Properties props = new Properties();
            props.load(in);
            commitId = props.getProperty("git.commit.id");
            String buildTime = props.getProperty("git.build.time");
            if (buildTime != null && !buildTime.isEmpty()) {
                buildDate = getDate(buildTime);
            }
        } catch (Exception e) {}

        return BuildInfo.builder()
                .buildDate(buildDate)
                .buildJDK(systemInfo.getJdkVersion())
                .commitId(commitId)
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

    @Builder
    @Getter
    public static class BuildInfo {

        private final OffsetDateTime buildDate;
        private final String buildJDK;
        private final String commitId;
    }

}
