import * as React from "react";
import { Box, } from "@mui/material";
import {GridPage, MuiDataGrid, useFormContext} from "reactlib";
import PageTitle from "../components/PageTitle.tsx";
import {useTranslation} from "react-i18next";
import { normalizeColors, PaletteColor, PaletteData, PaletteFormContent } from "../components/PaletteFormContent.tsx";


const defaultColors: PaletteColor[] = [
    {posicio: 0, valor: "#ffffff"},
    {posicio: 1, valor: "#1f2937"},
    {posicio: 2, valor: "#d1d5db"},
    {posicio: 3, valor: "#2563eb"},
    {posicio: 4, valor: "#16a34a"},
    {posicio: 5, valor: "#f3f4f6"},
];

const PaletteMiniature = ({colors}: { colors?: PaletteColor[] }) => (
    <Box sx={{display: "flex", width: 140, height: 22, border: "1px solid", borderColor: "divider", overflow: "hidden"}}>
        {normalizeColors(colors).map((color, index) => (
            <Box key={`${index}-${color.valor}`} sx={{flex: 1, minWidth: 8, bgcolor: color.valor || "transparent"}} />
        ))}
    </Box>
);

const PaletaForm = () => {
  const { data, apiRef } = useFormContext();
  const palette = (data as PaletteData) || { colors: [] };

  const handleChange = (newPalette: PaletteData) => {
    Object.keys(newPalette).forEach(key => {
      if (key !== 'id' && key !== 'clientId') {
        apiRef.current?.setFieldValue(key, newPalette[key]);
      }
    });
  };

  return (
    <PaletteFormContent
      palette={palette}
      onChange={handleChange}
      mode={data?.id ? 'edit' : 'create'}
      showDuplicateButton={false}
    />
  );
};

const Paletes = () => {
    const {t} = useTranslation();

    const columns = React.useMemo(() => [
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
            headerName: t($ => $.page.palette.column.colors),
            flex: 1,
            renderCell: (params: { row?: { colors?: PaletteColor[] } }) => <PaletteMiniature colors={params.row?.colors} />,
            sortable: false,
        },
    ], [t]);

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
                formAdditionalData={(row: { id?: unknown }) => !row?.id ? {colors: defaultColors} : {}}
            />
        </GridPage>
    );
};

export default Paletes;
