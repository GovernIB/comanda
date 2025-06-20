const translationCa = {
    menu: {
        configuracio: "Configuració",
        estadistiques: "Estadístiques",
        salut: "Salut",
        monitor: "Monitor",
        app: "Aplicacions",
        entorn: "Entorns",
        widget: "Widgets estadístics",
        dashboard: "Taulers de control",
        cache: "Gestió de cachés",
    },
    page: {
        salut: {
            title: "Salut",
            refrescar: "Refrescar",
            nd: "N/D",
            apps: {
                column: {
                    estat: "Estat",
                    codi: "Codi",
                    nom: "Nom",
                    versio: "Versió",
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
                data: "Darrera actualització",
                bdEstat: "Base de dades",
                appLatencia: "Latència",
                missatges: "Missatges",
                detalls: "Detalls",
            },
            latencia: {
                title: "Latència",
            },
            estatLatencia: {
                title: "Estat i latència",
            },
            integracions: {
                title: "Integracions",
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
                column: {
                    codi: "Codi",
                    nom: "Nom",
                    estat: "Estat",
                    latencia: "Latència",
                },
            },
            estats: {
                title: "Estats",
            },
        },
        apps: {
            title: "Aplicacions",
            create: "Crear aplicació",
            update: "Modificar aplicació",
            general: "General",
            entornApp: "Entorns de l'aplicació",
        },
        entorns: {
            title: "Entorns",
        },
        appsEntorns: {
            title: "Entorns",
            resourceTitle: "entorn",
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
            },
        },
        widget: {
            title: "Widgets estadístics",
            form: {
                periode: "Període",
                simple: "Widget simple",
                grafic: "Widget gràfic",
                taula: "Widget taula",
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
            },
            taula: {
                tab: {
                    title: "Taula",
                },
                title: "Widgets estadístics tipus taula",
                resourceTitle: "widget tipus taula",
                columna: {
                    indicador: "Indicador",
                    titolIndicador: "Títol",
                    tipusIndicador: "Tipus agrupació",
                    periodeIndicador: "Període agr.",
                    accions: "Accions",
                    arrossega: "Arrossega per reordenar",
                },
            },
        },
        dashboards: {
            title: "Taulers de control",
            edit: "Editar",
            dashboardView: "Anar al dashboard",
        },
        caches: {
            title: "Caches",
            columna: {
                codi: "Codi",
                nom: "Nom",
                entrades: "Núm. elements",
                mida: "Mida (bytes)",
            },
            buidar: {
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
        clear: "Netetjar",
        search: "Cercar",
        copiarContingut: "Copiar contingut",
        copiarContingutTitle: "Copiar el contingut",
        copiarContingutSuccess: "Contingut copiar al portapapers",
    },
};

export default translationCa;
