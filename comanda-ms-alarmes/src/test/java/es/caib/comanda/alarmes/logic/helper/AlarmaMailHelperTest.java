package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.logic.event.AlarmaMailEventType;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.*;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a AlarmaMailHelper")
class AlarmaMailHelperTest {

    @Mock private AlarmaClientHelper alarmaClientHelper;
    @Mock private MailHelper mailHelper;
    @Mock private UserInformationHelper userInformationHelper;
    @Mock private AlarmaRepository alarmaRepository;
    @Mock private ParametresHelper parametresHelper;

    @InjectMocks
    private AlarmaMailHelper alarmaMailHelper;

    private AlarmaEntity alarma;
    private AlarmaConfigEntity config;
    private EntornApp entornApp;
    private App app;
    private Entorn entorn;

    @BeforeEach
    void setUp() {
        lenient().when(parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_FROM_ADDRESS, "comanda@caib.es")).thenReturn("from@caib.es");
        lenient().when(parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_FROM_NAME, "Comanda")).thenReturn("Comanda");
        lenient().when(parametresHelper.getParametreBoolean(BaseConfig.PROP_ALARMA_LOG_ACTIVACIO, false)).thenReturn(true);

        config = new AlarmaConfigEntity();
        config.setNom("Test Alarma");
        config.setCreatedBy("creator");

        alarma = new AlarmaEntity();
        alarma.setAlarmaConfig(config);
        alarma.setEntornAppId(1L);
        alarma.setMissatge("Missatge d'alarma");
        alarma.setDataActivacio(LocalDateTime.now());

        entornApp = EntornApp.builder()
            .app(AppRef.builder().id(10L).nom("APP").build())
            .entorn(EntornRef.builder().id(20L).nom("ENTORN").build())
            .alarmesEmail("admin@caib.es")
            .build();
        app = EntornAppTestHelper.createApp(10L, "APP Nom");
        entorn = Entorn.builder().id(20L).nom("ENTORN Nom").build();
    }

    private static class EntornAppTestHelper {
        static App createApp(Long id, String nom) {
            App app = new App();
            ReflectionTestUtils.setField(app, "id", id);
            ReflectionTestUtils.setField(app, "nom", nom);
            return app;
        }
    }

    // ========================================================================
    // TESTOS ORIGINALS (MANTINGUTS I OPTIMITZATS)
    // ========================================================================

    @Test
    @DisplayName("Envia correu d'alarma genèrica correctament")
    void sendAlarmaGeneric_quanTotCorrecte_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        alarmaMailHelper.sendAlarmaGeneric(alarma, AlarmaMailEventType.ACTIVACIO);

        verify(mailHelper).sendSimple(eq("from@caib.es"), eq("Comanda"), eq("admin@caib.es"), anyString(), eq("[COMANDA] Alarma activada: Test Alarma"), anyString());
    }

    @Test
    @DisplayName("Envia correu d'alarma genèrica correctament sense nom")
    void sendAlarmaGeneric_quanNoNom_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        config.setNom(null);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        alarmaMailHelper.sendAlarmaGeneric(alarma, AlarmaMailEventType.ACTIVACIO);

        verify(mailHelper).sendSimple(eq("from@caib.es"), eq("Comanda"), eq("admin@caib.es"), anyString(), eq("[COMANDA] Alarma activada"), anyString());
    }

    @Test
    @DisplayName("Envia correu d'alarma a l'usuari creador")
    void sendAlarmaUser_quanUsuariNoAdmin_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        config.setAdmin(false);
        Usuari usuari = Usuari.builder().codi("creator").nom("Creator Name").email("creator@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();

        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        verify(mailHelper).sendSimple(eq("from@caib.es"), anyString(), eq("creator@caib.es"), eq("Creator Name"), eq("[COMANDA] Alarma activada: Test Alarma"), anyString());
    }

    @Test
    @DisplayName("Envia correu d'alarma a administradors")
    void sendAlarmaUser_quanAdmin_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        config.setAdmin(true);
        String[] admins = {"admin1", "admin2"};
        Usuari u1 = Usuari.builder().codi("admin1").nom("A1").email("a1@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();
        Usuari u2 = Usuari.builder().codi("admin2").nom("A2").email("a2@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();

        when(userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN)).thenReturn(admins);
        when(userInformationHelper.usuariFindByUsername("admin1")).thenReturn(u1);
        when(userInformationHelper.usuariFindByUsername("admin2")).thenReturn(u2);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        verify(mailHelper, times(2)).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("No envia correu genèric si no hi ha email configurat")
    void sendAlarmaGeneric_quanNoEmail_noEnvia() {
        entornApp.setAlarmesEmail(null);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);

        alarmaMailHelper.sendAlarmaGeneric(alarma, AlarmaMailEventType.ACTIVACIO);

        verifyNoInteractions(mailHelper);
    }

    @Test
    @DisplayName("Usa email alternatiu si està informat")
    void sendAlarmaUser_quanEmailAlternatiu_usaAlternatiu() throws MessagingException, UnsupportedEncodingException {
        config.setAdmin(false);
        Usuari usuari = Usuari.builder().codi("creator").nom("Creator").email("original@caib.es").emailAlternatiu("alternatiu@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();

        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        verify(mailHelper).sendSimple(anyString(), anyString(), eq("alternatiu@caib.es"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Usa email autogenerat si no hi ha ni email ni email alternatiu")
    void sendAlarmaUser_quanNoEmail_usaAutogenerat() throws MessagingException, UnsupportedEncodingException {
        config.setAdmin(false);
        Usuari usuari = Usuari.builder().codi("creator").nom("Creator").email(null).emailAlternatiu(null).alarmaMail(true).alarmaMailAgrupar(false).build();

        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);
        when(parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_DEFAULT_DOMAIN, "caib.es")).thenReturn("test.es");
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        verify(mailHelper).sendSimple(anyString(), anyString(), eq("creator@test.es"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Envia alarmes agrupades correctament")
    void sendAlarmesAgrupades_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        String[] admins = {"admin1"};
        Usuari u1 = Usuari.builder().codi("admin1").nom("A1").email("a1@caib.es").alarmaMail(true).alarmaMailAgrupar(true).build();

        when(userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN)).thenReturn(admins);
        when(userInformationHelper.usuariFindByUsername("admin1")).thenReturn(u1);
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(any())).thenReturn(Collections.singletonList(alarma));
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(any())).thenReturn(Collections.emptyList());
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByDataActivacioAfterAndDataEnviamentIsNull(any())).thenReturn(Collections.singletonList("user1"));
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(any())).thenReturn(Collections.emptyList());

        Usuari u2 = Usuari.builder().codi("user1").nom("U1").email("u1@caib.es").alarmaMail(true).alarmaMailAgrupar(true).build();
        when(userInformationHelper.usuariFindByUsername("user1")).thenReturn(u2);
        when(alarmaRepository.findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndDataActivacioAfterAndDataEnviamentIsNull(eq("user1"), any())).thenReturn(Collections.singletonList(alarma));
        when(alarmaRepository.findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(eq("user1"), any())).thenReturn(Collections.emptyList());
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        long count = alarmaMailHelper.sendAlarmesAgrupades();

        assertThat(count).isEqualTo(2);
        verify(mailHelper, times(2)).sendSimple(anyString(), anyString(), anyString(), anyString(), contains("Resum diari"), anyString());
    }

    @Test
    @DisplayName("No envia correus agrupats si no hi ha alarmes pendents")
    void sendAlarmesAgrupades_quanNoHiHaPendents_noEnvia() throws MessagingException, UnsupportedEncodingException {
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(any())).thenReturn(Collections.emptyList());
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(any())).thenReturn(Collections.emptyList());
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByDataActivacioAfterAndDataEnviamentIsNull(any())).thenReturn(Collections.emptyList());
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(any())).thenReturn(Collections.emptyList());

        long count = alarmaMailHelper.sendAlarmesAgrupades();

        assertThat(count).isZero();
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Envia correu d'alarma finalitzada amb subject correcte")
    void sendAlarmaGeneric_quanFinalitzada_subjectFinalitzada() throws MessagingException, UnsupportedEncodingException {
        alarma.setDataFinalitzacio(LocalDateTime.now());
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        alarmaMailHelper.sendAlarmaGeneric(alarma, AlarmaMailEventType.RECUPERACIO);

        verify(mailHelper).sendSimple(eq("from@caib.es"), eq("Comanda"), eq("admin@caib.es"), anyString(), eq("[COMANDA] Alarma finalitzada: Test Alarma"), anyString());
    }

    @Test
    @DisplayName("No envia correu d'alarma si l'usuari és el de httpauth o stats")
    void sendAlarmaUser_quanUsuariAuth_noEnvia() throws MessagingException, UnsupportedEncodingException {
        ReflectionTestUtils.setField(alarmaMailHelper, "httpAuthUsername", "httpuser");
        ReflectionTestUtils.setField(alarmaMailHelper, "statsAuthUsername", "statsuser");

        config.setAdmin(false);
        Usuari usuariHttp = Usuari.builder().codi("httpuser").alarmaMail(true).build();
        Usuari usuariStats = Usuari.builder().codi("statsuser").alarmaMail(true).build();

        when(userInformationHelper.usuariFindByUsername("httpuser")).thenReturn(usuariHttp);
        config.setCreatedBy("httpuser");
        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), eq("httpuser"), anyString(), anyString(), anyString());

        when(userInformationHelper.usuariFindByUsername("statsuser")).thenReturn(usuariStats);
        config.setCreatedBy("statsuser");
        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), eq("statsuser"), anyString(), anyString(), anyString());

        ReflectionTestUtils.setField(alarmaMailHelper, "httpAuthUsername", null);
        ReflectionTestUtils.setField(alarmaMailHelper, "statsAuthUsername", null);
    }

    @Test
    @DisplayName("Gestiona excepció en enviament sense petar")
    void sendAlarmaGeneric_quanExcepcio_noPeta() throws MessagingException, UnsupportedEncodingException {
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(new MessagingException("Error test"));

        assertThatCode(() -> alarmaMailHelper.sendAlarmaGeneric(alarma, AlarmaMailEventType.ACTIVACIO)).doesNotThrowAnyException();

        verify(mailHelper).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    // ========================================================================
    // TESTOS ADDICIONALS PER A COBERTURA COMPLETA (>90%)
    // ========================================================================

    @Test
    @DisplayName("sendAlarmaUser (Admin): fa fallback al creador quan falla la consulta LDAP d'administradors")
    void sendAlarmaUser_quanAdminIldapFalla_llavorsUsaCreatedBy() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        config.setAdmin(true);
        config.setCreatedBy("fallbackCreator");

        Usuari fallbackUser = Usuari.builder().codi("fallbackCreator").nom("Fallback").email("fallback@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();

        when(userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN)).thenThrow(new UserInformationHelper.UserInformationException("LDAP down", new String[]{}, new Exception()));
        when(userInformationHelper.usuariFindByUsername("fallbackCreator")).thenReturn(fallbackUser);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        // Assert
        verify(userInformationHelper, times(2)).usuariFindByUsername("fallbackCreator");
        verify(mailHelper, times(1)).sendSimple(anyString(), anyString(), eq("fallback@caib.es"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendAlarmaMailToUserWithProfileCheck: no envia si l'usuari no existeix (null)")
    void sendAlarmaMailToUserWithProfileCheck_quanUsuariNull_llavorsNoEnvia() throws Exception {
        // Arrange
        config.setAdmin(false);
        when(userInformationHelper.usuariFindByUsername(alarma.getAlarmaConfig().getCreatedBy())).thenReturn(null);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        // Assert
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendAlarmaMailToUserWithProfileCheck: no envia si alarmaMail és false")
    void sendAlarmaMailToUserWithProfileCheck_quanAlarmaMailFalse_llavorsNoEnvia() throws Exception {
        // Arrange
        config.setAdmin(false);
        Usuari usuari = Usuari.builder().codi("creator").nom("Creator").email("c@caib.es").alarmaMail(false).alarmaMailAgrupar(false).build();
        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        // Assert
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendAlarmaMailForUser: gestiona correctament quan sendSimple retorna false (amb email autogenerat)")
    void sendAlarmaMailForUser_quanSendSimpleRetornaFalseIEmailAutogenerat_llavorsRegistraMotiuCorrecte() throws Exception {
        // Arrange
        config.setAdmin(false);
        Usuari usuari = Usuari.builder().codi("creator").nom("Creator").email(null).emailAlternatiu(null).alarmaMail(true).alarmaMailAgrupar(false).build();

        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);
        when(parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_DEFAULT_DOMAIN, "caib.es")).thenReturn("test.es");
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma, AlarmaMailEventType.ACTIVACIO);

        // Assert
        verify(mailHelper).sendSimple(anyString(), anyString(), eq("creator@test.es"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendAlarmesAgrupades: fa fallback al creador quan falla LDAP per a agrupats")
    void sendAlarmesAgrupades_quanLdapFallaPerAgrupats_llavorsUsaCreatedBy() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        config.setAdmin(true);
        config.setCreatedBy("adminCreator");
        Usuari creator = Usuari.builder().codi("adminCreator").nom("Creator").email("creator@caib.es").alarmaMail(true).alarmaMailAgrupar(true).build();

        when(userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN)).thenThrow(new UserInformationHelper.UserInformationException("LDAP error", new String[]{}, new Exception()));
        when(userInformationHelper.usuariFindByUsername("adminCreator")).thenReturn(creator);
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(any())).thenReturn(Collections.singletonList(alarma));
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(any())).thenReturn(Collections.emptyList());
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByDataActivacioAfterAndDataEnviamentIsNull(any())).thenReturn(Collections.emptyList());
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(any())).thenReturn(Collections.emptyList());
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        long count = alarmaMailHelper.sendAlarmesAgrupades();

        // Assert
        assertThat(count).isEqualTo(1);
        verify(userInformationHelper, times(3)).usuariFindByUsername("adminCreator");
        verify(mailHelper, times(1)).sendSimple(anyString(), anyString(), eq("creator@caib.es"), anyString(), contains("Resum diari"), anyString());
    }

    @Test
    @DisplayName("sendAlarmaGroupedMailForUser: retorna false immediatament si la llista d'alarmes és buida")
    void sendAlarmaGroupedMailForUser_quanLlistaBuida_llavorsRetornaFalse() throws MessagingException, UnsupportedEncodingException {
        // Arrange & Act
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(alarmaMailHelper, "sendAlarmaGroupedMailForUser", Collections.emptyList(), "user1");

        // Assert
        assertThat(result).isFalse();
        verify(userInformationHelper, never()).usuariFindByUsername(anyString());
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendAlarmaGroupedMailForUser: retorna false si l'usuari no té el perfil agrupat actiu")
    void sendAlarmaGroupedMailForUser_quanPerfilNoAgrupat_llavorsRetornaFalse() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        Usuari usuari = Usuari.builder().codi("user1").nom("U1").email("u1@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();
        when(userInformationHelper.usuariFindByUsername("user1")).thenReturn(usuari);

        // Act
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(alarmaMailHelper, "sendAlarmaGroupedMailForUser", Collections.singletonList(alarma), "user1");

        // Assert
        assertThat(result).isFalse();
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("mergeAlarmes: elimina duplicats mantenint l'ordre d'inserció")
    void mergeAlarmes_quanHiHaDuplicats_llavorsRetornaLlistaSenseDuplicats() {
        // Arrange
        AlarmaEntity a1 = new AlarmaEntity(); a1.setId(1L);
        AlarmaEntity a2 = new AlarmaEntity(); a2.setId(2L);
        AlarmaEntity a1Dup = new AlarmaEntity(); a1Dup.setId(1L); // Duplicat

        // Act
        @SuppressWarnings("unchecked")
        List<AlarmaEntity> result = (List<AlarmaEntity>) ReflectionTestUtils.invokeMethod(alarmaMailHelper, "mergeAlarmes", List.of(a1, a2), List.of(a1Dup));

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("mergeUsuaris: elimina duplicats de noms d'usuari")
    void mergeUsuaris_quanHiHaDuplicats_llavorsRetornaLlistaSenseDuplicats() {
        // Arrange
        List<String> llista1 = List.of("user1", "user2");
        List<String> llista2 = List.of("user2", "user3");

        // Act
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) ReflectionTestUtils.invokeMethod(alarmaMailHelper, "mergeUsuaris", llista1, llista2);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("user1", "user2", "user3");
    }
}
