import * as React from "react";
import {useEffect, useMemo, useState} from "react";
import {useTranslation} from "react-i18next";
import {
    FormField,
    MuiDialog,
    useBaseAppContext,
    useResourceApiService,
} from "reactlib";
import {Grid} from "@mui/material";
import {SimpleTreeView, TreeItem} from "@mui/x-tree-view";
import Icon from "@mui/material/Icon";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import IconButton from "@mui/material/IconButton";
import Badge from "@mui/material/Badge";
import {useAclCustomPermissionManager} from "./AclPermissionManager.tsx";

const UoAclEntryForm: React.FC = () => {
    const { t } = useTranslation();
    return <Grid container spacing={2}>
        <Grid size={4}>
            <FormField name="subjectType" />
        </Grid>
        <Grid size={8}>
            <FormField name="subjectValue" />
        </Grid>
        <Grid size={12}>
            <FormField name="perm0Allowed" label={t($ => $.page.unitatOrganitzativa.acl.perm0Allowed)} />
        </Grid>
    </Grid>;
}

const EntitatOrganigrama = ({entitatId, codi, reloadToken, permisos}:{entitatId:any, codi:string, reloadToken:number, permisos?: (id:any, row:any) => void}) => {
    const { t } = useTranslation();
    const [unitats, setUnitats] = useState<any[] | undefined>();
    const [loading, setLoading] = useState<boolean>(true);

    const {isReady: apiIsReady, artifactAction: apiAction} = useResourceApiService('entitat');
    const {temporalMessageShow} = useBaseAppContext();

    useEffect(() => {
        if (apiIsReady && entitatId != null) {
            setLoading(true);
            // L'estructura ve cachejada al backend (només canvia amb una sincronització Dir3); el nombre de
            // permisos de cada node es calcula sempre en viu.
            apiAction(entitatId, {code: 'ORGANIGRAMA'})
                .then((rows: any[]) => {
                    if (rows.length > 0) {
                        setUnitats(rows)
                    } else {
                        temporalMessageShow(null, t($ => $.page.entitats.action.organigrama.ko), 'error')
                    }
                })
                .catch(error => temporalMessageShow(null, error.message, 'error'))
                .finally(() => setLoading(false))
        }
    }, [apiIsReady, entitatId, apiAction, reloadToken]);

    // Índex pare->fills en un únic recorregut, en lloc de filtrar tota la llista per cada node (O(n) enlloc de O(n^2)).
    const arrel = useMemo(() => {
        if (!unitats) return undefined;
        const fillsPerPare = new Map<string, any[]>();
        unitats.forEach(u => {
            const pare = u.codiUnitatSuperior;
            if (!fillsPerPare.has(pare)) fillsPerPare.set(pare, []);
            fillsPerPare.get(pare)!.push(u);
        });
        const construeix = (node: any): any => ({
            ...node,
            children: (fillsPerPare.get(node.codi) ?? []).map(construeix),
        });
        const node = unitats.find(u => u.codi === codi);
        return node && construeix(node);
    }, [unitats, codi]);

    const renderTree = (node:any) => (
        <TreeItem key={node.codi} itemId={node.codi} label={
            <Box display={'flex'} alignItems={'center'} gap={1}>
                <Icon>{node.codi == codi ?'home' :'folder'}</Icon>
                {node.codiNom}

                {permisos != null &&
                    <IconButton title={t($ => $.components.permisos.title)}
                                sx={{ ml: 'auto' }}
                                onClick={(ev) => {
                                    ev.stopPropagation()
                                    permisos?.(node.id, node)
                                }}>
                        <Badge badgeContent={node.numPermisos} color={'primary'}>
                            <Icon>lock</Icon>
                        </Badge>
                    </IconButton>
                }
            </Box>
        }>
            {Array.isArray(node.children) ? node.children.map((node:any) => renderTree(node)) : null}
        </TreeItem>
    );

    // Només bloquejam amb un spinner la càrrega inicial. Si ja hi ha dades (p.ex. es torna a demanar l'organigrama
    // per actualitzar el nombre de permisos), l'arbre es manté muntat perquè no es perdi l'estat dels nodes oberts.
    if (loading && !unitats) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress/>
        </Box>
    }

    return <>
        <SimpleTreeView defaultExpandedItems={[codi]} sx={{ '& .MuiTreeItem-label': { fontSize: 14 } }}>
            {arrel && renderTree(arrel)}
        </SimpleTreeView>
    </>
}

/**
 * Diàleg reutilitzable per mostrar l'organigrama d'unitats organitzatives d'una Entitat (identificada pel seu id
 * i codiDir3), amb gestió de permisos per node. Usat des de la pantalla d'Entitats i des de la pantalla de valors
 * de dimensió (per a dimensions de tipus ENTITAT).
 */
export const useOrganigramaDialog = () => {
    const { t } = useTranslation();
    const [open, setOpen] = useState<boolean>(false);
    const [entitat, setEntitat] = useState<{ id:any, codi:string }>();
    // Cada cop que es modifiquen els permisos d'un node de l'organigrama, cal tornar a demanar les dades perquè
    // el comptador de permisos es recalcula en viu al backend (l'estructura ve de la cache del servidor).
    const [reloadToken, setReloadToken] = useState(0);

    const uoAclColumns = useMemo(() => [{
        field: 'subjectType',
        sortable: false,
        flex: 2
    }, {
        field: 'subjectValue',
        sortable: false,
        flex: 4
    }, {
        field: 'perm0Allowed',
        headerName: t($ => $.page.unitatOrganitzativa.acl.perm0Allowed),
        sortable: false,
        flex: 1
    }], [t]);

    const {
        show: permissionShow,
        component: permissionComponent
    } = useAclCustomPermissionManager({
        resourceType: 'UNITAT',
        columns: uoAclColumns,
        formContent: <UoAclEntryForm/>,
        onEntryChanged: () => setReloadToken(token => token + 1),
    });

    const handleOpen = (id:any, codi:string) => {
        setEntitat({ id, codi });
        setOpen(true);
    };

    const handleClose = () => {
        setEntitat(undefined);
        setOpen(false);
    };

    const dialog = (<>
        <MuiDialog
            open={open}
            closeCallback={handleClose}
            title={t($ => $.page.entitats.action.organigrama.title)}
            componentProps={{ fullWidth: true, maxWidth: 'lg' }}
        >
            {entitat && <EntitatOrganigrama entitatId={entitat.id} codi={entitat.codi} reloadToken={reloadToken} permisos={(id, row) => permissionShow(id, row?.codiNom)}/>}
        </MuiDialog>
        {permissionComponent}
    </>);

    return {
        handleOpen,
        handleClose,
        dialog,
    };
}
