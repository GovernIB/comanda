package es.caib.comanda.api.test;

import java.util.Optional;

final class ClientProps2 {
    static final String PROP_BASE = "comanda.api2.basePath";
    static final String PROP_USER = "comanda.api2.user";
    static final String PROP_PWD = "comanda.api2.password";
    static final String PROP_AUTH = "comanda.api2.authHeader";

    static Optional<String> get(String key) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key.replace('.', '_').toUpperCase());
        return Optional.ofNullable(v).filter(s -> !s.isBlank());
    }
}
