const translationEs = {
    enum: {
        appEstat: {
            UP: {
                title: "Activa",
                tooltip: "La aplicación funciona <bold>correctamente</bold>."
            },
            WARN: {
                title: "Advertencia",
                tooltip: "Se han detectado tasas de <bold>errores menores</bold> en la aplicación, o algun subsistema no crítico fallando."
            },
            DOWN: {
                title: "Caida",
                tooltip: "La aplicación <bold>parece caida</bold>. No se puede acceder a ella, o a su sistema de salud."
            },
            DEGRADED: {
                title: "Degradada",
                tooltip: "Se han detectado tasas de <bold>errores significativas en la aplicación</bold>, o en algun subsistema crítico."
            },
            MAINTENANCE: {
                title: "Mantenimiento",
                tooltip: "La aplicación <bold>no está disponible</bold> debido a <underline>tareas de mantenimiento</underline>."
            },
            UNKNOWN: {
                title: "Desconocido",
                tooltip: "<bold>No se dispone de información</bold> acerca del estado de la aplicación."
            },
            ERROR: {
                title: "Error",
                tooltip: "Se han detectado <bold>altas tasas de errores</bold> en la aplicación, o algun subsistema crítico fallando."
            },
        },
        integracioEstat: {
            UP: {
                tooltip: "Servicio <bold>operativo</bold>.<br> Sin errores o con una tasa de errores <underline>inferior al 10%</underline> en las últimas peticiones"
            },
            WARN: {
                tooltip: "Servicio con una tasa de errores <underline>entre el 10% y el 20%</underline> en las últimas peticiones"
            },
            DEGRADED: {
                tooltip: "Servicio con una tasa de errores <underline>entre el 20% y el 50%</underline> en las últimas peticiones",
            },
            ERROR: {
                tooltip: "Servicio con una <bold>alta tasa de errores</bold>, <underline>superior al 50%</underline> en las últimas peticiones"
            },
            DOWN: {
                tooltip: "Servicio <bold>no operativo</bold>. Tasa de errores del <underline>100%</underline> en las últimas peticiones",
            },
            MAINTENANCE: {
                tooltip: "Sin información del servicio debido a tareas de <bold>mantenimiento</bold>",
            },
            UNKNOWN: {
                tooltip: "No se dispone de información sobre el estado del servicio",
            },
        },
    },
    menu: {
        salut: "Salud",
        estadistiques: "Estadísticas",
        tasca: "Tareas",
        avis: "Avisos",
        alarmes: "Alarmas activas",
        monitoritzacio: "Monitorización",
        monitor: "Monitor",
        cache: "Gestión de cachés",
        broker: "Gestor de colas",
        configuracio: "Configuración",
        app: "Aplicaciones",
        entorn: "Entornos",
        versionsEntorn: "Versiones por entorno",
        alarmaConfig: "Alarmas",
        integracio: "Integraciones",
        widget: "Widgets estadísticos",
        dashboard: "Cuadros de control",
        calendari: "Calendario",
        parametre: "Parámetros",
        dimensio: "Dimensiones",
        indicador: "Indicadores",
        user: {
            options: {
                profile: {
                    title: "Mi perfil",
                    form: {
                        userData: "Datos del usuario",
                        genericConfig: "Configuración genérica"
                    }
                }
            }
        }
    },
    page: {
        salut: {
            title: "Salut",
            refrescar: "Refrescar",
            filtrar: "Filtrar por aplicación/entorno",
            senseFiltres: "Sin filtros",
            nd: "N/D",
            refresh: {
                last: "Último refresco",
                next: "Próximo refresco en",
            },
            apps: {
                column: {
                    group: "Aplicación / entorno",
                    estat: "Estado",
                    infoData: "Última consulta",
                    infoDataDescription: "Fecha última consulta de información",
                    codi: "Código",
                    nom: "Nombre",
                    versio: "Versión",
                    revisio: "Revisión",
                    bd: "Base de datos",
                    latencia: "Latencia",
                    integ: "Integraciones",
                    subsis: "Subsistemas",
                    msgs: "Mensajes",
                },
                detalls: "Detalles",
            },
            groupingSelect: {
                BY_APPLICATION: "Por aplicación",
                BY_ENVIRONMENT: "Por entorno",
            },
            refreshperiod: {
                PT1M: "1 minuto",
                PT5M: "5 minutos",
                PT10M: "10 minutos",
                PT30M: "30 minutos",
                PT1H: "1 hora",
            },
            timerange: {
                PT15M: "Últimos 15 minutos",
                PT1H: "Última hora",
                P1D: "Último dia",
                P7D: "Última semana",
                P1M: "Último mes",
            },
            info: {
                title: "Información",
                versio: "Versión",
                revisio: "Revisión",
                jdk: {
                    versio: "Versión JDK",
                },
                data: "Última actualización",
                bdEstat: "Base de datos",
                appLatencia: "Latencia",
                missatges: "Mensajes",
                detalls: "Detalles",
                noInfo: 'No hay datos disponibles para este entorno',
                downAlert: "La aplicación se encuentra caída. No se puede mostrar toda la información de salud.",
            },
            latencia: {
                title: "Latencia",
                error: "Ha habido un error al mostrar el gráfico",
            },
            estatLatencia: {
                title: "Estado y latencia",
                noInfo: "No hay datos que mostrar",
            },
            integracions: {
                title: "Integraciones",
                integracioUpCount: "Activas",
                integracioDownCount: "Inactivas",
                integracioDesconegutCount: "Estado desconocido",
                noInfo: "No hay información de integraciones",
                column: {
                    codi: "Código",
                    nom: "Nombre",
                    estat: "Estado",
                    peticionsTotals: "Peticiones totales",
                    tempsMigTotal: "Tiempo medio total",
                    peticionsPeriode: "Peticiones periodo",
                    tempsMigPeriode: "Tiempo medio periodo",
                },
            },
            subsistemes: {
                title: "Subsistemas",
                subsistemaUpCount: "Activos",
                subsistemaDownCount: "Inactivos",
                subsistemaDesconegutCount: "Estado desconocido",
                noInfo: "No hay información de subsistemas",
                column: {
                    codi: "Código",
                    nom: "Nombre",
                    estat: "Estado",
                    peticionsTotals: "Peticiones totales",
                    tempsMigTotal: "Tiempo medio total",
                    peticionsPeriode: "Peticiones periodo",
                    tempsMigPeriode: "Tiempo medio periodo",
                },
            },
            missatges: {
                title: "Mensajes",
                noInfo: "No hay información",
                column: {
                    data: "Fecha",
                    nivell: "Nivel",
                    missatge: "Mensaje",
                },
            },
            msgs: {
                missatgeErrorCount: "Errores",
                missatgeWarnCount: "Alertas",
                missatgeInfoCount: "Información"
            },
            contexts: {
                title: "Contextos",
                noInfo: "No hay información de contextos",
                column: {
                    codi: "Código",
                    nom: "Nombre",
                    path: "Ruta",
                    api: "Api",
                    manuals: "Manuales",
                },
            },
            detalls: {
                title: "Detalles",
                noInfo: "No hay información sobre detalles de la aplicación",
            },
            estats: {
                title: "Estados",
            },
        },
        dashboards: {
            title: "Cuadros de control",
            edit: "Editar",
            dashboardView: "Ir al panel de control",
            cloneDashboard: {
                title: "Clonar el panel de control",
                success: "Panel de control clonado correctamente",
            },
            components: {
                llistar: "Listar componentes",
                afegir: "Añadir componente",
            },
            action: {
                select: {
                    title: "Seleccione el tablero a mostrar...",
                },
                llistarWidget: {
                    label: "Listar widgets",
                    title: "Listar widgets",
                },
                llistarTitle: {
                    label: "Listar títulos",
                    title: "Listar títulos",
                },
                patchItem: {
                    success: "Guardado correctamente",
                    warning: "No se pudo encontrar newDashboardItem con id {{id}}, la actualización no se propagará.",
                    error: "Error al guardar",
                    saveError: "Error al guardar",
                },
                addWidget: {
                    label: "Añadir widget",
                    title: "Añadir widget",
                    success: "Widget añadido correctamente",
                    error: "Error al añadir el widget",
                },
                afegirTitle: {
                    label: "Añadir título",
                    title: "Añadir título",
                },
                export: "Exportar tablero",
            },
            alert: {
                tornarLlistat: "Volver al listado",
                tornarTauler: "Volver al tablero por defecto",
                notExists: "El panel de control no existe.",
                notDefined: "No hay ningún panel de control definido.",
                carregar: "Error al cargar el panel de control.",
            }
        },
        tasques: {
            filter: {
                more: "Más campos",
                unfinishedOnlyDisabled: "Visualizar únicamente las tareas pendientes (desactivado)",
                unfinishedOnlyEnabled: "Visualizar únicamente las tareas pendientes (activado)",
                ownTasksOnlyDisabled: "Visualizar únicamente mis tareas (desactivado)",
                ownTasksOnlyEnabled: "Visualizar únicamente mis tareas (activado)",
            },
            grid: {
                groupHeader: "Nombre",
                action: {
                    obrir: "Obrir tasca",
                },
                column: {
                    appEntorn: "Aplicación - Entorno",
                },
            }
        },
        avisos: {
            filter: {
                more: "Más campos",
                unfinishedOnlyDisabled: "Visualizar únicamente los avisos pendientes (desactivado)",
                unfinishedOnlyEnabled: "Visualizar únicamente los avisos pendientes (activado)",
            },
            grid: {
                groupHeader: "Nombre",
                column: {
                    appEntorn: "Aplicación - Entorno",
                },
            }
        },
        alarmaConfig: {
            title: "Configuración de alarmas",
            create: "Crear configuración de alarma",
            update: "Modificar configuración de alarma",
            condicio: {
                title: "Condición",
                subtitle: "Condición que se debe cumplir para generar la alarma",
            },
            periode: {
                title: "Periodo",
                subtitle: "Periodo de tiempo durante el cual se debe cumplir la condición para generar la alarma",
                switch: "Con periodo de activación"
            },
        },
        apps: {
            title: "Aplicaciones",
            create: "Crear aplicación",
            update: "Modificar aplicación",
            general: "General",
            entornApp: "Entornos de la aplicación",
            fields: {
                compactable: "Compactable",
                compactacioSetmanalMesos: "Compactación semanal (meses)",
                compactacioMensualMesos: "Compactación mensual (meses)",
                eliminacioMesos: "Borrado (meses)",
            },
            tooltips: {
                compactacioMesos: "Cuántos meses completos se mantendrán las estadísticas antes de...\n - compactarlas por semana\n - compactarlas por mes\n - borrarlas.\nSi se dejan los campos sin valor, o valor 0, no se realizará la ...",
            },
            progress: {
                diaries: "estadísticas diarias",
                weeklies: "estadísticas semanales",
                monthlies: "estadísticas mensuales",
            },
            action: {
                export: "Exportar aplicación",
            },
        },
        entorns: {
            title: "Entornos",
        },
        appsEntorns: {
            title: "Entornos",
            resourceTitle: "entorno",
            action: {
                toolbarActiva: {
                    activar: "Activar",
                    desactivar: "Desactivar",
                    ok: "La acción se ha ejecutado correctamente",
                }
            }
        },
        versionsEntorns: {
            title: "Versiones por entorno",
        },
        integracions: {
            title: "Integraciones",
            column: {
                codi: "Código",
                nom: "Nombre",
                logo: "Logo",
            },
        },
        dimensions: {
            title: "Dimensiones",
            values: "Valores",
            column: {
                entornApp: "Entorno de aplicación",
                codi: "Código",
                nom: "Nombre",
                descripcio: "Descripción",
                agrupacions: "Tiene agrupaciones",
            },
        },
        indicadors: {
            title: "Indicadores",
            edit: "Editar",
            column: {
                entornApp: "Entorno de aplicación",
                codi: "Código",
                nom: "Nombre",
                descripcio: "Descripción",
                format: "Formato",
                indicadorMitjana: "Indicador para media",
            },
        },
        monitors: {
            title: "Monitores",
            detail: {
                title: "Detalles de la comunicación con la integración",
                data: "Fecha",
                operacio: "Descripción",
                tipus: "Tipo",
                estat: "Estado",
                codiUsuari: "Usuario",
                errorDescripcio: "Descripción del error",
                excepcioMessage: "Mensaje de la excepción",
                excepcioStacktrace: "Traza de la excepción",
                estatEnum: {
                    ok: "Correcto",
                    error: "Erróneo",
                    warn: "Aviso",
                },
                tipusEnum: {
                    sortida: "Salida",
                    entrada: "Entrada",
                    interna: "Interna",
                },
            },
            modulEnum: {
                salut: "Salud",
                estadistica: "Estadística",
                configuracio: "Configuración",
            },
        },
        widget: {
            title: "Widgets estadísticos",
            grid: {
                position: "Posición",
                size: "Tamaño",
            },
            form: {
                periode: "Periodo",
                simple: "Widget simple",
                grafic: "Widget gráfico",
                taula: "Widget tabla",
                preview: "Previsualización",
                configVisual: "Configuración visual",
                configGeneral: "Configuración general",
                configTaula: "Configuración tabla",
                configFont: "Configuración del tamaño de fuente",
                graficBar: "Gráfico de barras",
                graficLin: "Gráfico de líneas",
                graficPst: "Gráfico de pastel",
                graficGug: "Gráfico de gauge",
                graficMap: "Gráfico de heatmap",
            },
            simple: {
                tab: {
                    title: "Simples",
                },
                title: "Widgets estadísticos simples",
                resourceTitle: "widget simple",
            },
            grafic: {
                tab: {
                    title: "Gráficos",
                },
                title: "Widgets estadísticos gráficos",
                resourceTitle: "widget gráfico",
                indicadors: "Indicadores",
            },
            taula: {
                tab: {
                    title: "Tabla",
                },
                title: "Widgets estadísticos tipo tabla",
                resourceTitle: "widget tipo tabla",
                tableCols: "Columnas de la tabla",
                columna: {
                    indicador: "Indicador",
                    titolIndicador: "Título",
                    tipusIndicador: "Tipo agrupación",
                    periodeIndicador: "Período agr.",
                    accions: "Acciones",
                    arrossega: "Arrastra para reordenar",
                },
            },
            atributsVisuals: {
                colorText: "Color del texto",
                colorFons: "Color de fondo",
                icona: "Icono",
                colorIcona: "Color del icono",
                colorFonsIcona: "Color de fondo del icono",
                colorTextDestacat: "Color del texto destacado",
                mostrarVora: "Mostrar borde",
                colorVora: "Color del borde",
                ampleVora: "Ancho del borde",
                midaFontTitol: "Tamaño de fuente del título",
                midaFontDescripcio: "Tamaño de fuente de la descripción",
                midaFontValor: "Tamaño de fuente del valor",
                midaFontUnitats: "Tamaño de fuente de las unidades",
                midaFontCanviPercentual: "Tamaño de fuente del cambio porcentual",
                colorsPaleta: "Colores de la paleta",
                mostrarReticula: "Mostrar cuadrícula",
                barStacked: "Barras apiladas",
                barHorizontal: "Barras horizontales",
                lineShowPoints: "Mostrar puntos",
                area: "Rellenar área",
                lineSmooth: "Líneas suaves",
                lineWidth: "Ancho de línea",
                outerRadius: "Radio exterior",
                pieDonut: "Tipo donut",
                innerRadius: "Radio interior",
                pieShowLabels: "Mostrar etiquetas",
                labelSize: "Tamaño de etiquetas",
                gaugeMin: "Valor mínimo",
                gaugeMax: "Valor máximo",
                gaugeColors: "Colores (separados por comas)",
                gaugeRangs: "Rangos (separados por comas)",
                heatmapMinValue: "Valor mínimo",
                heatmapMaxValue: "Valor máximo",
                colorTextTaula: "Color de texto de la tabla",
                colorFonsTaula: "Color de fondo de la tabla",
                mostrarCapcalera: "Mostrar cabecera",
                colorCapcalera: "Color de texto de la cabecera",
                colorFonsCapcalera: "Color de fondo de la cabecera",
                mostrarAlternancia: "Mostrar alternancia de filas",
                colorAlternancia: "Color de alternancia",
                mostrarVoraTaula: "Mostrar borde de la tabla",
                colorVoraTaula: "Color del borde",
                ampleVoraTaula: "Ancho del borde",
                mostrarSeparadorHoritzontal: "Mostrar separador horizontal",
                colorSeparadorHoritzontal: "Color del separador",
                ampleSeparadorHoritzontal: "Ancho del separador",
                mostrarSeparadorVertical: "Mostrar separador vertical",
                colorSeparadorVertical: "Color del separador",
                ampleSeparadorVertical: "Ancho del separador",
            },
            editorPaleta: {
                title: "Editor de paleta de colores",
                color: "Color",
                hex: "Código HEX",
                palet: "Paleta actual:",
                empty: "No hay colores en la paleta.",
                exist: "¡Este color ya existe en la paleta!",
            },
            action: {
                add: {
                    label: "Añadir",
                },
                addColumn: {
                    label: "Añadir columna",
                },
            },
        },
        caches: {
            title: "Caches",
            buidar: {
                label: "Vaciar",
                titol: "Vaciar cache",
                confirm: "¿Estás seguro que quieres vaciar la cache?",
                success: "Cache vaciada",
                error: "Error vaciando cache",
                totes: {
                    titol: "Vaciar todas las caches",
                    confirm: "¿Estás seguro que quieres vaciar todas las caches?",
                    success: "Caches vaciadas",
                    error: "Error vaciando caches",
                },
            },
        },
        broker: {
            title: "Gestor de colas",
            error: {
                fetchFailed: "Error al obtener los datos del broker",
            },
            detail: {
                title: "Información del broker",
                version: "Versión",
                name: "Nombre",
                status: "Estado",
                uptime: "Tiempo de actividad",
                memoryUsage: "Uso de memoria",
                diskUsage: "Uso de disco",
                totalConnections: "Conexiones totales",
                totalQueues: "Colas totales",
                totalMessages: "Mensajes totales",
            },
            queues: {
                title: "Colas",
            },
            queue: {
                address: "Dirección",
                routingType: "Tipo de enrutamiento",
                durable: "Durable",
                messageCount: "Número de mensajes",
                consumerCount: "Número de consumidores",
                viewMessages: "Ver mensajes",
            },
        },
        queue: {
            title: "Cola: {{name}}",
            error: {
                fetchFailed: "Error al obtener los datos de la cola",
                purgeFailed: "Error al purgar la cola",
            },
            detail: {
                title: "Detalles de la cola",
                name: "Nombre",
                address: "Dirección",
                routingType: "Tipo de enrutamiento",
                durable: "Durable",
                messageCount: "Número de mensajes",
                consumerCount: "Número de consumidores",
                deliveringCount: "Mensajes en entrega",
                messagesAdded: "Mensajes añadidos",
                messagesAcknowledged: "Mensajes confirmados",
                filter: "Filtro",
                temporary: "Temporal",
                autoCreated: "Creación automática",
                purgeOnNoConsumers: "Purgar sin consumidores",
                maxConsumers: "Máximo de consumidores",
            },
            actions: {
                purge: "Purgar cola",
            },
            purge: {
                title: "Purgar cola",
                confirmation: "¿Estás seguro que quieres eliminar todos los mensajes de la cola {{name}}?",
            },
        },
        message: {
            title: "Mensajes",
            error: {
                deleteFailed: "Error al eliminar el mensaje",
            },
            detail: {
                title: "Detalles del mensaje",
                messageID: "ID del mensaje",
                queueName: "Nombre de la cola",
                timestamp: "Fecha y hora",
                type: "Tipo",
                durable: "Durable",
                priority: "Prioridad",
                size: "Tamaño",
                redelivered: "Reentregado",
                deliveryCount: "Número de entregas",
                expirationTime: "Fecha de expiración",
                properties: "Propiedades",
                content: "Contenido",
            },
            grid: {
                messageID: "ID del mensaje",
                timestamp: "Fecha y hora",
                type: "Tipo",
                priority: "Prioridad",
                size: "Tamaño",
                actions: "Acciones",
            },
        },
        parametres: {
            title: "Parámetros",
            detail: {
                title: "Detalles del parámetro",
                grup: "Grupo",
                subGrup: "Subgrupo",
                tipus: "Tipo",
                codi: "Código",
                nom: "Nombre",
                descripcio: "Descripción",
                valor: "Valor",
                tipusEnum: {
                    NUMERIC: "Numérico",
                    TEXT: "Texto",
                    BOOLEAN: "Verdadero o falso",
                    PASSWORD: "Contraseña",
                    CRON: "Cron",
                    SELECT: "Selección",
                },
                valuesTootip: {
                    true: "Verdadero",
                    false: "Falso",
                    null: "Indefinido",
                },
            },
        },
        notFound: "No encontrado",
    },
    generic: {
        tipus: "Tipo",
        periode: "Periodo",
    },
    errors: {
        camp: {
            obligatori: "Campo obligatorio",
        },
    },
    components: {
        clear: "Limpiar",
        search: "Buscar",
        copiarContingut: "Copiar contenido",
        copiarContingutTitle: "Copiar el contenido",
        copiarContingutSuccess: "Contenido copiado al portapapeles",
    },
    form: {
        field: {
            file: {
                edit: 'Editar',
                download: 'Descargar',
                clear: 'Borrar',
            },
        },
    },
    calendari: {
        seleccionar_entorn_app: "Seleccionar entorno de aplicación",
        seleccionar: "Seleccionar",
        seleccionar_entorn_app_primer: "Seleccione primero un entorno de aplicación",
        carregar_interval: "Cargar datos por intervalo",
        carregar_mes_actual: "Cargar datos del mes actual",
        today: "Today",
        data_inici: "Fecha inicio",
        data_fi: "Fecha fin",
        cancelar: "Cancelar",
        carregar: "Cargar",
        sense_dades: "No hay datos estadísticos",
        dades_buides: "La aplicación no dispone de datos estadísticos",
        dades_disponibles: "Datos disponibles",
        dades_disponibles_tooltip: "Clica para ver los datos disponibles",
        obtenir_dades: "Obtener datos",
        obtenir_dades_tooltip: "Haz clic para obtener los datos estadísticos del día",
        error_dades: "Error al obtener datos",
        error_dades_tooltip: "Se ha producido un error al intentar obtener los datos estadísticos de la aplicación",
        error_titol: "Error al obtener datos estadísticos",
        data: "Fecha",
        missatge: "Mensaje",
        traca: "Traza",
        tancar: "Cerrar",
        success_obtenir_dades: "Los datos estadísticos se han obtenido correctamente",
        error_obtenir_dades: "Error al obtener datos estadísticos",
        success_carregar_interval: "Los datos estadísticos se han obtenido correctamente",
        error_carregar_interval: "Error al cargar datos por intervalo",
        error_dades_disponibles: "Error al obtener los dias con datos estadísticos disponibles",
        error_dades_dia: "Error al obtener los datos estadísticos del dia",
        error_obtenir_dates_mes: "No se han podido determinar las fechas del mes actual",
        carregant: "Cargando",
        carregant_dades: "Cargando datos estadísticos...",
        modal_dades_dia: "Datos disponibles del día",
        dimensions: "Dimensiones",
        indicadors: "Indicadores",
    },
    treeData: {
        treeView: "Vista en árbol"
    },
};

export default translationEs;
