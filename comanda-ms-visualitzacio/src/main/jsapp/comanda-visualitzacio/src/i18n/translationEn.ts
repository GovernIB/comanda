const translationEn = {
    enum: {
        appEstat: {
            UP: {
                title: "Up",
                tooltip: "The application is working <bold>correctly</bold>. <br> The rate of <italic>detected errors</italic> is <underline>less than 5%</underline>."
            },
            WARN: {
                title: "Warn",
                tooltip: "The rate of <italic>detected errors</italic> is <underline>between 5% and 10%</underline>."
            },
            DOWN: {
                title: "Down",
                tooltip: "<bold>Errors have been detected</bold>. <br> The <italic>errors detected</italic> rate is <underline>greater than 30%</underline>."
            },
            DEGRADED: {
                title: "Degraded",
                tooltip: "<bold>There are occasional errors</bold>. <br> The <italic>rate of detected errors</italic> is <underline>between 10% and 30%</underline>."
            },
            MAINTENANCE: {
                title: "Maintenance",
                tooltip: "The application <bold>is unavailable</bold> due to <underline>maintenance</underline>."
            },
            UNKNOWN: {
                title: "Unknown",
                tooltip: "<bold>No information available</bold> about the application status."
            },
            ERROR: {
                title: "Error",
                tooltip: "The application <bold>is unavailable</bold> due to <underline>serious operational errors</underline>."
            },
        },
    },
    menu: {
        salut: "Health",
        estadistiques: "Statistics",
        tasca: "Tasks",
        avis: "Warnings",
        alarmes: "Active alarms",
        monitoritzacio: "Monitoring",
        monitor: "Monitor",
        cache: "Cache management",
        broker: "Queue Manager",
        configuracio: "Configuration",
        app: "Applications",
        entorn: "Environments",
        versionsEntorn: "Versions by environment",
        alarmaConfig: "Alarms",
        integracio: "Integrations",
        widget: "Statistic widgets",
        dashboard: "Dashboards",
        calendari: "Calendar",
        parametre: "Parameters",
        user: {
            options: {
                profile: {
                    title: "My profile",
                    form: {
                        userData: "User data",
                        genericConfig: "Generic config"
                    }
                }
            }
        }
    },
    page: {
        salut: {
            title: "Health",
            refrescar: "Refresh",
            filtrar: "Filter by application/environment",
            senseFiltres: "No filters",
            nd: "N/A",
            refresh: {
                last: "Last refresh",
                next: "Next refresh in",
            },
            apps: {
                column: {
                    group: "Application / environment",
                    estat: "State",
                    infoData: "Last query",
                    infoDataDescription: "Date of last information query",
                    codi: "Code",
                    nom: "Name",
                    versio: "Version",
                    revisio: "Revision",
                    bd: "Database",
                    latencia: "Latency",
                    integ: "Integrations",
                    subsis: "Subsystems",
                    msgs: "Messages",
                },
                detalls: "Details",
            },
            groupingSelect: {
                BY_APPLICATION: "By application",
                BY_ENVIRONMENT: "By environment",
            },
            refreshperiod: {
                PT1M: "1 minute",
                PT5M: "5 minutes",
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
                revisio: "Revision",
                jdk: {
                    versio: "JDK versin",
                },
                data: "Last update",
                bdEstat: "Database",
                appLatencia: "Latency",
                missatges: "Messages",
                detalls: "Details",
                noInfo: 'No data available for this environment',
                downAlert: "The application is crashed. Unable to display all health information.",
            },
            latencia: {
                title: "Latency",
                error: "There was an error displaying the chart",
            },
            estatLatencia: {
                title: "State and latency",
                noInfo: "No data to display.",
            },
            integracions: {
                title: "Integrations",
                integracioUpCount: "Active",
                integracioDownCount: "Inactive",
                integracioDesconegutCount: "Unknown state",
                noInfo: "There is no information about integrations",
                column: {
                    codi: "Code",
                    nom: "Name",
                    estat: "State",
                    peticionsTotals: "Total requests",
                    tempsMigTotal: "Total avg. time",
                    peticionsPeriode: "Time period requests",
                    tempsMigPeriode: "Time period avg. time",
                },
            },
            subsistemes: {
                title: "Subsystems",
                subsistemaUpCount: "Active",
                subsistemaDownCount: "Inactive",
                subsistemaDesconegutCount: "Unknown state",
                noInfo: "There is no information about subsystems",
                column: {
                    codi: "Code",
                    nom: "Name",
                    estat: "State",
                    peticionsTotals: "Total requests",
                    tempsMigTotal: "Total avg. time",
                    peticionsPeriode: "Time period requests",
                    tempsMigPeriode: "Time period avg. time",
                },
            },
            missatges: {
                title: "Messages",
                noInfo: "No information available",
                column: {
                    data: "Date",
                    nivell: "Level",
                    missatge: "Message",
                },
            },
            msgs: {
                missatgeErrorCount: "Errors",
                missatgeWarnCount: "Warnings",
                missatgeInfoCount: "Info"
            },
            contexts: {
                title: "Contexts",
                noInfo: "There is no information about contexts",
                column: {
                    codi: "Code",
                    nom: "Name",
                    path: "Path",
                    api: "Api",
                    manuals: "Manuals",
                },
            },
            detalls: {
                title: "Details",
                noInfo: "There is no information about application details",
            },
            estats: {
                title: "States",
            },
        },
        dashboards: {
            title: "Dashboards",
            edit: "Edit",
            dashboardView: "Go to dashboard",
            cloneDashboard: {
                title: "Clone the dashboard",
                success: "Dashboard cloned correctly",
            },
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
                },
                export: "Export dashboard",
            },
            alert: {
                tornarLlistat: "Return to list",
                tornarTauler: "Return to default dashboard",
                notExists: "The dashboard does not exist.",
                notDefined: "No dashboard is defined.",
                carregar: "Error loading the dashboard.",
            }
        },
        tasques: {
            filter: {
                more: "More fields",
                finished: "Finished only",
                unfinished: "Unfinished",
            },
            grid: {
                groupHeader: "Name",
                action: {
                    obrir: "Open task",
                }
            }
        },
        avisos: {
            filter: {
                more: "More fields",
                finished: "Finished only",
                unfinished: "Unfinished",
            },
            grid: {
                groupHeader: "Name",
            }
        },
        alarmaConfig: {
            title: "Alarm configuration",
            create: "Create alarm configuration",
            update: "Edit alarm configuration",
            condicio: {
                title: "Condition",
                subtitle: "Condition that must be met to trigger the alarm",
            },
            periode: {
                title: "Period",
                subtitle: "Time period during which the condition must be met to trigger the alarm",
                switch: "With activation period"
            },
        },
        apps: {
            title: "Applications",
            create: "Create application",
            update: "Update application",
            general: "General",
            entornApp: "App environments",
            fields: {
                compactable: "Compactable",
                compactacioSetmanalMesos: "Weekly compaction (months)",
                compactacioMensualMesos: "Monthly compaction (months)",
                eliminacioMesos: "Deletion (months)",
            },
            tooltips: {
                compactacioMesos: "How many full months the statistics will be kept before...\n - compacting them by week\n - compacting them by month\n - deleting them.\nIf the fields are left empty, or with value 0, the ... will not be performed",
            },
            progress: {
                diaries: "daily statistics",
                weeklies: "weekly statistics",
                monthlies: "monthly statistics",
            },
            action: {
                export: "Export application",
            },
        },
        entorns: {
            title: "Environments",
        },
        appsEntorns: {
            title: "Environments",
            resourceTitle: "environment",
            action: {
                toolbarActiva: {
                    activar: "Activate",
                    desactivar: "Deactivate",
                    ok: "The action has been executed successfully",
                }
            }
        },
        versionsEntorns: {
            title: "Versions by environment",
        },
        integracions: {
            title: "Integrations",
            column: {
                codi: "Code",
                nom: "Name",
                logo: "Logo",
            },
        },
        dimensions: {
            title: "Dimensions",
            values: "Values",
            column: {
                entornApp: "Application environment",
                codi: "Code",
                nom: "Name",
                descripcio: "Description",
                agrupacions: "Has groupings",
            },
        },
        indicators: {
            title: "Indicators",
            edit: "Edit",
            column: {
                entornApp: "Application environment",
                codi: "Code",
                nom: "Name",
                descripcio: "Description",
                format: "Format",
                indicadorMitjana: "Indicator by average",
            },
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
                estatEnum: {
                    ok: "OK",
                    error: "Error",
                    warn: "Warning",
                },
                tipusEnum: {
                    sortida: "Output",
                    entrada: "Input",
                    interna: "Internal",
                },
            },
            modulEnum: {
                salut: "Health",
                estadistica: "Statistics",
                configuracio: "Configuration",
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
                preview: "Preview",
                configVisual: "Visual configuration",
                configGeneral: "General configuration",
                configTaula: "Table configuration",
                configFont: "Font size configuration",
                graficBar: "Bar chart",
                graficLin: "Line chart",
                graficPst: "Pie chart",
                graficGug: "Gauge chart",
                graficMap: "Heatmap chart",
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
                indicadors: "Indicators",
            },
            taula: {
                tab: {
                    title: "Table",
                },
                title: "Statistic table widgets",
                resourceTitle: "table widget",
                tableCols: "Table columns",
                columna: {
                    indicador: "Indicator",
                    titolIndicador: "Title",
                    tipusIndicador: "Aggregation type",
                    periodeIndicador: "Periode aggr.",
                    accions: "Actions",
                    arrossega: "Drag to reorder",
                },
            },
            atributsVisuals: {
                colorText: "Text color",
                colorFons: "Background color",
                icona: "Icon",
                colorIcona: "Icon color",
                colorFonsIcona: "Icon background color",
                colorTextDestacat: "Highlighted text color",
                mostrarVora: "Show border",
                colorVora: "Border color",
                ampleVora: "Border width",
                midaFontTitol: "Title font size",
                midaFontDescripcio: "Description font size",
                midaFontValor: "Value font size",
                midaFontUnitats: "Units font size",
                midaFontCanviPercentual: "Percentual change font size",
                colorsPaleta: "Palette colors",
                mostrarReticula: "Show grid",
                barStacked: "Stacked bars",
                barHorizontal: "Horizontal bars",
                lineShowPoints: "Show points",
                area: "Fill area",
                lineSmooth: "Smooth lines",
                lineWidth: "Line width",
                outerRadius: "Outer radius",
                pieDonut: "Donut type",
                innerRadius: "Inner radius",
                pieShowLabels: "Show labels",
                labelSize: "Label size",
                gaugeMin: "Minimum value",
                gaugeMax: "Maximum value",
                gaugeColors: "Colors (comma-separated)",
                gaugeRangs: "Ranges (comma separated)",
                heatmapMinValue: "Minimum value",
                heatmapMaxValue: "Maximum value",
                colorTextTaula: "Table text color",
                colorFonsTaula: "Table background color",
                mostrarCapcalera: "Show header",
                colorCapcalera: "Header text color",
                colorFonsCapcalera: "Header background color",
                mostrarAlternancia: "Show row alternation",
                colorAlternancia: "Alternation color",
                mostrarVoraTaula: "Show table border",
                colorVoraTaula: "Border color",
                ampleVoraTaula: "Border width",
                mostrarSeparadorHoritzontal: "Show horizontal separator",
                colorSeparadorHoritzontal: "Separator color",
                ampleSeparadorHoritzontal: "Separator width",
                mostrarSeparadorVertical: "Show vertical separator",
                colorSeparadorVertical: "Separator color",
                ampleSeparadorVertical: "Separator width",
            },
            editorPaleta: {
                title: "Color Palette Editor",
                color: "Color",
                hex: "HEX Code",
                palet: "Current palette:",
                empty: "There are no colors in the palette.",
                exist: "This color already exists in the palette!",
            },
            action: {
                add: {
                    label: "Add",
                },
                addColumn: {
                    label: "Add column",
                },
            },
        },
        caches: {
            title: "Caches",
            buidar: {
                label: "Clear",
                titol: "Clear cache",
                confirm: "Are you sure you want to clear the cache?",
                success: "Cache cleared",
                error: "Error clearing cache",
                totes: {
                    titol: "Clear all caches",
                    confirm: "Are you sure you want to clear all caches?",
                    success: "Caches cleared",
                    error: "Error clearing caches",
                },
            },
        },
        broker: {
            title: "Queue Manager",
            error: {
                fetchFailed: "Error fetching broker data",
            },
            detail: {
                title: "Broker Information",
                version: "Version",
                name: "Name",
                status: "Status",
                uptime: "Uptime",
                memoryUsage: "Memory Usage",
                diskUsage: "Disk Usage",
                totalConnections: "Total Connections",
                totalQueues: "Total Queues",
                totalMessages: "Total Messages",
            },
            queues: {
                title: "Queues",
            },
            queue: {
                address: "Address",
                routingType: "Routing Type",
                durable: "Durable",
                messageCount: "Message Count",
                consumerCount: "Consumer Count",
                viewMessages: "View Messages",
            },
        },
        queue: {
            title: "Queue: {{name}}",
            error: {
                fetchFailed: "Error fetching queue data",
                purgeFailed: "Error purging queue",
            },
            detail: {
                title: "Queue Details",
                name: "Name",
                address: "Address",
                routingType: "Routing Type",
                durable: "Durable",
                messageCount: "Message Count",
                consumerCount: "Consumer Count",
                deliveringCount: "Delivering Count",
                messagesAdded: "Messages Added",
                messagesAcknowledged: "Messages Acknowledged",
                filter: "Filter",
                temporary: "Temporary",
                autoCreated: "Auto Created",
                purgeOnNoConsumers: "Purge On No Consumers",
                maxConsumers: "Max Consumers",
            },
            actions: {
                purge: "Purge Queue",
            },
            purge: {
                title: "Purge Queue",
                confirmation: "Are you sure you want to remove all messages from queue {{name}}?",
            },
        },
        message: {
            title: "Messages",
            error: {
                deleteFailed: "Error deleting message",
            },
            detail: {
                title: "Message Details",
                messageID: "Message ID",
                queueName: "Queue Name",
                timestamp: "Timestamp",
                type: "Type",
                durable: "Durable",
                priority: "Priority",
                size: "Size",
                redelivered: "Redelivered",
                deliveryCount: "Delivery Count",
                expirationTime: "Expiration Time",
                properties: "Properties",
                content: "Content",
            },
            grid: {
                messageID: "Message ID",
                timestamp: "Timestamp",
                type: "Type",
                priority: "Priority",
                size: "Size",
                actions: "Actions",
            },
        },
        parametres: {
            title: "Parameters",
            detail: {
                title: "Parameter details",
                grup: "Group",
                subGrup: "Subgroup",
                tipus: "Type",
                codi: "Code",
                nom: "Name",
                descripcio: "Description",
                valor: "Value",
                tipusEnum: {
                    NUMERIC: "Numeric",
                    TEXT: "Text",
                    BOOLEAN: "True or false",
                    PASSWORD: "Password",
                    CRON: "Cron",
                    SELECT: "Selection",
                },
                valuesTootip: {
                    true: "True",
                    false: "False",
                    null: "Undefined",
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
    form: {
        field: {
            file: {
                edit: 'Edit',
                download: 'Download',
                clear: 'Clear',
            },
        },
    },
    calendari: {
        seleccionar_entorn_app: "Select application environment",
        seleccionar: "Select",
        seleccionar_entorn_app_primer: "Select firs an application environment",
        carregar_interval: "Load data by interval",
        carregar_mes_actual: "Load data for the current month",
        today: "Hoy",
        data_inici: "Start date",
        data_fi: "End date",
        cancelar: "Cancel",
        carregar: "Load",
        sense_dades: "No statistical data",
        dades_buides: "The application has no statistical data",
        dades_disponibles: "Data available",
        dades_disponibles_tooltip: "Click to see the available statistical data",
        obtenir_dades: "Get data",
        obtenir_dades_tooltip: "Click to get the statistical data for this day",
        error_dades: "Error getting data",
        error_dades_tooltip: "An error occurred while trying to get the statistical data of the application",
        error_titol: "Error getting statistical data",
        data: "Date",
        missatge: "Message",
        traca: "Trace",
        tancar: "Close",
        success_obtenir_dades: "Statistics data obtained successfully",
        error_obtenir_dades: "Error getting statistical data",
        success_carregar_interval: " Statistics data loaded successfully",
        error_carregar_interval: "Error loading data by interval",
        error_dades_disponibles: "Error getting available statistical data days",
        error_dades_dia: "Error getting statistical data for the day",
        error_obtenir_dates_mes: "Could not determine the dates for the current month",
        carregant: "Loading",
        carregant_dades: "Loading statistical data...",
        modal_dades_dia: "Available data for the day",
        dimensions: "Dimensions",
        indicadors: "Indicators",
    },
    treeData: {
        treeView: "Tree view"
    },
};

export default translationEn;
