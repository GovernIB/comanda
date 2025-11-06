import React from 'react';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import { Icon, InputAdornment, ToggleButton, ToggleButtonGroup, Divider } from '@mui/material';
import ListItemText from '@mui/material/ListItemText';
import { FormField, MuiFormDialog, useFormContext } from 'reactlib';
import { DataFormDialogApi } from '../../lib/components/mui/datacommon/DataFormDialog.tsx';
import { useTranslation } from 'react-i18next';
import {
    AlternateEmail,
    Badge,
    FormatListNumbered,
    Language,
    Mail,
    Person,
    RecentActors,
    Tag,
} from '@mui/icons-material';
import Grid from '@mui/material/Grid';
import { UsuariModel } from '../types/usuari.model.tsx';
import { useUserContext } from './UserContext';

export const UserProfileFormDialogButton = ({ onClick }: { onClick: () => void }) => {
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
            <ListItemText>{t($ => $.menu.user.options.profile.title)}</ListItemText>
        </MenuItem>
    );
};

export const TemaObscurSelector: React.FC = () => {
  const { t } = useTranslation();
  const { data, apiRef } = useFormContext();
  const handleChange = (_event: any, newValue: boolean | null) => {
    if (newValue !== null) {
        apiRef?.current?.setFieldValue("temaObscur", newValue);
    }
  };
  return (
      <ToggleButtonGroup
        value={data?.temaObscur}
        exclusive
        onChange={handleChange}
        size="small"
        sx={{
          display: 'flex',
          width: '100%',
          justifyContent: 'center',
        }}
      >
          <ToggleButton value={false} sx={{ flex: 1, gap: 1 }} >
            <Icon>light_mode</Icon> {t($ => $.menu.user.options.profile.tema.clar)}
          </ToggleButton>
          <ToggleButton value={true} sx={{ flex: 1, gap: 1 }} >
            <Icon>dark_mode</Icon> {t($ => $.menu.user.options.profile.tema.obscur)}
          </ToggleButton>
      </ToggleButtonGroup>
  );
};


export const UserProfileFormDialog = ({
    dialogApiRef,
}: {
    dialogApiRef: React.MutableRefObject<DataFormDialogApi | undefined>;
}) => {
    const { t } = useTranslation();
    const { setUser } = useUserContext();

    return (
        <MuiFormDialog
            resourceName="usuari"
            title={t($ => $.menu.user.options.profile.title)}
            onClose={(reason?: string) => reason !== 'backdropClick'}
            apiRef={dialogApiRef}
            dialogComponentProps={{ fullWidth: true, maxWidth: 'lg', sx: { p: 0, }, }}
            formComponentProps={{ onSaveSuccess: setUser, sx: { p: 0, }, }}
        >
            <Grid container spacing={1} p={1} sx={{ maxWidth: "100%" }}>
                <Grid size={{ xs: 12 }}>
                    <Divider sx={{ mb: 2 }}>
                        {t($ => $.menu.user.options.profile.form.userData)}
                    </Divider>
                </Grid>
                <Grid size={{ xs: 12, sm: 4, md: 4, lg: 2 }}>
                    <FormField
                        name={UsuariModel.CODI}
                        readOnly={true}
                        disabled
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end">
                                <Tag />
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 8, md: 8, lg: 4 }}>
                    <FormField
                        name={UsuariModel.NOM}
                        readOnly={true}
                        disabled
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end">
                                <Person />
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 4, md: 4, lg: 2 }}>
                    <FormField
                        name={UsuariModel.NIF}
                        readOnly={true}
                        disabled
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end">
                                <Badge />
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 8, md: 8, lg: 4 }}>
                    <FormField
                        name={UsuariModel.EMAIL}
                        readOnly={true}
                        disabled
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end">
                                <AlternateEmail />
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 12, md: 12, lg: 12 }}>
                    <FormField
                        name={UsuariModel.ROLS}
                        readOnly={true}
                        disabled
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end">
                                <RecentActors />
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 8, md: 6, lg: 4 }}>
                    <FormField
                        name={UsuariModel.EMAIL_ALTERNATIU}
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end">
                                <Mail />
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12 }}>
                    <Divider sx={{ my: 2 }}>
                        {t($ => $.menu.user.options.profile.form.genericConfig)}
                    </Divider>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 6, lg: 4 }}>
                    <FormField
                        name={UsuariModel.NUM_ELEMENTS_PAGINA}
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end" sx={{ mr: 2 }}>
                                <FormatListNumbered/>
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 6, lg: 4 }}>
                    <FormField
                        name={UsuariModel.IDIOMA}
                        componentProps={{ slotProps: { input: { endAdornment: (
                            <InputAdornment position="end" sx={{ mr: 2 }}>
                                <Language/>
                            </InputAdornment>
                        ), }, } }}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 6, lg: 4 }}>
                    <TemaObscurSelector />
                </Grid>
            </Grid>
        </MuiFormDialog>
    );
};
