package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UOEstatEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnitatOrganitzativaHelper {

    private final UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;
    private final UnitatOrganitzativaRepository unitatOrganitzativaRepository;

    public UnitatOrganitzativaEntity updateByCodi(String codi) throws SistemaExternException {
        UnitatOrganitzativaEntity uo = unitatsOrganitzativesPlugin.findUnidad(codi);
        if (uo.getCodiConselleria() != null && !unitatOrganitzativaRepository.existsByCodi(uo.getCodiConselleria()))
            this.updateByCodi(uo.getCodiConselleria());
        return this.update(uo);
    }

    public UnitatOrganitzativaEntity update(UnitatOrganitzativaEntity uo) {
        Optional<UnitatOrganitzativaEntity> uoExists = unitatOrganitzativaRepository.findByCodi(uo.getCodi());

        if (uoExists.isPresent()) {
            UnitatOrganitzativaEntity u = uoExists.get();
            uoExists.get().update(uo);
            return unitatOrganitzativaRepository.save(u);
        }
        return unitatOrganitzativaRepository.save(uo);
    }

    @Transactional
    public List<UnitatOrganitzativaEntity> updateAll(List<UnitatOrganitzativaEntity> unitats) {
        if (unitats == null || unitats.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> codis = unitats.stream()
            .map(UnitatOrganitzativaEntity::getCodi)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        List<UnitatOrganitzativaEntity> existingList = unitatOrganitzativaRepository.findByCodiIn(codis);
        Map<String, UnitatOrganitzativaEntity> existingMap = existingList.stream()
            .collect(Collectors.toMap(UnitatOrganitzativaEntity::getCodi, Function.identity()));

        List<UnitatOrganitzativaEntity> result = new ArrayList<>(unitats.size());

        for (UnitatOrganitzativaEntity input : unitats) {
            UnitatOrganitzativaEntity target = existingMap.get(input.getCodi());

            if (target != null) {
                target.update(input);
                result.add(target);
            } else {
                if (input.getEstat() == null)
                    input.setEstat(UOEstatEnum.T);

                result.add(input);
            }
        }

        return unitatOrganitzativaRepository.saveAll(result);
    }
}
