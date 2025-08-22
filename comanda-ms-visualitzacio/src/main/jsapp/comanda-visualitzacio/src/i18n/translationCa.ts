const translationCa = {
    enum: {
        appEstat: {
            UP: {
                title: "Actiu",
                tooltip: "L'aplicació funciona <bold>correctament</bold>. <br> La taxa de <italic>errors detectats</italic> és <underline>inferior al 5%</underline>."
            },
            WARN: {
                title: "Avís",
                tooltip: "La taxa <italic>errors detectats</italic> està <underline>entre el 5% i el 10%</underline>."
            },
            DOWN: {
                title: "Inactiu",
                tooltip: "<bold>S'han detectat errors</bold>. <br> La <italic>taxa de errors detectats</italic> és <underline>superior al 30%</underline>."
            },
            DEGRADED: {
                title: "Degradat",
                tooltip: "<bold>Hi ha errors puntuals</bold>. <br> La <italic>taxa d'errors detectats</italic> està <underline>entre el 10% i el 30%</underline>."
            },
            MAINTENANCE: {
                title: "Manteniment",
                tooltip: "L'aplicació <bold>no està disponible</bold> per <underline>tasques de manteniment</underline>."
            },
            UNKNOWN: {
                title: "Desconegut",
                tooltip: "<bold>No teniu informació</bold> sobre l'estat de l'aplicació."
            },
            ERROR: {
                title: "Erreur",
                tooltip: "L'aplicació <bold>no està disponible</bold> per <underline>errors greus en el funcionament</underline>."
            },
        }
    },
    menu: {
        salut: "Salut",
        estadistiques: "Estadístiques",
        tasca: "Tasques",
        avis: "Avisos",
        monitoritzacio: "Monitorització",
        monitor: "Monitor",
        cache: "Gestió de cachés",
        broker: "Gestor de cues",
        configuracio: "Configuració",
        app: "Aplicacions",
        entorn: "Entorns",
        versionsEntorn: "Versions per entorn",
        integracio: "Integracions",
        widget: "Widgets estadístics",
        dashboard: "Taulers de control",
        calendari: "Calendari",
        parametre: "Paràmetres",
    },
    page: {
        salut: {
            title: "Salut",
            refrescar: "Refrescar",
            filtrar: "Filtrar per aplicació/entorn",
            senseFiltres: "Sense filtres",
            nd: "N/D",
            refresh: {
                last: "Darrer refresc",
                next: "Proper refresc en",
            },
            apps: {
                column: {
                    group: "Aplicació / entorn",
                    estat: "Estat",
                    codi: "Codi",
                    nom: "Nom",
                    versio: "Versió",
                    revisio: "Revisió",
                    bd: "Base de dades",
                    latencia: "Latència",
                    integ: "Integracions",
                    subsis: "Subsistemes",
                    msgs: "Missatges",
                },
                detalls: "Detalls",
            },
            refreshperiod: {
                PT1M: "1 minut",
                PT5M: "5 minuts",
                PT10M: "10 minuts",
                PT30M: "30 minuts",
                PT1H: "1 hora",
            },
            timerange: {
                PT15M: "Darrers 15 minuts",
                PT1H: "Darrera hora",
                P1D: "Darrer dia",
                P7D: "Darrera setmana",
                P1M: "Darrer mes",
            },
            info: {
                title: "Informació",
                revisio: "Revisió",
                jdk: {
                    versio: "Versió JDK",
                },
                data: "Darrera actualització",
                bdEstat: "Base de dades",
                appLatencia: "Latència",
                missatges: "Missatges",
                detalls: "Detalls",
            },
            latencia: {
                title: "Latència",
                error: "Hi ha hagut un error al mostrar el gràfic",
            },
            estatLatencia: {
                title: "Estat i latència",
                noInfo: "No hi ha dades per mostrar",
            },
            integracions: {
                title: "Integracions",
                integracioUpCount: "Actives",
                integracioDownCount: "Inactives",
                integracioDesconegutCount: "Estat desconegut",
                noInfo: "No hi ha informació de integracions",
                column: {
                    codi: "Codi",
                    nom: "Nom",
                    estat: "Estat",
                    latencia: "Latència",
                    peticions: "Peticions",
                },
            },
            subsistemes: {
                title: "Subsistemes",
                subsistemaUpCount: "Actives",
                subsistemaDownCount: "Inactives",
                noInfo: "No hi ha informació de subsistemes",
                column: {
                    codi: "Codi",
                    nom: "Nom",
                    estat: "Estat",
                    latencia: "Latència",
                },
            },
            msgs: {
                missatgeErrorCount: "Errors",
                missatgeWarnCount: "Alertes",
                missatgeInfoCount: "Informació"
            },
            contexts: {
                title: "Contexts",
                noInfo: "No hi ha informació de contexts",
                column: {
                    codi: "Códi",
                    nom: "Nom",
                    path: "Ruta",
                    api: "Api",
                    manuals: "Manuals",
                },
            },
            detalls: {
                title: "Detalls",
                noInfo: "No hi ha informació sobre detalls de l'aplicació",
            },
            estats: {
                title: "Estats",
            },
        },
        dashboards: {
            title: "Taulers de control",
            edit: "Editar",
            dashboardView: "Anar al taulers de control",
            cloneDashboard: {
                title: "Clonar el tauler de control",
                success: "Tauler de controls clonat correctament",
            },
            components: {
                llistar: "Llistar components",
                afegir: "Afegir component",
            },
            action: {
                select: {
                    title: "Seleccioni el tauler a mostrar...",
                },
                llistarWidget: {
                    label: "Llistar widgets",
                    title: "Llistar widgets",
                },
                llistarTitle: {
                    label: "Llistar títols",
                    title: "Llistar títols",
                },
                patchItem: {
                    success: "Desat correctament",
                    warning: "No s'ha pogut trobar el newDashboardItem amb l'id {{id}}, l'actualització no es propagarà.",
                    error: "Error en desar",
                    saveError: "Error en desar",
                },
                addWidget: {
                    label: "Afegir widget",
                    title: "Afegir widget",
                    success: "Widget afegit correctament",
                    error: "Error en afegir el widget",
                },
                afegirTitle: {
                    label: "Afegir títol",
                    title: "Afegir títol",
                },
                export: "Exportar tauler",
            },
            alert: {
                tornarLlistat: "Tornar al llistat",
                tornarTauler: "Tornar al tauler per defecte",
                notExists: "El tauler de control no existeix.",
                notDefined: "No hi ha cap tauler de control definit.",
                carregar: "Error en carregar el tauler de control.",
            }
        },
        tasques: {
            filter: {
                more: "Més camps",
                finished: "Només finalitzades",
            },
            grid: {
                groupHeader: "Nom",
                action: {
                    obrir: "Obrir tasca",
                }
            }
        },
        avisos: {
            filter: {
                more: "Més camps",
                finished: "Només finalitzats",
            },
            grid: {
                groupHeader: "Nom",
            }
        },
        apps: {
            title: "Aplicacions",
            create: "Crear aplicació",
            update: "Modificar aplicació",
            general: "General",
            entornApp: "Entorns de l'aplicació",
            logo: "Logo",
            uploadLogo: "Pujar logo",
            changeLogo: "Canviar logo",
            removeLogo: "Eliminar logo",
            action: {
                export: "Exportar aplicació",
            },
        },
        entorns: {
            title: "Entorns",
        },
        appsEntorns: {
            title: "Entorns",
            resourceTitle: "entorn",
        },
        versionsEntorns: {
            title: "Versions per entorn",
        },
        integracions: {
            title: "Integracions",
            column: {
                codi: "Codi",
                nom: "Nom",
                logo: "Logo",
            },
        },
        monitors: {
            title: "Monitors",
            detail: {
                title: "Detalls de la comunicació amb la integració",
                data: "Data",
                operacio: "Descripció",
                tipus: "Tipus",
                estat: "Estat",
                codiUsuari: "Usuari",
                errorDescripcio: "Descripció de l'error",
                excepcioMessage: "Missatge de l'excepció",
                excepcioStacktrace: "Traça de l'excepció",
            },
            modulEnum: {
                salut: "Salut",
                estadistica: "Estadística",
                configuracio: "Configuració",
            },
        },
        widget: {
            title: "Widgets estadístics",
            grid: {
                position: "Posició",
                size: "Mida",
            },
            form: {
                periode: "Període",
                simple: "Widget simple",
                grafic: "Widget gràfic",
                taula: "Widget taula",
                preview: "Previsualització",
                configVisual: "Configuració visual",
                configGeneral: "Configuració general",
                configTaula: "Configuració taula",
                configFont: "Configuració de la mida de font",
                graficBar: "Gràfic de barres",
                graficLin: "Gràfic de línies",
                graficPst: "Gràfic de pastís",
                graficGug: "Gràfic de gauge",
                graficMap: "Gràfic de heatmap",
            },
            simple: {
                tab: {
                    title: "Simples",
                },
                title: "Widgets estadístics simples",
                resourceTitle: "widget simple",
            },
            grafic: {
                tab: {
                    title: "Gràfics",
                },
                title: "Widgets estadístics gràfics",
                resourceTitle: "widget gràfic",
                indicadors: "Indicadors",
            },
            taula: {
                tab: {
                    title: "Taula",
                },
                title: "Widgets estadístics tipus taula",
                resourceTitle: "widget tipus taula",
                tableCols: "Columnes de la taula",
                columna: {
                    indicador: "Indicador",
                    titolIndicador: "Títol",
                    tipusIndicador: "Tipus agrupació",
                    periodeIndicador: "Període agr.",
                    accions: "Accions",
                    arrossega: "Arrossega per reordenar",
                },
            },
            atributsVisuals: {
                colorText: "Color del text",
                colorFons: "Color de fons",
                icona: "Icona",
                colorIcona: "Color de la icona",
                colorFonsIcona: "Color de fons de la icona",
                colorTextDestacat: "Color del text destacat",
                mostrarVora: "Mostrar vora",
                colorVora: "Color de la vora",
                ampleVora: "Ample de la vora",
                midaFontTitol: "Mida de la font del títol",
                midaFontDescripcio: "Mida de la font de la descripció",
                midaFontValor: "Mida de la font del valor",
                midaFontUnitats: "Mida de la font de les unitats",
                midaFontCanviPercentual: "Mida de la font del canvi percentual",
                colorsPaleta: "Colors de la paleta",
                mostrarReticula: "Mostrar retícula",
                barStacked: "Barres apilades",
                barHorizontal: "Barres horitzontals",
                lineShowPoints: "Mostrar punts",
                area: "Emplenar area",
                lineSmooth: "Línies suaus",
                lineWidth: "Amplada de línia",
                outerRadius: "Radi exterior",
                pieDonut: "Tipus donut",
                innerRadius: "Radi interior",
                pieShowLabels: "Mostrar etiquetes",
                labelSize: "Mida etiquetes",
                gaugeMin: "Valor mínim",
                gaugeMax: "Valor màxim",
                gaugeColors: "Colors (separats per comes)",
                gaugeRangs: "Rangs (separats per comes)",
                heatmapMinValue: "Valor mínim",
                heatmapMaxValue: "Valor màxim",
                colorTextTaula: "Color de text de la taula",
                colorFonsTaula: "Color de fons de la taula",
                mostrarCapcalera: "Mostrar capçalera",
                colorCapcalera: "Color de text de la capçalera",
                colorFonsCapcalera: "Color de fons de la capçalera",
                mostrarAlternancia: "Mostrar alternança de files",
                colorAlternancia: "Color d'alternança",
                mostrarVoraTaula: "Mostrar vora de taula",
                colorVoraTaula: "Color de la vora",
                ampleVoraTaula: "Ample de la vora",
                mostrarSeparadorHoritzontal: "Mostrar separador horitzontal",
                colorSeparadorHoritzontal: "Color del separador",
                ampleSeparadorHoritzontal: "Ample del separador",
                mostrarSeparadorVertical: "Mostrar separador vertical",
                colorSeparadorVertical: "Color del separador",
                ampleSeparadorVertical: "Ample del separador",
            },
            editorPaleta: {
                title: "Editor de Paleta de Colors",
                color: "Color",
                hex: "Codi HEX",
                palet: "Paleta actual:",
                empty: "No hi ha colors a la paleta.",
                exist: "Aquest color ja existeix a la paleta!",
            },
            action: {
                add: {
                    label: "Afegir",
                },
                addColumn: {
                    label: "Afegir columna",
                }
            },
        },
        caches: {
            title: "Caches",
            buidar: {
                label: "Buidar",
                titol: "Buidar cache",
                confirm: "Estau segur que voleu buidar la cache?",
                success: "Cache buidada",
                error: "Error buidant cache",
                totes: {
                    titol: "Buidar totes les caches",
                    confirm: "Estau segur que voleu buidar totes les caches?",
                    success: "Caches buidades",
                    error: "Error buidant caches",
                },
            },
        },
        broker: {
            title: "Gestor de cues",
            error: {
                fetchFailed: "Error en obtenir les dades del broker",
            },
            detail: {
                title: "Informació del broker",
                version: "Versió",
                name: "Nom",
                status: "Estat",
                uptime: "Temps d'activitat",
                memoryUsage: "Ús de memòria",
                diskUsage: "Ús de disc",
                totalConnections: "Connexions totals",
                totalQueues: "Cues totals",
                totalMessages: "Missatges totals",
            },
            queues: {
                title: "Cues",
            },
            queue: {
                address: "Adreça",
                routingType: "Tipus d'enrutament",
                durable: "Durable",
                messageCount: "Nombre de missatges",
                consumerCount: "Nombre de consumidors",
                viewMessages: "Veure missatges",
            },
        },
        queue: {
            title: "Cua: {{name}}",
            error: {
                fetchFailed: "Error en obtenir les dades de la cua",
                purgeFailed: "Error en purgar la cua",
            },
            detail: {
                title: "Detalls de la cua",
                name: "Nom",
                address: "Adreça",
                routingType: "Tipus d'enrutament",
                durable: "Durable",
                messageCount: "Nombre de missatges",
                consumerCount: "Nombre de consumidors",
                deliveringCount: "Missatges en entrega",
                messagesAdded: "Missatges afegits",
                messagesAcknowledged: "Missatges confirmats",
                filter: "Filtre",
                temporary: "Temporal",
                autoCreated: "Creació automàtica",
                purgeOnNoConsumers: "Purgar sense consumidors",
                maxConsumers: "Màxim de consumidors",
            },
            actions: {
                purge: "Purgar cua",
            },
            purge: {
                title: "Purgar cua",
                confirmation: "Estau segur que voleu eliminar tots els missatges de la cua {{name}}?",
            },
        },
        message: {
            title: "Missatges",
            error: {
                deleteFailed: "Error en eliminar el missatge",
            },
            detail: {
                title: "Detalls del missatge",
                messageID: "ID del missatge",
                queueName: "Nom de la cua",
                timestamp: "Data i hora",
                type: "Tipus",
                durable: "Durable",
                priority: "Prioritat",
                size: "Mida",
                redelivered: "Reentregat",
                deliveryCount: "Nombre d'entregues",
                expirationTime: "Data d'expiració",
                properties: "Propietats",
                content: "Contingut",
            },
            grid: {
                messageID: "ID del missatge",
                timestamp: "Data i hora",
                type: "Tipus",
                priority: "Prioritat",
                size: "Mida",
                actions: "Accions",
            },
        },
        parametres: {
            title: "Paràmetres",
            detail: {
                title: "Detalls del paràmetre",
                grup: "Grup",
                subGrup: "Subgrup",
                tipus: "Tipus",
                codi: "Codi",
                nom: "Nom",
                descripcio: "Descripció",
                valor: "Valor",
            },
        },
        notFound: "No trobat",
    },
    generic: {
        tipus: "Tipus",
        periode: "Període",
    },
    errors: {
        camp: {
            obligatori: "Camp obligatori",
        },
    },
    components: {
        clear: "Netejar",
        search: "Cercar",
        copiarContingut: "Copiar contingut",
        copiarContingutTitle: "Copiar el contingut",
        copiarContingutSuccess: "Contingut copiar al portapapers",
    },
    calendari: {
        seleccionar_entorn_app: "Seleccionar entorn d'aplicació",
        seleccionar: "Seleccionar",
        seleccionar_entorn_app_primer: "Seleccioneu primer un entorn d'aplicació",
        carregar_interval: "Carregar dades per interval",
        carregar_mes_actual: "Carregar dades del mes actual",
        today: "Avui",
        data_inici: "Data inici",
        data_fi: "Data fi",
        cancelar: "Cancel·lar",
        carregar: "Carregar",
        sense_dades: "Sense dades estadístiques",
        dades_buides: "L'aplicació no disposa de dades estadístiques",
        dades_disponibles: "Dades disponibles",
        dades_disponibles_tooltip: "Clica per veure les dades disponibles",
        obtenir_dades: "Obtenir dades",
        obtenir_dades_tooltip: "Clica per obtenir les dades destadístiques del dia",
        error_dades: "Error en obtenir dades",
        error_dades_tooltip: "S'ha produït un error al intentar obtenir les dades estadístiques de l'aplicació",
        error_titol: "Error en obtenir dades estadístiques",
        data: "Data",
        missatge: "Missatge",
        traca: "Traça",
        tancar: "Tancar",
        success_obtenir_dades: "Les dades estadístiques s'han obtingut correctament",
        error_obtenir_dades: "Error en obtenir dades estadístiques",
        success_carregar_interval: "Les dades estadístiques s'han obtingut correctament",
        error_carregar_interval: "Error en obtenir dades estadístiques dades per interval",
        error_dades_disponibles: "Error al obtenir els dies amb dades estadístiques disponibles",
        error_dades_dia: "Error al obtenir les dades estadístiques del dia",
        error_obtenir_dates_mes: "No s'han pogut determinar les dates del mes actual",
        carregant: "Carregant",
        carregant_dades: "Carregant dades estadístiques...",
        modal_dades_dia: "Dades disponibles del dia",
        dimensions: "Dimensions",
        indicadors: "Indicadors",
    },
    treeData: {
        treeView: "Vista en arbre"
    },
};

export default translationCa;
