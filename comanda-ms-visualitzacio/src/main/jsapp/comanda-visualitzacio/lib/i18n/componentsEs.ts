const componentsEs = {
    app: {
        menu: {
            home: 'Inicio',
        },
        auth: {
            logout: 'Cerrar sesión',
        },
        error: {
            '404': 'No encontrado',
            '503': 'Sin connexión',
        },
        offline: {
            message: 'Sin conexión con el servidor',
        },
    },
    datacommon: {
        details: {
            label: 'Detalles',
        },
        create: {
            label: 'Crear',
        },
        update: {
            label: 'Modificar',
        },
        delete: {
            label: 'Borrar',
            single: {
                label: 'Confirmación',
                confirm:
                    '¿Está seguro de que desea borrar este elemento (esta acción no se puede deshacer)?',
                success: 'Elemento borrado',
                error: 'Error borrando elemento',
            },
        },
        export: {
            label: 'Exportar',
        },
        refresh: {
            label: 'Refrescar',
        },
        toolbar: {
            error: 'Se ha producido un error',
        },
        findDisabled: 'Sin consultar',
        noRows: 'Sin datos',
        error: 'Error',
    },
    grid: {
        selection: {
            one: '1 selected row ',
            multiple: '{{count}} selected rows',
        },
        pageInfo: '{{from}} a {{to}} de {{count}}',
    },
    form: {
        goBack: {
            title: 'Ir atrás',
        },
        revert: {
            title: 'Deshacer cambios',
            confirm: '¿Está seguro de que desea deshacer los cambios hechos al formulario?',
        },
        create: {
            title: 'Crear',
            success: 'Elemento creado',
            error: 'Error creando elemento',
        },
        update: {
            title: 'Modificar',
            success: 'Elemento modificado',
            error: 'Error modificando elemento',
            wrong_resource_type:
                'No es posible guardar los formularios con tipo de recurso "{{resourceType}}"',
        },
        delete: {
            title: 'Borrar',
            confirm:
                '¿Está seguro de que desea borrar este elemento (esta acción no se puede deshacer)?',
            success: 'Elemento borrado',
            error: 'Error borrando elemento',
        },
        field: {
            enum: {
                clear: 'Borrar',
                noOptions: 'Sin opciones',
            },
            reference: {
                open: 'Abrir',
                close: 'Cerrar',
                clear: 'Borrar',
                loading: 'Cargando...',
                noOptions: 'Sin opciones',
                page: 'Mostrando {{size}} de {{totalElements}} elementos',
                advanced: {
                    title: 'Seleccionar valor',
                },
            },
            checkboxSelect: {
                true: 'Si',
                false: 'No',
            },
        },
        dialog: {
            create: 'Crear',
            update: 'Modificar',
        },
        validate: {
            error: 'Error de validación',
            saveErrors: 'Hay errores de validación',
        },
    },
    actionreport: {
        action: {
            confirm: {
                title: 'Confirmación',
                message: '¿Está seguro de que desea ejecutar la acción {{action}}?',
            },
            error: 'Error ejecutando la acción',
        },
        report: {
            error: 'Error generando el informe',
        },
    },
    buttons: {
        answerRequired: {
            accept: 'Aceptar',
            cancel: 'Cancelar',
        },
        confirm: {
            accept: 'Aceptar',
            cancel: 'Cancelar',
        },
        form: {
            save: 'Guardar',
            cancel: 'Cancelar',
        },
        action: {
            exec: 'Ejecutar',
            cancel: 'Cancelar',
        },
        report: {
            generate: 'Generar',
            cancel: 'Cancelar',
        },
        misc: {
            close: 'Cerrar',
            retry: 'Reintentar',
        },
    },
};

export default componentsEs;
