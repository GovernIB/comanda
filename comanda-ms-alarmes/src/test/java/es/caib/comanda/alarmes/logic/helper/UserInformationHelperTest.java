package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.UsuariServiceClient;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import feign.FeignException;
import org.fundaciobit.pluginsib.userinformation.ldap.LdapUserInformationPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.EntityModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a UserInformationHelper")
class UserInformationHelperTest {

    @Mock
    private UsuariServiceClient usuariServiceClient;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AlarmaClientHelper alarmaClientHelper;

    @Mock
    private Environment environment;

    @InjectMocks
    private UserInformationHelper userInformationHelper;

    private static final String USERNAME = "testuser";
    private static final String ROLE = "ROLE_ADMIN";
    private static final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        // Mockeig lenient de l'Environment per al constructor
        // (evita NPE si l'array estàtic PROPS_LDAP té valors)
        lenient().when(environment.getProperty(anyString())).thenReturn("mocked_value");
    }

    // ========================================================================
    // 1. TESTOS PER A usuariFindByUsername
    // ========================================================================

    @Test
    @DisplayName("usuariFindByUsername: retorna l'usuari quan el client el troba")
    void usuariFindByUsername_quanExisteix_llavorsRetornaUsuari() {
        // Arrange
        Usuari usuari = Usuari.builder().codi(USERNAME).nom("Test User").build();
        EntityModel<Usuari> entityModel = EntityModel.of(usuari);

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(usuariServiceClient.getOneByCodiInternal(eq(USERNAME), eq(AUTH_HEADER))).thenReturn(entityModel);

        // Act
        Usuari result = userInformationHelper.usuariFindByUsername(USERNAME);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCodi()).isEqualTo(USERNAME);
        verify(usuariServiceClient, times(1)).getOneByCodiInternal(USERNAME, AUTH_HEADER);
    }

    @Test
    @DisplayName("usuariFindByUsername: retorna null quan el client retorna null")
    void usuariFindByUsername_quanClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(usuariServiceClient.getOneByCodiInternal(anyString(), anyString())).thenReturn(null);

        // Act
        Usuari result = userInformationHelper.usuariFindByUsername(USERNAME);

        // Assert
        assertThat(result).isNull();
        verify(usuariServiceClient, times(1)).getOneByCodiInternal(USERNAME, AUTH_HEADER);
    }

    @Test
    @DisplayName("usuariFindByUsername: retorna null quan es llança FeignException.NotFound")
    void usuariFindByUsername_quanLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(usuariServiceClient.getOneByCodiInternal(anyString(), anyString()))
            .thenThrow(FeignException.NotFound.class);

        // Act
        Usuari result = userInformationHelper.usuariFindByUsername(USERNAME);

        // Assert
        assertThat(result).isNull();
        verify(usuariServiceClient, times(1)).getOneByCodiInternal(USERNAME, AUTH_HEADER);
    }

    @Test
    @DisplayName("usuariFindByUsername: propaga l'excepció quan es llança una excepció diferent de NotFound")
    void usuariFindByUsername_quanLlancaExcepcioGenerica_llavorsPropagaExcepcio() {
        // Arrange
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        RuntimeException expectedException = new RuntimeException("Error de xarxa");
        when(usuariServiceClient.getOneByCodiInternal(anyString(), anyString()))
            .thenThrow(expectedException);

        // Act & Assert
        assertThatThrownBy(() -> userInformationHelper.usuariFindByUsername(USERNAME))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Error de xarxa")
            .isSameAs(expectedException);

        verify(usuariServiceClient, times(1)).getOneByCodiInternal(USERNAME, AUTH_HEADER);
    }

    // ========================================================================
    // 2. TESTOS PER A findByRole
    // ========================================================================

    @Test
    @DisplayName("findByRole: retorna l'array d'usuaris quan el plugin els troba")
    void findByRole_quanPluginTrobaUsuaris_llavorsRetornaArray() throws Exception {
        // Arrange
        String[] expectedUsers = {"user1", "user2"};

        // Utilitzem MockedConstruction per mockejar l'objecte creat internament amb 'new'
        try (MockedConstruction<LdapUserInformationPlugin> mocked = mockConstruction(LdapUserInformationPlugin.class,
            (mock, context) -> {
                when(mock.getUsernamesByRol(ROLE)).thenReturn(expectedUsers);
            })) {

            // Reiniciem el helper perquè utilitzi el plugin mockejat en el seu constructor
            userInformationHelper = new UserInformationHelper(usuariServiceClient, httpAuthorizationHeaderHelper, alarmaClientHelper, environment);

            // Act
            String[] result = userInformationHelper.findByRole(ROLE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).containsExactly("user1", "user2");
            assertThat(mocked.constructed()).hasSize(1);
            verify(mocked.constructed().get(0), times(1)).getUsernamesByRol(ROLE);
        }
    }

    @Test
    @DisplayName("findByRole: retorna array buit quan el plugin no troba usuaris")
    void findByRole_quanPluginNoTrobaUsuaris_llavorsRetornaArrayBuit() {
        try (MockedConstruction<LdapUserInformationPlugin> mocked = mockConstruction(LdapUserInformationPlugin.class,
            (mock, context) -> {
                when(mock.getUsernamesByRol(ROLE)).thenReturn(new String[0]);
            })) {

            userInformationHelper = new UserInformationHelper(usuariServiceClient, httpAuthorizationHeaderHelper, alarmaClientHelper, environment);

            // Act
            String[] result = userInformationHelper.findByRole(ROLE);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("findByRole: llança UserInformationException quan el plugin llança una excepció")
    void findByRole_quanPluginLlancaExcepcio_llavorsLlancaUserInformationException() {
        // Arrange
        RuntimeException pluginException = new RuntimeException("Error LDAP");

        try (MockedConstruction<LdapUserInformationPlugin> mocked = mockConstruction(LdapUserInformationPlugin.class,
            (mock, context) -> {
                when(mock.getUsernamesByRol(ROLE)).thenThrow(pluginException);
            })) {

            userInformationHelper = new UserInformationHelper(usuariServiceClient, httpAuthorizationHeaderHelper, alarmaClientHelper, environment);

            // Act & Assert
            assertThatThrownBy(() -> userInformationHelper.findByRole(ROLE))
                .isInstanceOf(UserInformationHelper.UserInformationException.class)
                .satisfies(ex -> {
                    UserInformationHelper.UserInformationException uie = (UserInformationHelper.UserInformationException) ex;
                    assertThat(uie.getMethod()).isEqualTo("getUsernamesByRol");
                    assertThat(uie.getParams()).containsExactly(ROLE);
                    assertThat(uie.getCause()).isSameAs(pluginException);
                    assertThat(uie.getMessage()).contains("getUsernamesByRol").contains(ROLE);
                });
        }
    }
}
