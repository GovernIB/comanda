import React from 'react';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import { Icon, InputAdornment, ToggleButton, ToggleButtonGroup, Divider } from '@mui/material';
import ListItemText from '@mui/material/ListItemText';
import { FormField, MuiFormDialog, useFormContext } from 'reactlib';
import { DataFormDialogApi } from '../../lib/components/mui/datacommon/DataFormDialog.tsx';
import { useTranslation } from 'react-i18next';
import AlternateEmail from '@mui/icons-material/AlternateEmail';
import FormatListNumbered from '@mui/icons-material/FormatListNumbered';
import Language from '@mui/icons-material/Language';
import Mail from '@mui/icons-material/Mail';
import Person from '@mui/icons-material/Person';
import RecentActors from '@mui/icons-material/RecentActors';
import Tag from '@mui/icons-material/Tag';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import { MenuEstil, UsuariModel } from '../types/usuari.model.tsx';
import { useUserContext } from './UserContext';

const selectorLabelSx = {
    display: 'block',
    ml: 1.75,
    mb: 0.75,
    fontSize: '0.75rem',
    lineHeight: 1,
    color: 'text.secondary',
};

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
    const handleChange = (_event: React.MouseEvent<HTMLElement>, newValue: boolean | null) => {
        if (newValue !== null) {
            apiRef?.current?.setFieldValue('temaObscur', newValue);
        }
    };
    return (
        <>
            <Typography component="label" sx={selectorLabelSx}>
                {t($ => $.menu.user.options.profile.form.applicationTheme)}
            </Typography>
            <ToggleButtonGroup
                value={data?.temaObscur ?? ""}
                exclusive
                onChange={handleChange}
                size="small"
                sx={{
                    display: 'flex',
                    width: '100%',
                    justifyContent: 'center',
                }}
            >
                <ToggleButton value={false} sx={{ flex: 1, gap: 1 }}>
                    <Icon>light_mode</Icon> {t($ => $.menu.user.options.profile.tema.clar)}
                </ToggleButton>
                <ToggleButton value={true} sx={{ flex: 1, gap: 1 }}>
                    <Icon>dark_mode</Icon> {t($ => $.menu.user.options.profile.tema.obscur)}
                </ToggleButton>
                <ToggleButton value={""} sx={{ flex: 1, gap: 1 }}>
                    <Icon>settings_brightness</Icon> {t($ => $.menu.user.options.profile.tema.sistema)}
                </ToggleButton>
            </ToggleButtonGroup>
        </>
    );
};

export const EstilMenuSelector: React.FC = () => {
    const { t } = useTranslation();
    const { data, apiRef } = useFormContext();
    const handleChange = (_event: React.MouseEvent<HTMLElement>, newValue: MenuEstil | null) => {
        if (newValue !== null) {
            apiRef?.current?.setFieldValue(UsuariModel.ESTIL_MENU, newValue);
        }
    };
    return (
        <>
            <Typography component="label" sx={selectorLabelSx}>
                {t($ => $.menu.user.options.profile.form.menuTheme)}
            </Typography>
            <ToggleButtonGroup
                value={data?.estilMenu ?? MenuEstil.TEMA}
                exclusive
                onChange={handleChange}
                size="small"
                sx={{
                    display: 'flex',
                    width: '100%',
                    justifyContent: 'center',
                }}
            >
                <ToggleButton value={MenuEstil.TEMA} sx={{ flex: 1, gap: 1 }}>
                    <Icon>palette</Icon> {t($ => $.menu.user.options.profile.estilMenu.tema)}
                </ToggleButton>
                <ToggleButton value={MenuEstil.TEMA_INVERTIT} sx={{ flex: 1, gap: 1 }}>
                    <Icon>invert_colors</Icon> {t($ => $.menu.user.options.profile.estilMenu.temaInvertit)}
                </ToggleButton>
                <ToggleButton value={MenuEstil.PEU} sx={{ flex: 1, gap: 1 }}>
                    <Icon>vertical_align_bottom</Icon> {t($ => $.menu.user.options.profile.estilMenu.peu)}
                </ToggleButton>
            </ToggleButtonGroup>
        </>
    );
};

const UserProfileForm = () => {
    const { t } = useTranslation();
    const { data } = useFormContext();

    return <Grid container spacing={1} sx={{ px: 1, }}>
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
        <Grid size={{ xs: 12, sm: 8, md: 8, lg: 5 }}>
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
        <Grid size={{ xs: 12, sm: 12, md: 12, lg: 5 }}>
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
        <Grid size={{ xs: 12, sm: 12, md: 6, lg: 4 }}>
            <FormField
                name={UsuariModel.EMAIL_ALTERNATIU}
                componentProps={{ slotProps: { input: { endAdornment: (
                    <InputAdornment position="end">
                        <Mail />
                    </InputAdornment>
                ), }, } }}
            />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3, lg: 4 }}>
            <FormField name={UsuariModel.ALARMA_MAIL} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3, lg: 4 }}>
            <FormField name={UsuariModel.ALARMA_MAIL_AGRUPAT} disabled={!data[UsuariModel.ALARMA_MAIL]} />
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
        <Grid size={{ xs: 12, sm: 12, md: 8, lg: 6 }}>
            <TemaObscurSelector />
        </Grid>
        <Grid size={{ xs: 12, sm: 12, md:8, lg: 6 }}>
            <EstilMenuSelector />
        </Grid>
    </Grid>
}


export const UserProfileFormDialog = ({
    dialogApiRef,
}: {
    dialogApiRef: React.RefObject<DataFormDialogApi | undefined>;
}) => {
    const { t } = useTranslation();
    const { refresh } = useUserContext();

    return (
        <MuiFormDialog
            resourceName="usuari"
            title={t($ => $.menu.user.options.profile.title)}
            onClose={(reason?: string) => reason !== 'backdropClick'}
            apiRef={dialogApiRef}
            dialogComponentProps={{ fullWidth: true, maxWidth: 'lg', }}
            formComponentProps={{
                onSaveSuccess: () => refresh(),
                componentProps: { sx: { mt: 0 } },
            }}
        >
            <UserProfileForm/>
        </MuiFormDialog>
    );
};
