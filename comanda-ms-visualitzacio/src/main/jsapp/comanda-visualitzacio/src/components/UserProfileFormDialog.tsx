import React from 'react';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import { Icon } from '@mui/material';
import ListItemText from '@mui/material/ListItemText';
import { FormField, MuiFormDialog } from 'reactlib';
import { DataFormDialogApi } from '../../lib/components/mui/datacommon/DataFormDialog.tsx';
import {useTranslation} from "react-i18next";

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
            <ListItemText>{t('menu.user.options.profile')}</ListItemText>
        </MenuItem>
    );
}

export const UserProfileFormDialog = ({ dialogApiRef }: { dialogApiRef: React.MutableRefObject<DataFormDialogApi | undefined> }) => {
    const { t } = useTranslation();
    return (
        <MuiFormDialog
            resourceName="usuari"
            title={t('menu.user.options.profile')}
            onClose={(reason?: string) => reason !== 'backdropClick'}
            apiRef={dialogApiRef}
            dialogComponentProps={{ fullWidth: true, maxWidth: 'lg' }}
        >
            <FormField name="codi" />
            <FormField name="nom" />
            <FormField name="nif" />
            <FormField name="email" />
        </MuiFormDialog>
    );
}
