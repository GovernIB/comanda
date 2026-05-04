import {
    Box,
    Button,
    Grid,
    IconButton,
    Stack,
    TextField,
    Tooltip,
    Typography,
} from "@mui/material";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import DeleteIcon from "@mui/icons-material/Delete";
import {FormField, GridPage, MuiDataGrid, useFormContext} from "reactlib";
import PageTitle from "../components/PageTitle.tsx";
import {useTranslation} from "react-i18next";

type PaletteColor = {
    id?: number;
    posicio?: number;
    valor?: string;
};

const defaultColors: PaletteColor[] = [
    {posicio: 0, valor: "#ffffff"},
    {posicio: 1, valor: "#1f2937"},
    {posicio: 2, valor: "#d1d5db"},
    {posicio: 3, valor: "#2563eb"},
    {posicio: 4, valor: "#16a34a"},
    {posicio: 5, valor: "#f3f4f6"},
];

const normalizeColors = (colors?: PaletteColor[]) => (
    [...(colors || [])]
        .filter((color) => color != null)
        .sort((a, b) => (a.posicio ?? Number.MAX_SAFE_INTEGER) - (b.posicio ?? Number.MAX_SAFE_INTEGER))
        .map((color, posicio) => ({...color, posicio}))
);

const colorInputValue = (value?: string) => /^#[0-9a-f]{6}$/i.test(value || "") ? value || "#000000" : "#000000";

const PaletteMiniature = ({colors}: { colors?: PaletteColor[] }) => (
    <Box sx={{display: "flex", width: 140, height: 22, border: "1px solid", borderColor: "divider", overflow: "hidden"}}>
        {normalizeColors(colors).map((color, index) => (
            <Box key={`${index}-${color.valor}`} sx={{flex: 1, minWidth: 8, bgcolor: color.valor || "transparent"}} />
        ))}
    </Box>
);

const PaletaForm = () => {
    const {data, apiRef} = useFormContext();
    const colors = normalizeColors(data?.colors);

    const setColors = (nextColors: PaletteColor[]) => {
        apiRef.current?.setFieldValue("colors", normalizeColors(nextColors));
    };

    const updateColor = (index: number, valor: string) => {
        const nextColors = [...colors];
        nextColors[index] = {...nextColors[index], valor};
        setColors(nextColors);
    };

    const moveColor = (index: number, direction: -1 | 1) => {
        const target = index + direction;
        if (target < 0 || target >= colors.length) return;
        const nextColors = [...colors];
        [nextColors[index], nextColors[target]] = [nextColors[target], nextColors[index]];
        setColors(nextColors);
    };

    const addColor = () => {
        setColors([...colors, {posicio: colors.length, valor: "#000000"}]);
    };

    const deleteColor = (index: number) => {
        setColors(colors.filter((_, posicio) => posicio !== index));
    };

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="nom" required />
            </Grid>
            <Grid size={12}>
                <FormField name="descripcio" />
            </Grid>
            <Grid size={12}>
                <Stack spacing={1}>
                    <Typography variant="subtitle2">Colors</Typography>
                    <PaletteMiniature colors={colors} />
                    {colors.map((color, index) => (
                        <Stack key={`${index}-${color.valor}`} direction="row" spacing={1} alignItems="center">
                            <Typography variant="body2" sx={{width: 28}}>
                                {index}
                            </Typography>
                            <TextField
                                type="color"
                                size="small"
                                value={colorInputValue(color.valor)}
                                onChange={(event) => updateColor(index, event.target.value)}
                                sx={{width: 64}}
                            />
                            <TextField
                                size="small"
                                value={color.valor || ""}
                                onChange={(event) => updateColor(index, event.target.value)}
                                sx={{flex: 1}}
                            />
                            <Tooltip title="Moure amunt">
                                <span>
                                    <IconButton size="small" onClick={() => moveColor(index, -1)} disabled={index === 0}>
                                        <ArrowUpwardIcon fontSize="small" />
                                    </IconButton>
                                </span>
                            </Tooltip>
                            <Tooltip title="Moure avall">
                                <span>
                                    <IconButton size="small" onClick={() => moveColor(index, 1)} disabled={index === colors.length - 1}>
                                        <ArrowDownwardIcon fontSize="small" />
                                    </IconButton>
                                </span>
                            </Tooltip>
                            <Tooltip title="Eliminar">
                                <span>
                                    <IconButton size="small" onClick={() => deleteColor(index)} disabled={colors.length <= 1}>
                                        <DeleteIcon fontSize="small" />
                                    </IconButton>
                                </span>
                            </Tooltip>
                        </Stack>
                    ))}
                    <Button variant="outlined" startIcon={<AddCircleOutlineIcon />} onClick={addColor}>
                        Afegir color
                    </Button>
                </Stack>
            </Grid>
        </Grid>
    );
};

const columns = [
    {
        field: "nom",
        flex: 1,
    },
    {
        field: "descripcio",
        flex: 2,
    },
    {
        field: "colors",
        headerName: "Colors",
        flex: 1,
        renderCell: (params: any) => <PaletteMiniature colors={params.row?.colors} />,
        sortable: false,
    },
];

const Paletes = () => {
    const {t} = useTranslation();

    return (
        <GridPage>
            <PageTitle title={t($ => $.menu.paleta)} />
            <MuiDataGrid
                title={t($ => $.menu.paleta)}
                resourceName="paleta"
                columns={columns}
                toolbarType="upper"
                paginationActive
                popupEditCreateActive
                popupEditActive
                popupEditFormContent={<PaletaForm />}
                popupEditFormDialogResourceTitle={t($ => $.menu.paleta)}
                popupEditFormDialogComponentProps={{fullWidth: true, maxWidth: "md"}}
                formAdditionalData={(row: any) => !row?.id ? {colors: defaultColors} : {}}
            />
        </GridPage>
    );
};

export default Paletes;
