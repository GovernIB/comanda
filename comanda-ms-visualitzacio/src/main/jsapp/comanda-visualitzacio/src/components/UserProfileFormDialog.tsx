import React from 'react';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import {Icon} from '@mui/material';
import ListItemText from '@mui/material/ListItemText';
import {FormField, MuiFormDialog} from 'reactlib';
import {DataFormDialogApi} from '../../lib/components/mui/datacommon/DataFormDialog.tsx';
import {useTranslation} from "react-i18next";
import {UserProfileCardData} from "./UserProfileCardData.tsx";
import {
    AlternateEmail,
    AssignmentInd, FormatListNumbered,
    Language,
    Mail,
    ManageAccounts,
    Person,
    RecentActors,
    Tag
} from "@mui/icons-material";
import Badge from "@mui/material/Badge";
import Grid from "@mui/material/Grid";
import {UsuariModel} from "../types/usuari.model.tsx";
import i18n from "../i18n/i18n.ts";

export const UserProfileFormDialogButton = ({ onClick }: { onClick: () => void}) => {
    const { t } = useTranslation();
    return (
        <MenuItem
            onClick={() => {
                onClick();
            }}
        >
            <ListItemIcon>
                <Icon fontSize={'small'}>person</Icon>
            </ListItemIcon>
            <ListItemText>{t('menu.user.options.profile.title')}</ListItemText>
        </MenuItem>
    );
}

export const UserProfileFormDialog = ({ dialogApiRef }: { dialogApiRef: React.MutableRefObject<DataFormDialogApi | undefined> }) => {
    const { t } = useTranslation();
    const {COLOR, BACKGROUND} = {COLOR: '#474747', BACKGROUND: '#f8f8f8'};
    return (
        <MuiFormDialog
            resourceName="usuari"
            title={t('menu.user.options.profile.title')}
            onClose={(reason?: string) => reason !== 'backdropClick'}
            apiRef={dialogApiRef}
            dialogComponentProps={{ fullWidth: true, maxWidth: 'lg' }}
            formComponentProps={{ onSaveSuccess: (data: UsuariModel) => {i18n.changeLanguage(data.idioma.toLowerCase())}}}
        >
            <UserProfileCardData
                icon={<AssignmentInd/>}
                title={t('menu.user.options.profile.form.userData')}
                cardProps={{border: '1px solid #004B99'}}
                headerProps={{color: 'white', backgroundColor: '#3f96f6'}}
            >
                <Grid size={{xs: 12, sm: 4, md: 3, lg: 2}}>
                    <FormField name={UsuariModel.CODI} readOnly={true} componentProps={{
                        slotProps: {
                            input: {
                                style: { color: COLOR, backgroundColor: BACKGROUND },
                                startAdornment: (<Tag/>),
                            },
                        },
                    }}/>
                </Grid>
                <Grid size={{xs: 12, sm: 8, md: 6, lg: 4}}>
                    <FormField name={UsuariModel.NOM} readOnly={true} componentProps={{
                        slotProps: {
                            input: {
                                style: { color: COLOR, backgroundColor: BACKGROUND },
                                startAdornment: (<Person/>),
                            },
                        },
                    }}/>
                </Grid>
                <Grid size={{xs: 12, sm: 4, md: 3, lg: 2}}>
                    <FormField name={UsuariModel.NIF} readOnly={true} componentProps={{
                        slotProps: {
                            input: {
                                style: { color: COLOR, backgroundColor: BACKGROUND },
                                startAdornment: (<Badge/>),
                            },
                        },
                    }} />
                </Grid>
                <Grid size={{xs: 12, sm: 8, md: 9, lg: 4}}>
                    <FormField name={UsuariModel.EMAIL} readOnly={true} componentProps={{
                        slotProps: {
                            input: {
                                style: { color: COLOR, backgroundColor: BACKGROUND },
                                startAdornment: (<AlternateEmail/>),
                            },
                        },
                    }}/>
                </Grid>
                <Grid size={{xs: 12, sm: 12, md: 12, lg: 12}}>
                    <FormField name={UsuariModel.ROLS} readOnly={true} componentProps={{
                        slotProps: {
                            input: {
                                style: { color: COLOR, backgroundColor: BACKGROUND },
                                startAdornment: (<RecentActors/>),
                            },
                        },
                    }}/>
                </Grid>

                <Grid size={{xs: 12, sm: 6, md: 6, lg: 4}}>
                    <FormField name={UsuariModel.EMAIL_ALTERNATIU} componentProps={{
                        slotProps: {
                            input: {
                                startAdornment: (<Mail/>),
                            },
                        },
                    }}/>
                </Grid>
                <Grid size={{xs: 12, sm: 6, md: 6, lg: 4}}>
                    <FormField name={UsuariModel.IDIOMA} componentProps={{
                        slotProps: {
                            input: {
                                startAdornment: (<Language/>),
                            },
                        },
                    }}/>
                </Grid>
            </UserProfileCardData>

            <UserProfileCardData
                icon={<ManageAccounts/>}
                title={t('menu.user.options.profile.form.genericConfig')}
                cardProps={{border: '1px solid #004B99', marginTop: '1rem'}}
                headerProps={{color: 'white', backgroundColor: '#9ea4ad'}}
            >
                <Grid size={{xs: 12, sm: 6, md: 6, lg: 4}}>
                    <FormField name={UsuariModel.NUM_ELEMENTS_PAGINA} componentProps={{
                        slotProps: {
                            input: {
                                startAdornment: (<FormatListNumbered/>),
                            },
                        },
                    }}/>
                </Grid>
            </UserProfileCardData>
        </MuiFormDialog>
    );
}
