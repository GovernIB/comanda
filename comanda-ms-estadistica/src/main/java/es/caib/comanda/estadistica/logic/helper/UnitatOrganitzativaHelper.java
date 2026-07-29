package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnitatOrganitzativaHelper {

    private final UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;
    private final UnitatOrganitzativaRepository unitatOrganitzativaRepository;

    public UnitatOrganitzativaEntity updateByCodi(String codi) throws SistemaExternException {
        UnitatOrganitzativaEntity uo = unitatsOrganitzativesPlugin.findUnidad(codi);
        if (uo.getCodiConselleria() != null) this.updateByCodi(uo.getCodiConselleria());
        return this.update(uo);
    }

    public UnitatOrganitzativaEntity update(UnitatOrganitzativaEntity uo) {
        Optional<UnitatOrganitzativaEntity> uoExists = unitatOrganitzativaRepository.findByCodi(uo.getCodi());

        if (uoExists.isPresent()) {
            UnitatOrganitzativaEntity u = uoExists.get();
            u.setDenominacioEs(uo.getDenominacioEs());
            u.setDenominacioCa(uo.getDenominacioCa());
            u.setNifCif(uo.getNifCif());
            u.setCodiUnitatArrel(uo.getCodiUnitatArrel());
            u.setCodiUnitatSuperior(uo.getCodiUnitatSuperior());
            u.setCodiConselleria(uo.getCodiConselleria());
            u.setEstat(uo.getEstat());
            return unitatOrganitzativaRepository.save(u);
        }
        return unitatOrganitzativaRepository.save(uo);
    }
}
