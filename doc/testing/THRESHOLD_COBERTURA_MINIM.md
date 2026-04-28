# Threshold de Cobertura Mínim - COMANDA

## Objectiu
Establir els mínims de cobertura de tests que s'han de complir per mantenir qualitat i evitar regressions.

## Abast
- Backend Java (Maven + JaCoCo)
- Frontend React/TypeScript (Vitest coverage)

## Llindars mínims obligatoris

### 1) Backend Java (per mòdul)
- `LINE`: **70%** mínim
- `BRANCH`: **60%** mínim

### 2) Frontend (comanda-ms-visualitzacio)
- `Lines`: **70%** mínim
- `Branches`: **60%** mínim
- `Functions`: **70%** mínim
- `Statements`: **70%** mínim

### 3) Codi nou o modificat (tots els mòduls)
- Cobertura mínima en codi nou/modificat: **80%** línies
- Cap fitxer nou de lògica de negoci ha d'entrar amb cobertura `0%`

## Regles d'aplicació a PR
- Si una PR baixa la cobertura global del mòdul per davall del llindar, la PR no s'ha d'acceptar.
- Si la cobertura global del mòdul ja és inferior al llindar, la PR no pot empitjorar-la.
- Les excepcions han d'estar justificades al text de la PR i aprovades per revisió tècnica.

## Exclusions permeses (amb criteri)
Es pot excloure de cobertura:
- Codi generat automàticament (OpenAPI, clients generats).
- DTOs/POJOs sense lògica.
- Configuració pura (`config`) sense comportament de negoci.

No s'ha d'excloure:
- Serveis, helpers, validacions, controladors amb lògica i mappers no trivials.

## Pla d'adopció
- Fase actual: aplicar llindars de PR immediatament per codi nou/modificat.
- Fase progressiva: elevar mòduls crítics cap a `LINE 80%` i `BRANCH 70%`.
- Revisió trimestral: ajustar thresholds segons maduresa i risc dels mòduls.

## Mesura i eines
- Backend: JaCoCo (`mvn test` + report JaCoCo).
- Frontend: `npm run test:coverage` (Vitest).
- Publicar report de cobertura a CI per cada PR.

## Referències
- `doc/testing/CONVENCIONS_TESTING.md`
- `doc/testing/GUIA_ONBOARDING_TESTING.md`
