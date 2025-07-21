const translationEs = {
    menu: {
        configuracio: "Configuración",
        estadistiques: "Estadísticas",
        salut: "Salud",
        app: "Aplicaciones",
        entorn: "Entornos",
        monitor: "Monitor",
        versionsEntorn: "Versiones por entorno",
        widget: "Widgets estadísticos",
        dashboard: "Cuadros de control",
        cache: "Gestión de cachés",
        integracio: "Integraciones",
        broker: "Gestor de colas",
    },
    page: {
        salut: {
            title: "Salut",
            refrescar: "Refrescar",
            nd: "N/D",
            apps: {
                column: {
                    estat: "Estado",
                    codi: "Código",
                    nom: "Nombre",
                    versio: "Versión",
                    bd: "Base de datos",
                    latencia: "Latencia",
                    integ: "Integraciones",
                    subsis: "Subsistemas",
                    msgs: "Mensajes",
                },
                detalls: "Detalles",
            },
            refreshperiod: {
                PT1M: "1 minuto",
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
                data: "Última actualización",
                bdEstat: "Base de datos",
                appLatencia: "Latencia",
                missatges: "Mensajes",
                detalls: "Detalles",
            },
            latencia: {
                title: "Latencia",
            },
            estatLatencia: {
                title: "Estado y latencia",
            },
            integracions: {
                title: "Integraciones",
                column: {
                    codi: "Código",
                    nom: "Nombre",
                    estat: "Estado",
                    latencia: "Latencia",
                    peticions: "Peticiones",
                },
            },
            subsistemes: {
                title: "Subsistemas",
                column: {
                    codi: "Código",
                    nom: "Nombre",
                    estat: "Estado",
                    latencia: "Latencia",
                },
            },
            estats: {
                title: "Estados",
            },
        },
        apps: {
            title: "Aplicaciones",
            create: "Crear aplicación",
            update: "Modificar aplicación",
            general: "General",
            entornApp: "Entornos de la aplicación",
            logo: "Logo",
            uploadLogo: "Subir logo",
            changeLogo: "Cambiar logo",
            removeLogo: "Eliminar logo",
        },
        entorns: {
            title: "Entornos",
        },
        appsEntorns: {
            title: "Entornos",
            resourceTitle: "entorno",
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
            },
            modulEnum: {
                salut: "Health",
                estadistica: "Statistics",
            },
        },
        widget: {
            title: "Widgets estadísticos",
            grid: {
                position: "Posición",
                size: "Tamaño",
            },
            form: {
                periode: "Period",
                simple: "Widget simple",
                grafic: "Widget gráfico",
                taula: "Widget tabla",
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
            },
            taula: {
                tab: {
                    title: "Tabla",
                },
                title: "Widgets estadísticos tipo tabla",
                resourceTitle: "widget tipo tabla",
                columna: {
                    indicador: "Indicador",
                    titolIndicador: "Título",
                    tipusIndicador: "Tipo agrupación",
                    periodeIndicador: "Período agr.",
                    accions: "Acciones",
                    arrossega: "Arrastra para reordenar",
                },
            },
            action: {
                add: {
                    label: "Añadir",
                },
            },
        },
        dashboards: {
            title: "Cuadros de control",
            edit: "Editar",
            dashboardView: "Ir al panel de control",
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
                }
            },
            alert: {
                tornarLlistat: "Volver al listado",
                tornarTauler: "Volver al tablero por defecto",
                notExists: "El panel de control no existe.",
                notDefined: "No hay ningún panel de control definido.",
                carregar: "Error al cargar el panel de control.",
            }
        },
        caches: {
            title: "Caches",
            columna: {
                codi: "Código",
                nom: "Nombre",
                entrades: "Núm. elementos",
                mida: "Tamaño (bytes)",
            },
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
};

export default translationEs;
