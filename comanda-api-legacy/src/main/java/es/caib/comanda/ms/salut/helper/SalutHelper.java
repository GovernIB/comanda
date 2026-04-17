package es.caib.comanda.ms.salut.helper;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

public class SalutHelper {

    private static BuildInfo buildInfo;

    public static synchronized BuildInfo getBuildInfo() {
        if (buildInfo == null) {
            buildInfo = generateBuildInfo();
        }
        return buildInfo;
    }

    private static BuildInfo generateBuildInfo() {
        String commitId = null;
        Date buildDate = null;
        String version = null;
        MonitorHelper.SystemInfo systemInfo = MonitorHelper.getSystemInfo();
        InputStream in = null;
        try {
            in = SalutHelper.class.getClassLoader().getResourceAsStream("git.properties");
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                commitId = props.getProperty("git.commit.id");
                version = props.getProperty("git.build.version");
                buildDate = parseGitBuildTime(props.getProperty("git.build.time"));
            }
        } catch (Exception ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
        }
        return new BuildInfo(buildDate, systemInfo != null ? systemInfo.getJdkVersion() : null, commitId, version);
    }

    public static Date parseGitBuildTime(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        String normalized = normalizeOffset(value);
        Date parsed = parseDate(normalized, "yyyy-MM-dd'T'HH:mm:ssZ");
        if (parsed != null) {
            return parsed;
        }
        return parseDate(normalized, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    private static String normalizeOffset(String value) {
        if (value.endsWith("Z")) {
            return value.substring(0, value.length() - 1) + "+0000";
        }
        if (value.length() > 5) {
            char sign = value.charAt(value.length() - 5);
            if ((sign == '+' || sign == '-') && value.charAt(value.length() - 3) == ':') {
                return value.substring(0, value.length() - 3) + value.substring(value.length() - 2);
            }
        }
        return value;
    }

    private static Date parseDate(String value, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    public static class BuildInfo {
        private final Date buildDate;
        private final String buildJDK;
        private final String commitId;
        private final String version;

        public BuildInfo(Date buildDate, String buildJDK, String commitId, String version) {
            this.buildDate = buildDate;
            this.buildJDK = buildJDK;
            this.commitId = commitId;
            this.version = version;
        }

        public Date getBuildDate() {
            return buildDate;
        }

        public String getBuildJDK() {
            return buildJDK;
        }

        public String getCommitId() {
            return commitId;
        }

        public String getVersion() {
            return version;
        }
    }
}
