package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.*;
import es.caib.comanda.client.model.monitor.Monitor;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmaMailHelperTest {

    @Mock
    private AlarmaClientHelper alarmaClientHelper;
    @Mock
    private MailHelper mailHelper;
    @Mock
    private UserInformationHelper userInformationHelper;
    @Mock
    private AlarmaRepository alarmaRepository;
    @Mock
    private ParametresHelper parametresHelper;

    @InjectMocks
    private AlarmaMailHelper alarmaMailHelper;

    private AlarmaEntity alarma;
    private AlarmaConfigEntity config;
    private EntornApp entornApp;
    private App app;
    private Entorn entorn;

    @BeforeEach
    void setUp() {
        lenient().when(parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_FROM_ADDRESS, "comanda@caib.es"))
                .thenReturn("from@caib.es");
        lenient().when(parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_FROM_NAME, "Comanda"))
                .thenReturn("Comanda");

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

    // Helper intern per crear App ja que no té builder
    private static class EntornAppTestHelper {
        static App createApp(Long id, String nom) {
            App app = new App();
            ReflectionTestUtils.setField(app, "id", id);
            ReflectionTestUtils.setField(app, "nom", nom);
            return app;
        }
    }

    @Test
    @DisplayName("Envia correu d'alarma genèrica correctament")
    void sendAlarmaGeneric_quanTotCorrecte_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        // Act
        alarmaMailHelper.sendAlarmaGeneric(alarma);

        // Assert
        verify(mailHelper).sendSimple(
                eq("from@caib.es"), eq("Comanda"),
                eq("admin@caib.es"), anyString(),
                eq("[COMANDA] Alarma activada: Test Alarma"), anyString());
        verify(alarmaClientHelper).monitorCreate(any(Monitor.class));
    }

    @Test
    @DisplayName("Envia correu d'alarma genèrica correctament sense nom")
    void sendAlarmaGeneric_quanNoNom_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        config.setNom(null);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        // Act
        alarmaMailHelper.sendAlarmaGeneric(alarma);

        // Assert
        verify(mailHelper).sendSimple(
                eq("from@caib.es"), eq("Comanda"),
                eq("admin@caib.es"), anyString(),
                eq("[COMANDA] Alarma activada"), anyString());
    }

    @Test
    @DisplayName("Envia correu d'alarma a l'usuari creador")
    void sendAlarmaUser_quanUsuariNoAdmin_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        config.setAdmin(false);
        config.setNom("Test Alarma");
        Usuari usuari = Usuari.builder()
                .codi("creator")
                .nom("Creator Name")
                .email("creator@caib.es")
                .alarmaMail(true)
                .alarmaMailAgrupar(false)
                .build();

        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);
        // Per generateAlarmaBodyMessage:
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma);

        // Assert
        verify(mailHelper).sendSimple(
                eq("from@caib.es"), anyString(),
                eq("creator@caib.es"), eq("Creator Name"),
                eq("[COMANDA] Alarma activada: Test Alarma"), anyString());
        verify(alarmaClientHelper).monitorCreate(any(Monitor.class));
    }

    @Test
    @DisplayName("Envia correu d'alarma a l'usuari creador sense nom")
    void sendAlarmaUser_quanUsuariNoAdminNoNom_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        config.setAdmin(false);
        config.setNom(null);
        Usuari usuari = Usuari.builder()
                .codi("creator")
                .nom("Creator Name")
                .email("creator@caib.es")
                .alarmaMail(true)
                .alarmaMailAgrupar(false)
                .build();

        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);
        // Per generateAlarmaBodyMessage:
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma);

        // Assert
        verify(mailHelper).sendSimple(
                eq("from@caib.es"), anyString(),
                eq("creator@caib.es"), eq("Creator Name"),
                eq("[COMANDA] Alarma activada"), anyString());
    }

    @Test
    @DisplayName("Envia correu d'alarma a administradors")
    void sendAlarmaUser_quanAdmin_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        config.setAdmin(true);
        String[] admins = {"admin1", "admin2"};
        Usuari u1 = Usuari.builder().codi("admin1").nom("A1").email("a1@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();
        Usuari u2 = Usuari.builder().codi("admin2").nom("A2").email("a2@caib.es").alarmaMail(true).alarmaMailAgrupar(false).build();

        when(userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN)).thenReturn(admins);
        when(userInformationHelper.usuariFindByUsername("admin1")).thenReturn(u1);
        when(userInformationHelper.usuariFindByUsername("admin2")).thenReturn(u2);
        
        // Per generateAlarmaBodyMessage
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma);

        // Assert
        verify(mailHelper, times(2)).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(alarmaClientHelper, times(2)).monitorCreate(any(Monitor.class));
    }

    @Test
    @DisplayName("No envia correu genèric si no hi ha email configurat")
    void sendAlarmaGeneric_quanNoEmail_noEnvia() {
        // Arrange
        entornApp.setAlarmesEmail(null);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);

        // Act
        alarmaMailHelper.sendAlarmaGeneric(alarma);

        // Assert
        verifyNoInteractions(mailHelper);
    }

    @Test
    @DisplayName("Usa email alternatiu si està informat")
    void sendAlarmaUser_quanEmailAlternatiu_usaAlternatiu() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        config.setAdmin(false);
        Usuari usuari = Usuari.builder()
                .codi("creator")
                .nom("Creator")
                .email("original@caib.es")
                .emailAlternatiu("alternatiu@caib.es")
                .alarmaMail(true)
                .alarmaMailAgrupar(false)
                .build();

        when(userInformationHelper.usuariFindByUsername("creator")).thenReturn(usuari);
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);

        // Act
        alarmaMailHelper.sendAlarmaUser(alarma);

        // Assert
        verify(mailHelper).sendSimple(anyString(), anyString(), eq("alternatiu@caib.es"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Envia alarmes agrupades correctament")
    void sendAlarmesAgrupades_enviamentOk() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String[] admins = {"admin1"};
        Usuari u1 = Usuari.builder().codi("admin1").nom("A1").email("a1@caib.es").alarmaMail(true).alarmaMailAgrupar(true).build();
        
        when(userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN)).thenReturn(admins);
        when(userInformationHelper.usuariFindByUsername("admin1")).thenReturn(u1);
        
        List<AlarmaEntity> alarmesAdmin = Collections.singletonList(alarma);
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(any()))
                .thenReturn(alarmesAdmin);
        
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByDataActivacioAfterAndDataEnviamentIsNull(any()))
                .thenReturn(Collections.singletonList("user1"));
        Usuari u2 = Usuari.builder().codi("user1").nom("U1").email("u1@caib.es").alarmaMail(true).alarmaMailAgrupar(true).build();
        when(userInformationHelper.usuariFindByUsername("user1")).thenReturn(u2);
        
        List<AlarmaEntity> alarmesUser = Collections.singletonList(alarma);
        when(alarmaRepository.findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndDataActivacioAfterAndDataEnviamentIsNull(eq("user1"), any()))
                .thenReturn(alarmesUser);

        // Per generateAlarmaBodyMessage
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);

        // Configurem mailHelper per retornar true (èxit enviament)
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        // Act
        long count = alarmaMailHelper.sendAlarmesAgrupades();

        // Assert
        assertThat(count).isEqualTo(2);
        verify(mailHelper, times(2)).sendSimple(anyString(), anyString(), anyString(), anyString(), contains("Resum diari"), anyString());
    }

    @Test
    @DisplayName("No envia correus agrupats si no hi ha alarmes pendents")
    void sendAlarmesAgrupades_quanNoHiHaPendents_noEnvia() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        when(alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(any()))
                .thenReturn(Collections.emptyList());
        when(alarmaRepository.findDistinctAlarmaConfigCreatedByDataActivacioAfterAndDataEnviamentIsNull(any()))
                .thenReturn(Collections.singletonList("user1"));
        when(alarmaRepository.findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndDataActivacioAfterAndDataEnviamentIsNull(eq("user1"), any()))
                .thenReturn(Collections.emptyList());

        // Act
        long count = alarmaMailHelper.sendAlarmesAgrupades();

        // Assert
        assertThat(count).isZero();
        verify(mailHelper, never()).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(userInformationHelper, never()).findByRole(BaseConfig.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Gestiona excepció en enviament sense petar")
    void sendAlarmaGeneric_quanExcepcio_noPeta() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        when(alarmaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);
        when(alarmaClientHelper.appFindById(10L)).thenReturn(app);
        when(alarmaClientHelper.entornById(20L)).thenReturn(entorn);
        when(mailHelper.sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new MessagingException("Error test"));

        // Act & Assert (no hauria de llançar excepció cap a fora)
        alarmaMailHelper.sendAlarmaGeneric(alarma);
        
        verify(mailHelper).sendSimple(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
}
