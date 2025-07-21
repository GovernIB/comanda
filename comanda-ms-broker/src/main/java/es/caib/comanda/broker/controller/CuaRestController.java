package es.caib.comanda.broker.controller;

import es.caib.comanda.ms.broker.model.Avis;
import es.caib.comanda.ms.broker.model.Integracio;
import es.caib.comanda.ms.broker.model.Permis;
import es.caib.comanda.ms.broker.model.Tasca;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static es.caib.comanda.ms.broker.model.Cues.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cues")
public class CuaRestController {

    private final JmsTemplate jmsTemplate;

    @PostMapping("/tasques")
    public ResponseEntity<String> sendToTasquesQueue(@RequestBody Tasca tasca) {
        jmsTemplate.convertAndSend(CUA_TASQUES, tasca);
        return ResponseEntity.ok("Missatge enviat a " + CUA_TASQUES);
    }

    @PostMapping("/avisos")
    public ResponseEntity<String> sendToAvisosQueue(@RequestBody Avis avis) {
        jmsTemplate.convertAndSend(CUA_AVISOS, avis);
        return ResponseEntity.ok("Missatge enviat a " + CUA_AVISOS);
    }

    @PostMapping("/permisos")
    public ResponseEntity<String> sendToPermisosQueue(@RequestBody Permis permis) {
        jmsTemplate.convertAndSend(CUA_PERMISOS, permis);
        return ResponseEntity.ok("Missatge enviat a " + CUA_PERMISOS);
    }

    @PostMapping("/integracions")
    public ResponseEntity<String> sendToIntegracionsQueue(@RequestBody Integracio integracio) {
        jmsTemplate.convertAndSend(CUA_INTEGRACIONS, integracio);
        return ResponseEntity.ok("Missatge enviat a " + CUA_INTEGRACIONS);
    }

}
