package es.caib.comanda.api.test;

import java.util.Optional;

final class ClientProps {
    static final String PROP_BASE = "comanda.api.basePath";
    static final String PROP_USER = "comanda.api.user";
    static final String PROP_PWD = "comanda.api.password";
    static final String PROP_AUTH = "comanda.api.authHeader";

    static Optional<String> get(String key) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key.replace('.', '_').toUpperCase());
        return Optional.ofNullable(v).filter(s -> !s.isBlank());
    }
}
