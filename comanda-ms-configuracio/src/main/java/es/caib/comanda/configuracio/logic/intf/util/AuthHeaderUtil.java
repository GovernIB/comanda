    package es.caib.comanda.configuracio.logic.intf.util;

    import lombok.NoArgsConstructor;
    import org.springframework.core.env.Environment;
    import org.springframework.http.HttpEntity;
    import org.springframework.http.HttpHeaders;

    import java.nio.charset.StandardCharsets;
    import java.util.Base64;
    import java.util.Optional;

    /**
     * Util per a la generació de capçaleres d'autenticació.
     * <p>
     * Centralitza la lògica de resolució de credencials, permetent:
     * <ul>
     *   <li>Valors literals definits a l'entornApp</li>
     *   <li>Valors resolts des de l'{@code Environment} (quan {@code parametreAuth} = true)</li>
     *   <li>Fallback a valors globals de l'aplicació</li>
     * </ul>
     *
     * @author Límit Tecnologies
     */
    @NoArgsConstructor
    public final class AuthHeaderUtil {

        /**
         * Construeix un {@link HttpEntity} amb les capçaleres d'autenticació Basic Auth.
         *
         * @param valorStaticUser      Usuari global de fallback
         * @param valorStaticPassword  Contrasenya global de fallback
         * @param nomUsuariAuth        Usuari definit a l'entornApp (literal o clau d'Environment)
         * @param contrasenyaAuth      Contrasenya definida a l'entornApp (literal o clau d'Environment)
         * @param parametreAuth        Si és true, els valors de l'entornApp es busquen a l'Environment
         * @param environment          Environment de Spring per resoldre propietats
         * @return {@link HttpEntity} amb la capçalera Authorization, sense capçalera si no poden resoldre les credencials
         */
        public static HttpEntity<Void> buildAuthHttpEntity(
                String valorStaticUser,
                String valorStaticPassword,
                String nomUsuariAuth,
                String contrasenyaAuth,
                boolean parametreAuth,
                Environment environment) {
            String nomUsuari = buildValorAuth(valorStaticUser, nomUsuariAuth, parametreAuth, environment);
            if (nomUsuari == null || nomUsuari.isBlank()) {
                return new HttpEntity<>((HttpHeaders) null);
            }

            String contrasenya = Optional.ofNullable(
                    buildValorAuth(valorStaticPassword, contrasenyaAuth, parametreAuth, environment)
            ).orElse("");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", basicAuthHeader(nomUsuari, contrasenya));
            return new HttpEntity<>(headers);
        }

        /**
         * Resol un valor d'autenticació segons la configuració.
         * <ol>
         *   <li>Si {@code valorEntornApp} no és null i {@code parametreAuth} = false: retorna el valor literal</li>
         *   <li>Si {@code valorEntornApp} no és null i {@code parametreAuth} = true: busca a l'Environment</li>
         *   <li>Si {@code valorEntornApp} és null: retorna el valor estàtic (global)</li>
         * </ol>
         */
        public static String buildValorAuth(
                String valorStatic,
                String valorEntornApp,
                boolean parametreAuth,
                Environment environment) {
            if (valorEntornApp != null) {
                if (!parametreAuth) {
                    return valorEntornApp;
                }
                return environment.getProperty(valorEntornApp);
            }
            return valorStatic;
        }

        /** Genera la capçalera Basic Auth en format "Basic base64(user:password)". */
        private static String basicAuthHeader(String user, String password) {
            String token = Base64.getEncoder().encodeToString(
                    (user + ":" + password).getBytes(StandardCharsets.UTF_8)
            );
            return "Basic " + token;
        }

    }