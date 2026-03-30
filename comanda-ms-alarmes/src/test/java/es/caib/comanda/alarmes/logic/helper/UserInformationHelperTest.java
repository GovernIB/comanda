package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.UsuariServiceClient;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInformationHelperTest {

    @Mock
    private UsuariServiceClient usuariServiceClient;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @InjectMocks
    private UserInformationHelper userInformationHelper;

    private static final String USERNAME = "testuser";
    private static final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        // El constructor crea un LdapUserInformationPlugin(""); que no podem mockejar fàcilment
        // però podem provar els mètodes que no l'usen o veure com es comporta.
    }

    @Test
    @DisplayName("Cerca usuari per codi amb èxit")
    void usuariFindByUsername_quanExisteix_retornaUsuari() {
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
    }

    @Test
    @DisplayName("Retorna null quan l'usuari no existeix")
    void usuariFindByUsername_quanNoExisteix_retornaNull() {
        // Arrange
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(usuariServiceClient.getOneByCodiInternal(anyString(), anyString()))
                .thenReturn(null);

        // Act
        Usuari result = userInformationHelper.usuariFindByUsername(USERNAME);

        // Assert
        assertThat(result).isNull();
    }
}
