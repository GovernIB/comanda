const translationEn = {
    menu: {
        configuracio: "Configuration",
        estadistiques: "Statistics",
        salut: "Health",
        app: "Applications",
        entorn: "Environments",
        monitor: "Monitor",
        widget: "Statistic widgets",
        dashboard: "Dashboards",
        cache: "Cache management",
    },
    page: {
        salut: {
            title: "Health",
            refrescar: "Refresh",
            nd: "N/A",
            apps: {
                column: {
                    estat: "State",
                    codi: "Code",
                    nom: "Name",
                    versio: "Version",
                    bd: "Database",
                    latencia: "Latency",
                    integ: "Integrations",
                    subsis: "Subsystems",
                    msgs: "Messages",
                },
                detalls: "Details",
            },
            refreshperiod: {
                PT1M: "1 minute",
                PT10M: "10 minutes",
                PT30M: "30 minutes",
                PT1H: "1 hour",
            },
            timerange: {
                PT15M: "Last 15 minutes",
                PT1H: "Last hour",
                P1D: "Last day",
                P7D: "Last week",
                P1M: "Last month",
            },
            info: {
                title: "Information",
                data: "Last update",
                bdEstat: "Database",
                appLatencia: "Latency",
                missatges: "Messages",
                detalls: "Details",
            },
            latencia: {
                title: "Latency",
            },
            estatLatencia: {
                title: "State and latency",
            },
            integracions: {
                title: "Integrations",
                column: {
                    codi: "Code",
                    nom: "Name",
                    estat: "State",
                    latencia: "Latency",
                    peticions: "Requests",
                },
            },
            subsistemes: {
                title: "Subsystems",
                column: {
                    codi: "Code",
                    nom: "Name",
                    estat: "State",
                    latencia: "Latency",
                },
            },
            estats: {
                title: "States",
            },
        },
        apps: {
            title: "Applications",
            create: "Create application",
            update: "Update application",
            general: "General",
            entornApp: "App environments",
        },
        entorns: {
            title: "Environments",
        },
        appsEntorns: {
            title: "Environments",
            resourceTitle: "environment",
        },
        monitors: {
            title: "Monitors",
            detail: {
                title: "Communication details with the integration",
                data: "Date",
                operacio: "Description",
                tipus: "Type",
                estat: "Status",
                codiUsuari: "User",
                errorDescripcio: "Error description",
                excepcioMessage: "Exception message",
                excepcioStacktrace: "Exception stack trace",
            },
            modulEnum: {
                salut: "Salud",
                estadistica: "Estadística",
            },
        },
        widget: {
            title: "Statistic widgets",
            grid: {
                position: "Position",
                size: "Size",
            },
            form: {
                periode: "Period",
                simple: "Simple widget",
                grafic: "Graphic widget",
                taula: "Table widget",
            },
            simple: {
                tab: {
                    title: "Simple",
                },
                title: "Simple statistic Widgets",
                resourceTitle: "simple widget",
            },
            grafic: {
                tab: {
                    title: "Chart",
                },
                title: "Statistic chart widgets",
                resourceTitle: "chart widget",
            },
            taula: {
                tab: {
                    title: "Table",
                },
                title: "Statistic table widgets",
                resourceTitle: "table widget",
                columna: {
                    indicador: "Indicator",
                    titolIndicador: "Title",
                    tipusIndicador: "Aggregation type",
                    periodeIndicador: "Periode aggr.",
                    accions: "Actions",
                    arrossega: "Drag to reorder",
                },
            },
            action: {
                add: {
                    label: "Add",
                },
            },
        },
        dashboards: {
            title: "Dashboards",
            edit: "Edit",
            dashboardView: "Go to dashboard",
            components: {
                llistar: "List components",
                afegir: "Add component",
            },
            action: {
                select: {
                    title: "Select the dashboard to display...",
                },
                llistarWidget: {
                    label: "List widgets",
                    title: "List widgets",
                },
                llistarTitle: {
                    label: "List titles",
                    title: "List titles",
                },
                patchItem: {
                    success: "Saved successfully",
                    warning: "Could not find newDashboardItem with id {{id}}, the update will not propagate.",
                    error: "Error while saving",
                    saveError: "Save error",
                },
                addWidget: {
                    label: "Add widget",
                    title: "Add widget",
                    success: "Widget added successfully",
                    error: "Error while adding the widget",
                },
                afegirTitle: {
                    label: "Add title",
                    title: "Add title",
                }
            },
            alert: {
                tornarLlistat: "Return to list",
                tornarTauler: "Return to default dashboard",
                notExists: "The dashboard does not exist.",
                notDefined: "No dashboard is defined.",
                carregar: "Error loading the dashboard.",
            }
        },
        caches: {
            title: "Caches",
            columna: {
                codi: "Code",
                nom: "Name",
                entrades: "Elements",
                mida: "Size (bytes)",
            },
            buidar: {
                label: "Clear",
                titol: "Clear cache",
                confirm: "Are you sure you want to clear the cache?",
                success: "Cache cleared",
                error: "Error clearing cache",
                totes: {
                    titol: "Clear all caches",
                    confirm: "Are you sure ypu want to clear oll caches?",
                    success: "Caches cleared",
                    error: "Error clearing caches",
                },
            },
        },
        notFound: "Not found",
    },
    generic: {
        tipus: "Type",
        periode: "Period",
    },
    errors: {
        camp: {
            obligatori: "Required field",
        },
    },
    components: {
        clear: "Clear",
        search: "Search",
        copiarContingut: "Copy content",
        copiarContingutTitle: "Copy the content",
        copiarContingutSuccess: "Content copied to clipboard",
    },
};

export default translationEn;
