package es.caib.comanda.configuracio.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test-h2")
//@TestPropertySource(properties = {
//        "es.caib.comanda.properties=classpath:application-test-h2.properties",
//        "es.caib.comanda.system.properties=classpath:application-test-h2.properties"
//})
public class H2IntegrationTest {

//    @MockBean
//    JwtDecoder jwtDecoder;
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Test
//    void testH2Connection() throws SQLException {
//        assertNotNull(dataSource);
//        try (Connection connection = dataSource.getConnection()) {
//            assertNotNull(connection);
//            String url = connection.getMetaData().getURL();
//            assertTrue(url.contains("jdbc:h2:mem:testdb"), "URL should be H2 mem: " + url);
//        }
//    }
}
