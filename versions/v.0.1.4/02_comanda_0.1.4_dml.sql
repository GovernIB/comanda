-- Changeset db/changelog/changes/acl/0.1.4/0.1.4_acl_001.yaml::acl-salut-perm-migration-1::Limit
-- Migrar permisos de Salut de READ (mask 1) a PERM2 (mask 128) per a App i EntornApp
UPDATE com_acl_entry SET mask = 128 WHERE mask = 1
                                      AND acl_object_identity IN (
        SELECT oi.id
        FROM com_acl_object_identity oi
                 JOIN com_acl_class c ON oi.object_id_class = c.id
        WHERE c.class IN (
                          'es.caib.comanda.client.model.App',
                          'es.caib.comanda.client.model.EntornApp'
            )
    );

-- Changeset db/changelog/changes/est/0.1.4/0.1.4_est_001.yaml::plugin-uo-dir3-2::Limit
-- Afegir paràmetre del plugin d'unitats organitzatives dir3
INSERT INTO com_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, VALOR, EDITABLE) VALUES ('Plugins', 'Unitats Organitzatives', 'TEXT', 'es.caib.comanda.plugin.unitats.organitzatives.dir3.service.url', 'Url', '', 0);

INSERT INTO com_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, VALOR, EDITABLE) VALUES ('Plugins', 'Unitats Organitzatives', 'TEXT', 'es.caib.comanda.plugin.unitats.organitzatives.dir3.service.username', 'Usuari', '', 0);

INSERT INTO com_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, VALOR, EDITABLE) VALUES ('Plugins', 'Unitats Organitzatives', 'PASSWORD', 'es.caib.comanda.plugin.unitats.organitzatives.dir3.service.password', 'Contrasenya', '', 0);

-- Changeset db/changelog/changes/est/0.1.4/0.1.4_est_001.yaml::plugin-uo-dir3-5::Limit
INSERT INTO com_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, VALOR, EDITABLE) VALUES ('Plugins', 'Unitats Organitzatives', 'TEXT', 'es.caib.comanda.estadistica.dir3.govern.codi.arrel', 'Codi unitat arrel', '', 0);

-- Changeset db/changelog/changes/est/0.1.4/0.1.4_est_009.yaml::est-dashboard-titol-vores-costat-0.1.4-002::limit
-- Migrar les dades de la vora única anterior (mostrar_vora/color_vora/ample_vora) cap als 4 costats nous: l'antiga vora "completa" (mostrar_vora=true) es reparteix a top/right/left, i la vora inferior (que abans es mostrava sempre per defecte al frontend) es formalitza a mostrar_vora_bottom.
UPDATE com_est_dashboard_titol SET mostrar_vora_top = COALESCE(mostrar_vora, 0),
                                   color_vora_top = COALESCE(color_vora, color_vora_top),
                                   ample_vora_top = COALESCE(ample_vora, ample_vora_top),
                                   mostrar_vora_right = COALESCE(mostrar_vora, 0),
                                   color_vora_right = COALESCE(color_vora, color_vora_right),
                                   ample_vora_right = COALESCE(ample_vora, ample_vora_right),
                                   color_vora_bottom = COALESCE(color_vora, color_vora_bottom),
                                   ample_vora_bottom = COALESCE(ample_vora, ample_vora_bottom),
                                   mostrar_vora_left = COALESCE(mostrar_vora, 0),
                                   color_vora_left = COALESCE(color_vora, color_vora_left),
                                   ample_vora_left = COALESCE(ample_vora, ample_vora_left)
WHERE 1 = 1;

-- Changeset db/changelog/changes/est/0.1.4/0.1.4_est_015.yaml::est-dashboard-item-sync-entorn-id-0.1.4-001::limit
-- Sincronitzar el camp entorn_id dels ítems de dashboard (com_est_dashboard_item) amb l'entorn_id del seu dashboard pare (com_est_dashboard) per a tots aquells dashboards que tenen entorn definit.
UPDATE com_est_dashboard_item SET entorn_id = (
    SELECT d.entorn_id
    FROM com_est_dashboard d
    WHERE d.id = com_est_dashboard_item.dashboard_id
) WHERE dashboard_id IN (
    SELECT d.id
    FROM com_est_dashboard d
    WHERE d.entorn_id IS NOT NULL
);

-- Changeset db/changelog/changes/est/0.1.4/0.1.4_est_017.yaml::plugin-uo-dir3-codi-arrel-parametre-update-0.1.4-001::limit
-- Actualitzar paràmetre codi unitat arrel DIR3 per unificar la clau de configuració i fer-lo editable per BBDD
UPDATE com_PARAMETRE SET CODI = 'es.caib.comanda.plugin.unitats.organitzatives.dir3.govern.codi.arrel', EDITABLE = 1, VALOR = 'A04003003' WHERE CODI = 'es.caib.comanda.estadistica.dir3.govern.codi.arrel';
