-- Afegir paràmetre per activar o ocultar el dashboard de base de dades
INSERT INTO COM_PARAMETRE (GRUP,
                           SUBGRUP,
                           TIPUS,
                           CODI,
                           NOM,
                           DESCRIPCIO,
                           VALOR,
                           EDITABLE)
VALUES ('Monitor',
        'Base de dades',
        'BOOLEAN',
        'es.caib.comanda.monitor.db.actiu',
        'Monitor de base de dades actiu',
        'Indica si el dashboard de base de dades és accessible. Desactivar si no s''han concedit permisos a les vistes DBA.',
        'true',
        1);

-- Afegir paràmetre de retenció de l'històric d'entorn aplicació de configuració
INSERT INTO COM_PARAMETRE (GRUP,
                           SUBGRUP,
                           TIPUS,
                           CODI,
                           NOM,
                           DESCRIPCIO,
                           VALOR,
                           EDITABLE)
VALUES ('Configuracio',
        'Històric',
        'NUMERIC',
        'es.caib.comanda.entorn.app.hist.retencio.dies',
        'Dies de retenció de l''històric de versions d''entorn aplicació',
        'Nombre de dies que s''ha de conservar l''històric de canvis de versions d''entorn aplicació per entorn d''aplicació abans d''eliminar-lo automàticament.',
        '30',
        1);
