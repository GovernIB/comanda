import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import { GridPage, useDebounce } from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';
import { ParametresGrups } from './parametres/ParametresGrups';
import { ParametresItems } from './parametres/ParametresItems';

const ParametresQuickFilter: React.FC<{
    onChange: (value: string) => void;
}> = ({ onChange }) => {
    const { t } = useTranslation();
    const [value, setValue] = React.useState('');
    const debounced = useDebounce(value);
    React.useEffect(() => { onChange(debounced); }, [debounced, onChange]);
    return (
        <TextField
            value={value}
            onChange={(e) => setValue(e.target.value)}
            label={t($ => $.page.parametres.find)}
            variant="outlined"
            fullWidth
            size="small"
            slotProps={{
                input: {
                    startAdornment: (
                        <InputAdornment position="start">
                            <Icon fontSize="small">search</Icon>
                        </InputAdornment>
                    ),
                    endAdornment: value && (
                        <InputAdornment position="end">
                            <IconButton size="small" onClick={() => setValue('')}>
                                <Icon fontSize="inherit">clear</Icon>
                            </IconButton>
                        </InputAdornment>
                    ),
                },
            }}
        />
    );
};

const Parametres: React.FC = () => {
    const { t } = useTranslation();
    const [quickFilter, setQuickFilter] = React.useState('');
    const [selectedGrup, setSelectedGrup] = React.useState<string | null>(null);
    const [selectedSubGrup, setSelectedSubGrup] = React.useState<string | null>(null);

    const handleGroupChange = React.useCallback((grup: string | null, subGrup: string | null) => {
        setSelectedGrup(grup);
        setSelectedSubGrup(subGrup);
    }, []);

    return (
        <GridPage disableMargins>
            <PageTitle title={t($ => $.page.parametres.title)} />
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    height: '100%',
                    overflow: 'hidden',
                }}
            >
                <Box
                    sx={{
                        p: 2,
                        px: { xs: 2, md: 10 },
                        flexShrink: 0,
                        bgcolor: 'background.paper',
                        borderBottom: '1px solid',
                        borderColor: 'divider',
                    }}
                >
                    <ParametresQuickFilter onChange={setQuickFilter} />
                </Box>

                <Box sx={{ display: 'flex', flexGrow: 1, overflow: 'hidden' }}>
                    <Box
                        sx={{
                            width: { xs: '180px', md: '250px', lg: '350px' },
                            flexShrink: 0,
                            height: '100%',
                            borderRight: '1px solid',
                            borderColor: 'divider',
                            overflowY: 'auto',
                            p: 1.5,
                        }}
                    >
                        <ParametresGrups quickFilter={quickFilter} onChange={handleGroupChange} />
                    </Box>

                    <Box
                        sx={{
                            flexGrow: 1,
                            overflowY: 'auto',
                            py: 2,
                            bgcolor: 'grey.50',
                        }}
                    >
                        <ParametresItems
                            grup={selectedGrup}
                            subGrup={selectedSubGrup}
                            quickFilter={quickFilter}
                        />
                    </Box>
                </Box>
            </Box>
        </GridPage>
    );
};

export default Parametres;
