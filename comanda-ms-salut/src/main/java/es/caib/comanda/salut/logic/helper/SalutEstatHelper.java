package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.salut.logic.intf.model.SalutInformeAgrupacio;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static es.caib.comanda.salut.logic.helper.SalutInfoHelper.MINUTS_PER_AGRUPACIO;

/**
 * Lògica de càlcul de les sèries temporals d'estat de salut, reutilitzable
 * tant pel servei de consultes com pel listener de notificació SSE.
 */
@Component
@RequiredArgsConstructor
public class SalutEstatHelper {

    private final SalutRepository salutRepository;

    /**
     * Retorna els estatItems per a totes les agrupacions de l'entornApp indicat.
     * Claus del mapa: noms de SalutInformeAgrupacio (p.ex. "MINUT", "HORA", …).
     * Equivalent a {@code computeEstatItemsPerAgrupacio(entornAppId, true)}.
     */
    @Transactional(readOnly = true)
    public Map<String, List<SalutInformeEstatItem>> computeEstatItemsPerAgrupacio(Long entornAppId) {
        return computeEstatItemsPerAgrupacio(entornAppId, true);
    }

    /**
     * Retorna els estatItems per a les agrupacions de l'entornApp indicat.
     * Si {@code includeAll} és false, només es calcula l'agrupació MINUT (la més lleugera);
     * la resta s'omet per reduir la càrrega quan no hi ha clients SSE connectats.
     */
    @Transactional(readOnly = true)
    public Map<String, List<SalutInformeEstatItem>> computeEstatItemsPerAgrupacio(
            Long entornAppId, boolean includeAll) {
        Map<String, List<SalutInformeEstatItem>> result = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (SalutInformeAgrupacio agrupacio : SalutInformeAgrupacio.values()) {
            if (!includeAll && agrupacio != SalutInformeAgrupacio.MINUT) {
                continue;
            }
            TipusRegistreSalut tipus = mapTipusAgrupacio(agrupacio);
            LocalDateTime dataInici = getDataIniciAjustada(agrupacio, now);
            result.put(agrupacio.name(), generateEstatList(dataInici, tipus, entornAppId));
        }
        return result;
    }

    public TipusRegistreSalut mapTipusAgrupacio(SalutInformeAgrupacio agrupacio) {
        switch (agrupacio) {
            case MINUT:      return TipusRegistreSalut.MINUT;
            case MINUTS_HORA: return TipusRegistreSalut.MINUTS;
            case HORA:       return TipusRegistreSalut.HORA;
            case DIA_SETMANA:
            case DIA_MES:    return TipusRegistreSalut.DIA;
            default: throw new IllegalArgumentException("Agrupació desconeguda: " + agrupacio);
        }
    }

    public TemporalAmount getTemporalAmountAgrupacio(SalutInformeAgrupacio agrupacio) {
        switch (agrupacio) {
            case DIA_MES:    return Period.ofDays(30);
            case DIA_SETMANA: return Period.ofDays(7);
            case HORA:       return Period.ofDays(1);
            case MINUTS_HORA: return Duration.ofHours(1);
            case MINUT:
            default:         return Duration.ofMinutes(15);
        }
    }

    public LocalDateTime getDataIniciAjustada(SalutInformeAgrupacio agrupacio, LocalDateTime dataReferencia) {
        TemporalAmount temporalAmount = getTemporalAmountAgrupacio(agrupacio);
        LocalDateTime dataFi = dataReferencia.withSecond(0).withNano(0);
        switch (agrupacio) {
            case DIA_MES:
            case DIA_SETMANA:
                dataFi = dataFi.withHour(0).withMinute(0);
                break;
            case HORA:
                dataFi = dataFi.withMinute(0);
                break;
            case MINUTS_HORA:
                if (dataFi.getMinute() % MINUTS_PER_AGRUPACIO != 0) {
                    dataFi = dataFi.withMinute(dataFi.getMinute() - dataFi.getMinute() % MINUTS_PER_AGRUPACIO);
                }
                break;
            default:
                break;
        }
        return dataFi.minus(temporalAmount);
    }

    public List<LocalDateTime> generarGrupsDates(LocalDateTime dataInici, SalutInformeAgrupacio agrupacio) {
        List<LocalDateTime> result = new ArrayList<>();
        LocalDateTime data = dataInici;
        LocalDateTime dataFi = dataInici.plus(getTemporalAmountAgrupacio(agrupacio));
        while (data.isBefore(dataFi) || data.isEqual(dataFi)) {
            result.add(data);
            switch (agrupacio) {
                case DIA_MES:
                case DIA_SETMANA:
                    data = data.plusDays(1);
                    break;
                case HORA:
                    data = data.plusHours(1);
                    break;
                case MINUTS_HORA:
                    data = data.plusMinutes(MINUTS_PER_AGRUPACIO);
                    break;
                case MINUT:
                default:
                    data = data.plusMinutes(1);
                    break;
            }
        }
        return result;
    }

    public List<SalutInformeEstatItem> generateEstatList(
            LocalDateTime dataInici, TipusRegistreSalut tipus, Long entornAppId) {
        List<SalutEntity> entities = salutRepository
                .findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                        entornAppId, dataInici, tipus);
        List<SalutInformeEstatItem> result = entities.stream()
                .map(SalutInformeEstatItem::new)
                .collect(Collectors.toList());
        List<LocalDateTime> existingDates = result.stream()
                .map(SalutInformeEstatItem::getData)
                .collect(Collectors.toList());
        for (LocalDateTime data : generarFechas(dataInici, tipus)) {
            if (!existingDates.contains(data)) {
                result.add(new SalutInformeEstatItem(
                        data, 0, 0, 0, 0, 0, 0, 1, 1,
                        0, 0, 0, 0, 0, 0, 100));
            }
        }
        result.sort(Comparator.comparing(SalutInformeEstatItem::getData));
        return result;
    }

    private LocalDateTime plusTime(LocalDateTime data, long amount, TipusRegistreSalut tipus) {
        switch (tipus) {
            case HORA:   return data.plusHours(amount);
            case DIA:    return data.plusDays(amount);
            case MINUTS: return data.plusMinutes(amount * MINUTS_PER_AGRUPACIO);
            default:     return data.plusMinutes(amount);
        }
    }

    private List<LocalDateTime> generarFechas(LocalDateTime dataInici, TipusRegistreSalut tipus) {
        List<LocalDateTime> result = new ArrayList<>();
        LocalDateTime actual = dataInici;
        LocalDateTime ara = LocalDateTime.now();
        while (!actual.isAfter(ara)) {
            result.add(actual);
            actual = plusTime(actual, 1, tipus);
        }
        return result;
    }
}
