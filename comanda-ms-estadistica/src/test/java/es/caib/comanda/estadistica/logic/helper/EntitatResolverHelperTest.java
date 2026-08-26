package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesRestClient;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.EntitatValorTipus;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.EntitatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EntitatResolverHelper")
class EntitatResolverHelperTest {

    @Mock private DimensioRepository dimensioRepository;
    @Mock private DimensioValorRepository dimensioValorRepository;
    @Mock private EntitatRepository entitatRepository;
    @Mock private UnitatsOrganitzativesRestClient unitatsOrganitzativesRestClient;
    @Mock private UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;
    @Mock private UnitatOrganitzativaHelper unitatOrganitzativaHelper;

    @InjectMocks
    private EntitatResolverHelper entitatResolverHelper;

    private DimensioEntity dimensio(EntitatValorTipus tipus) {
        DimensioEntity d = new DimensioEntity();
        d.setCodi("ENT");
        d.setEntitatValorTipus(tipus);
        return d;
    }

    // ========================================================================
    // 1. TESTOS PER A resolveEntitat
    // ========================================================================

    @Test
    @DisplayName("resolveEntitat: retorna buit quan la dimensió és null")
    void resolveEntitat_quanDimensioEsNull_llavorsRetornaBuit() {
        assertThat(entitatResolverHelper.resolveEntitat(null, "A01")).isEmpty();
    }

    @Test
    @DisplayName("resolveEntitat: retorna buit quan el valor és null o en blanc")
    void resolveEntitat_quanValorEsBlanc_llavorsRetornaBuit() {
        DimensioEntity d = dimensio(EntitatValorTipus.CODI);
        assertThat(entitatResolverHelper.resolveEntitat(d, null)).isEmpty();
        assertThat(entitatResolverHelper.resolveEntitat(d, "  ")).isEmpty();
    }

    @Test
    @DisplayName("resolveEntitat: cerca per Entitat.codi quan el tipus és CODI")
    void resolveEntitat_quanTipusCodi_llavorsCercaPerCodi() {
        DimensioEntity d = dimensio(EntitatValorTipus.CODI);
        EntitatEntity e = new EntitatEntity();
        when(dimensioValorRepository.findByDimensioAndValor(d, "A01")).thenReturn(Optional.empty());
        when(entitatRepository.findByCodi("A01")).thenReturn(Optional.of(e));

        assertThat(entitatResolverHelper.resolveEntitat(d, "A01")).contains(e);
    }

    @Test
    @DisplayName("resolveEntitat: cerca per Entitat.codi quan el tipus és null (per defecte)")
    void resolveEntitat_quanTipusEsNull_llavorsCercaPerCodiPerDefecte() {
        DimensioEntity d = dimensio(null);
        EntitatEntity e = new EntitatEntity();
        when(dimensioValorRepository.findByDimensioAndValor(d, "A01")).thenReturn(Optional.empty());
        when(entitatRepository.findByCodi("A01")).thenReturn(Optional.of(e));

        assertThat(entitatResolverHelper.resolveEntitat(d, "A01")).contains(e);
    }

    @Test
    @DisplayName("resolveEntitat: cerca per Entitat.codiDir3 quan el tipus és CODI_DIR3")
    void resolveEntitat_quanTipusCodiDir3_llavorsCercaPerCodiDir3() {
        DimensioEntity d = dimensio(EntitatValorTipus.CODI_DIR3);
        EntitatEntity e = new EntitatEntity();
        when(dimensioValorRepository.findByDimensioAndValor(d, "A04001")).thenReturn(Optional.empty());
        when(entitatRepository.findByCodiDir3("A04001")).thenReturn(Optional.of(e));

        assertThat(entitatResolverHelper.resolveEntitat(d, "A04001")).contains(e);
        verify(entitatRepository, never()).findByCodi(anyString());
    }

    @Test
    @DisplayName("resolveEntitat: cerca per Entitat.nom quan el tipus és NOM")
    void resolveEntitat_quanTipusNom_llavorsCercaPerNom() {
        DimensioEntity d = dimensio(EntitatValorTipus.NOM);
        EntitatEntity e = new EntitatEntity();
        when(dimensioValorRepository.findByDimensioAndValor(d, "Ajuntament de Palma")).thenReturn(Optional.empty());
        when(entitatRepository.findFirstByNom("Ajuntament de Palma")).thenReturn(Optional.of(e));

        assertThat(entitatResolverHelper.resolveEntitat(d, "Ajuntament de Palma")).contains(e);
    }

    @Test
    @DisplayName("resolveEntitat: cerca per Entitat.cif quan el tipus és CIF")
    void resolveEntitat_quanTipusCif_llavorsCercaPerCif() {
        DimensioEntity d = dimensio(EntitatValorTipus.CIF);
        EntitatEntity e = new EntitatEntity();
        when(dimensioValorRepository.findByDimensioAndValor(d, "Q0700077E")).thenReturn(Optional.empty());
        when(entitatRepository.findByCif("Q0700077E")).thenReturn(Optional.of(e));

        assertThat(entitatResolverHelper.resolveEntitat(d, "Q0700077E")).contains(e);
        verify(entitatRepository, never()).findByCodi(anyString());
    }

    @Test
    @DisplayName("resolveEntitat: no intenta cap automatisme quan el tipus és MANUAL")
    void resolveEntitat_quanTipusManual_llavorsNoIntentaAutomatisme() {
        DimensioEntity d = dimensio(EntitatValorTipus.MANUAL);
        when(dimensioValorRepository.findByDimensioAndValor(d, "X")).thenReturn(Optional.empty());

        assertThat(entitatResolverHelper.resolveEntitat(d, "X")).isEmpty();
        verify(entitatRepository, never()).findByCodi(anyString());
        verify(entitatRepository, never()).findByCodiDir3(anyString());
        verify(entitatRepository, never()).findFirstByNom(anyString());
        verify(entitatRepository, never()).findByCif(anyString());
    }

    @Test
    @DisplayName("resolveEntitat: la sobreescriptura manual (entitatMapejada) té prioritat sobre qualsevol tipus")
    void resolveEntitat_quanHiHaEntitatMapejada_llavorsTePrioritat() {
        DimensioEntity d = dimensio(EntitatValorTipus.CODI);
        EntitatEntity mapejada = new EntitatEntity();
        DimensioValorEntity valor = new DimensioValorEntity();
        valor.setEntitatMapejada(mapejada);
        when(dimensioValorRepository.findByDimensioAndValor(d, "A01")).thenReturn(Optional.of(valor));

        assertThat(entitatResolverHelper.resolveEntitat(d, "A01")).contains(mapejada);
        verify(entitatRepository, never()).findByCodi(anyString());
    }

    // ========================================================================
    // 2. TESTOS PER A resolveOrCreateEntitat
    // ========================================================================

    @Test
    @DisplayName("resolveOrCreateEntitat: retorna l'entitat existent sense crear-ne cap altra")
    void resolveOrCreateEntitat_quanJaExisteix_llavorsNoCreaCap() {
        DimensioEntity d = dimensio(EntitatValorTipus.CODI);
        EntitatEntity existent = new EntitatEntity();
        when(dimensioValorRepository.findByDimensioAndValor(d, "A01")).thenReturn(Optional.empty());
        when(entitatRepository.findByCodi("A01")).thenReturn(Optional.of(existent));

        Optional<EntitatEntity> result = entitatResolverHelper.resolveOrCreateEntitat(d, "A01");

        assertThat(result).contains(existent);
        verify(entitatRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolveOrCreateEntitat: crea l'Entitat amb el camp codi quan el tipus és CODI")
    void resolveOrCreateEntitat_quanTipusCodiINoExisteix_llavorsCreaAmbCodi() {
        DimensioEntity d = dimensio(EntitatValorTipus.CODI);
        when(dimensioValorRepository.findByDimensioAndValor(d, "A01")).thenReturn(Optional.empty());
        when(entitatRepository.findByCodi("A01")).thenReturn(Optional.empty());
        when(entitatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<EntitatEntity> result = entitatResolverHelper.resolveOrCreateEntitat(d, "A01");

        ArgumentCaptor<EntitatEntity> captor = ArgumentCaptor.forClass(EntitatEntity.class);
        verify(entitatRepository).save(captor.capture());
        assertThat(captor.getValue().getCodi()).isEqualTo("A01");
        assertThat(captor.getValue().getNom()).isNull();
        assertThat(captor.getValue().getCodiDir3()).isNull();
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("resolveOrCreateEntitat: crea l'Entitat amb el camp codiDir3 quan el tipus és CODI_DIR3 i dispara la sincronització d'UO")
    void resolveOrCreateEntitat_quanTipusCodiDir3INoExisteix_llavorsCreaAmbCodiDir3ISincronitzaUO() {
        DimensioEntity d = dimensio(EntitatValorTipus.CODI_DIR3);
        when(dimensioValorRepository.findByDimensioAndValor(d, "A04001")).thenReturn(Optional.empty());
        when(entitatRepository.findByCodiDir3("A04001")).thenReturn(Optional.empty());
        when(entitatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        entitatResolverHelper.resolveOrCreateEntitat(d, "A04001");

        ArgumentCaptor<EntitatEntity> captor = ArgumentCaptor.forClass(EntitatEntity.class);
        verify(entitatRepository).save(captor.capture());
        assertThat(captor.getValue().getCodiDir3()).isEqualTo("A04001");
        verify(unitatOrganitzativaHelper).refreshFromEntitatCodiDir3("A04001");
    }

    @Test
    @DisplayName("resolveOrCreateEntitat: crea l'Entitat amb el camp nom quan el tipus és NOM")
    void resolveOrCreateEntitat_quanTipusNomINoExisteix_llavorsCreaAmbNom() {
        DimensioEntity d = dimensio(EntitatValorTipus.NOM);
        when(dimensioValorRepository.findByDimensioAndValor(d, "Ajuntament")).thenReturn(Optional.empty());
        when(entitatRepository.findFirstByNom("Ajuntament")).thenReturn(Optional.empty());
        when(entitatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        entitatResolverHelper.resolveOrCreateEntitat(d, "Ajuntament");

        ArgumentCaptor<EntitatEntity> captor = ArgumentCaptor.forClass(EntitatEntity.class);
        verify(entitatRepository).save(captor.capture());
        assertThat(captor.getValue().getNom()).isEqualTo("Ajuntament");
        verify(unitatOrganitzativaHelper, never()).refreshFromEntitatCodiDir3(anyString());
    }

    @Test
    @DisplayName("resolveOrCreateEntitat: crea l'Entitat amb el camp cif quan el tipus és CIF")
    void resolveOrCreateEntitat_quanTipusCifINoExisteix_llavorsCreaAmbCif() {
        DimensioEntity d = dimensio(EntitatValorTipus.CIF);
        when(dimensioValorRepository.findByDimensioAndValor(d, "Q0700077E")).thenReturn(Optional.empty());
        when(entitatRepository.findByCif("Q0700077E")).thenReturn(Optional.empty());
        when(entitatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        entitatResolverHelper.resolveOrCreateEntitat(d, "Q0700077E");

        ArgumentCaptor<EntitatEntity> captor = ArgumentCaptor.forClass(EntitatEntity.class);
        verify(entitatRepository).save(captor.capture());
        assertThat(captor.getValue().getCif()).isEqualTo("Q0700077E");
        verify(unitatOrganitzativaHelper, never()).refreshFromEntitatCodiDir3(anyString());
    }

    @Test
    @DisplayName("resolveOrCreateEntitat: no crea res quan el tipus és MANUAL")
    void resolveOrCreateEntitat_quanTipusManual_llavorsNoCreaRes() {
        DimensioEntity d = dimensio(EntitatValorTipus.MANUAL);
        when(dimensioValorRepository.findByDimensioAndValor(d, "X")).thenReturn(Optional.empty());

        Optional<EntitatEntity> result = entitatResolverHelper.resolveOrCreateEntitat(d, "X");

        assertThat(result).isEmpty();
        verify(entitatRepository, never()).save(any());
        verify(unitatOrganitzativaHelper, never()).refreshFromEntitatCodiDir3(any());
    }
}
