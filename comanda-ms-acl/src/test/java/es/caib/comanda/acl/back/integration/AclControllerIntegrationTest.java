package es.caib.comanda.acl.back.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

//@SpringBootTest(classes = AclControllerIntegrationTest.TestApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // H2 in-memory DB for integration tests
        "spring.datasource.url=jdbc:h2:mem:acl-it;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        // JPA + Liquibase
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        // Use schema.sql instead of Liquibase for faster/simpler IT setup
        "spring.liquibase.enabled=false",
        "spring.sql.init.mode=always",
        "spring.jpa.defer-datasource-initialization=true",
        // Logging quieter
        "logging.level.org.springframework.security.acls=INFO",
        // Disable jboss tx detection
        "comanda.persist.container-transactions-disabled=true",
        "es.caib.comanda.client.base.url=http://localhost:8080/api",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AclControllerIntegrationTest {

    /*@SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
    })
    @ComponentScan({"es.caib.comanda.acl"})
    @Import({
            AclPersistenceConfig.class,
            AuditingConfig.class,
            SpringAclConfig.class,
            AclControllerIntegrationTest.CachingTestConfig.class,
            AclControllerIntegrationTest.SecurityTestConfig.class,
            AclControllerIntegrationTest.JwtTestConfig.class
    })
    static class TestApp {

    }

    @TestConfiguration
    static class CachingTestConfig {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public CacheManager cacheManager() {
            // Provide caches used by SpringAclConfig and AclEntryServiceImpl
            return new ConcurrentMapCacheManager("springAclCache", "aclCheckCache");
        }
    }

    @TestConfiguration
    static class SecurityTestConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @TestConfiguration
    static class JwtTestConfig {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder() {
            return token -> org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .claim("preferred_username", "test-user")
                    .claim("azp", "comanda-client")
                    .build();
        }

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public es.caib.comanda.ms.back.config.JwtAuthConverter jwtAuthConverter() {
            return new es.caib.comanda.ms.back.config.JwtAuthConverter();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AclEntryService aclEntryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AclEntry entry(ResourceType rt, long rid, SubjectType st, String sv, AclAction action, AclEffect effect) {
        AclEntry e = new AclEntry();
        e.setResourceType(rt);
        e.setResourceId(rid);
        e.setSubjectType(st);
        e.setSubjectValue(sv);
        e.setAction(action);
        e.setEffect(effect);
        return e;
    }


    @Test
    void single_action_allowed_for_user() throws Exception {
        // Given: user1 has READ on ENTORN_APP:100
        aclEntryService.create(entry(ResourceType.ENTORN_APP, 100L, SubjectType.USER, "user1", AclAction.READ, AclEffect.ALLOW), null);

        String payload = "{" +
                "\"user\":\"user1\"," +
                "\"roles\":[\"ROLE_USER\"]," +
                "\"resourceType\":\"ENTORN_APP\"," +
                "\"resourceId\":100," +
                "\"action\":\"READ\"" +
                "}";

        String body = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"allowed\":true");
    }

    @Test
    void single_action_denied_when_not_granted() throws Exception {
        // No ACL entries created -> should be denied
        String payload = "{" +
                "\"user\":\"userX\"," +
                "\"roles\":[\"ROLE_USER\"]," +
                "\"resourceType\":\"ENTORN_APP\"," +
                "\"resourceId\":200," +
                "\"action\":\"READ\"" +
                "}";

        String body = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"allowed\":false");
    }

    @Test
    void multi_actions_any_true_if_any_granted() throws Exception {
        // Given: role ROLE_EDITOR has WRITE allowed on ENTORN_APP:300; user has that role
        aclEntryService.create(entry(ResourceType.ENTORN_APP, 300L, SubjectType.ROLE, "ROLE_EDITOR", AclAction.WRITE, AclEffect.ALLOW), null);

        String payload = "{" +
                "\"user\":\"user2\"," +
                "\"roles\":[\"ROLE_EDITOR\"]," +
                "\"resourceType\":\"ENTORN_APP\"," +
                "\"resourceId\":300," +
                "\"actions\":[\"READ\",\"WRITE\"]," +
                "\"mode\":\"ANY\"" +
                "}";

        String body = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"allowed\":true");
    }

    @Test
    void multi_actions_all_true_only_if_all_granted() throws Exception {
        // Given: user3 has READ and WRITE on ENTORN_APP:400
        aclEntryService.create(entry(ResourceType.ENTORN_APP, 400L, SubjectType.USER, "user3", AclAction.READ, AclEffect.ALLOW), null);
        aclEntryService.create(entry(ResourceType.ENTORN_APP, 400L, SubjectType.USER, "user3", AclAction.WRITE, AclEffect.ALLOW), null);

        String payloadOk = "{" +
                "\"user\":\"user3\"," +
                "\"roles\":[]," +
                "\"resourceType\":\"ENTORN_APP\"," +
                "\"resourceId\":400," +
                "\"actions\":[\"READ\",\"WRITE\"]," +
                "\"mode\":\"ALL\"" +
                "}";

        String ok = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadOk))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(ok).contains("\"allowed\":true");

        String payloadFail = "{" +
                "\"user\":\"user3\"," +
                "\"roles\":[]," +
                "\"resourceType\":\"ENTORN_APP\"," +
                "\"resourceId\":400," +
                "\"actions\":[\"READ\",\"ADMIN\"]," +
                "\"mode\":\"ALL\"" +
                "}";

        String fail = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadFail))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(fail).contains("\"allowed\":false");
    }

    @Test
    void multi_actions_empty_list_returns_false() throws Exception {
        String payload = "{" +
                "\"user\":\"user4\"," +
                "\"roles\":[]," +
                "\"resourceType\":\"ENTORN_APP\"," +
                "\"resourceId\":500," +
                "\"actions\":[]" +
                "}";

        String body = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"allowed\":false");
    }*/

}
