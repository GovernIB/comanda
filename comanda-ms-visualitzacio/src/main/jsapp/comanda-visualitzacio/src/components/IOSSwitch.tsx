import Switch, { SwitchProps } from '@mui/material/Switch';
import { styled } from '@mui/material/styles';

/** Switch estilitzat a l'estil iOS (recepta estàndard de MUI). */
const IOSSwitch = styled(({ slotProps, ...props }: SwitchProps) => (
    <Switch
        focusVisibleClassName=".Mui-focusVisible"
        disableRipple
        {...props}
        // MUI (a diferència de l'antic inputProps) no fa merge del role intern amb el slotProps.input
        // que es passi des de fora: si el crider només vol afegir un aria-label, cal preservar el role
        // "switch" explícitament perquè no es perdi l'accessibilitat del control.
        slotProps={{ ...slotProps, input: { role: 'switch', ...slotProps?.input } }}
    />
))(({ theme }) => ({
    width: 42,
    height: 26,
    padding: 0,
    '& .MuiSwitch-switchBase': {
        padding: 0,
        margin: 2,
        transitionDuration: '150ms',
        '&.Mui-checked': {
            transform: 'translateX(16px)',
            color: '#fff',
            '& + .MuiSwitch-track': {
                backgroundColor: theme.palette.mode === 'dark' ? '#2ECA45' : '#65C466',
                opacity: 1,
                border: 0,
            },
        },
    },
    '& .MuiSwitch-thumb': {
        boxSizing: 'border-box',
        width: 22,
        height: 22,
    },
    '& .MuiSwitch-track': {
        borderRadius: 26 / 2,
        backgroundColor: theme.palette.mode === 'dark' ? '#39393D' : '#E9E9EA',
        opacity: 1,
        transition: theme.transitions.create(['background-color'], { duration: 500 }),
    },
}));

export default IOSSwitch;
