/**
 *
 */
package es.caib.comanda.estadistica.logic.dir3;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UOEstatEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementació de proves del plugin d'unitats organitzatives.
 *
 * @author Limit Tecnologies <limit@limit.es>
 */
@Service
@RequiredArgsConstructor
public class UnitatsOrganitzativesPluginDir3 implements UnitatsOrganitzativesPlugin {

    private final UnitatsOrganitzativesRestClient unitatsOrganitzativesRestClient;

    @Override
    public UnitatOrganitzativaEntity findUnidad(String codi) throws SistemaExternException {
        return this.findUnidad(codi, null, null);
    }

//    @Override
    public UnitatOrganitzativaEntity findUnidad(String codi, String fechaActualizacion, String fechaSincronizacion) throws SistemaExternException {
        try {
            UnidadRest unidad = unitatsOrganitzativesRestClient.obtenerUnidad(
                codi, fechaActualizacion, fechaSincronizacion, false);
            if (unidad != null) {
                return toUnitatOrganitzativa(unidad);
            } else {
                throw new SistemaExternException("La unitat organitzativa no està vigent (" + "codi=" + codi + ")");
            }
        } catch (SistemaExternException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UnitatOrganitzativaEntity toUnitatOrganitzativa(UnidadRest unidad) {
        UnitatOrganitzativaEntity unitat = UnitatOrganitzativaEntity.builder()
                .codi(unidad.getCodigo())
                .denominacioEs(unidad.getDenominacion())
                .denominacioCa(unidad.getDenominacionCooficial())
                .nifCif(unidad.getCodigo())
                .estat(UOEstatEnum.fromValue(unidad.getCodigoEstadoEntidad()))
                .codiUnitatSuperior(unidad.getCodUnidadSuperior())
                .codiUnitatArrel(unidad.getCodUnidadRaiz())
                .build();
        return unitat;
    }

}
