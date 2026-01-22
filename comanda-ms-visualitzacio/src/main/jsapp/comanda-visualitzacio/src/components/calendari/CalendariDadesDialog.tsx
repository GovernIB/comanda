import React, { useState, useEffect, useMemo } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormGroup,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Checkbox,
  ListItemText,
  OutlinedInput,
  IconButton,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Grid,
  Button,
  TextField,
  Icon,
} from "@mui/material";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import { DadesDia } from "./CalendariTypes";

interface CalendariDadesDialogProps {
  dimensions: any[];
  indicadors: any[];
  currentDadesDia: DadesDia[];
  currentDataDia: string;
  dadesDiaModalOpen: boolean;
  setDadesDiaModalOpen: (open: boolean) => void;
}

const CalendariDadesDialog: React.FC<CalendariDadesDialogProps> = ({
  dimensions,
  indicadors,
  currentDadesDia,
  currentDataDia,
  dadesDiaModalOpen,
  setDadesDiaModalOpen,
}) => {
  const { t } = useTranslation();

  // const dimensionsCodis = dimensions.map((i) => i.codi);
  const indicadorsCodis = indicadors.map((i) => i.codi);

  const [indicadorsShow, setIndicadorsShow] = useState<any[]>(indicadorsCodis);
  const [open, setOpen] = useState<boolean>(true);
  const [filterForm, setFilterForm] = useState<Record<string, any>>({});
  const deferredFilter = React.useDeferredValue(filterForm);

  const currentDadesDiaFiltered = useMemo(() => {
    if (!Object.keys(deferredFilter).length) return currentDadesDia;
    return currentDadesDia.filter((dada: DadesDia) =>
      Object.entries(deferredFilter).every(([key, value]) =>
        dada.dimensionsJson?.[key]?.toLowerCase?.().includes?.(value?.toLowerCase?.())
      ));
  }, [currentDadesDia, deferredFilter]);

  useEffect(() => {
    if (!currentDadesDia.length) return;
    setFilterForm({});
    const newIndicadors = Object.keys(currentDadesDia[0]?.indicadorsJson || {});
    setIndicadorsShow((prev) =>
      JSON.stringify(prev) !== JSON.stringify(newIndicadors)
        ? newIndicadors
        : prev
    );
  }, [currentDadesDia]);

  const DadesTable = React.memo(
    ({ filteredData, dimensions, indicadors, indicadorsShow }: any) => (
      <TableContainer component={Paper} sx={{ maxHeight: "calc(95vh - 200px)" }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {dimensions.map((dim: any) => (
                <TableCell key={`dim-${dim.codi}`}>{dim.nom}</TableCell>
              ))}
              {indicadors
                .filter((i: any) => indicadorsShow.includes(i.codi))
                .map((ind: any) => (
                  <TableCell key={`ind-${ind.codi}`} align="right">
                    {ind.nom}
                  </TableCell>
                ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredData.map((fet: DadesDia, index: number) => (
              <TableRow
                key={index}
                sx={{ backgroundColor: index % 2 === 0 ? "background.default" : "grey.50",}}
              >
                {dimensions.map((dim: any) => (
                  <TableCell key={`dim-val-${index}-${dim.codi}`}>
                    {fet.dimensionsJson[dim.codi]}
                  </TableCell>
                ))}
                {indicadors
                  .filter((i: any) => indicadorsShow.includes(i.codi))
                  .map((ind: any) => (
                    <TableCell key={`ind-val-${index}-${ind.codi}`} align="right">
                      {fet.indicadorsJson[ind.codi]}
                    </TableCell>
                  ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    )
  );

  if (!dadesDiaModalOpen) return null;

  return (
    <Dialog
      open={dadesDiaModalOpen}
      onClose={() => setDadesDiaModalOpen(false)}
      maxWidth="xl"
      fullWidth
      fullScreen
    >
      <DialogTitle>
        {t($ => $.calendari.modal_dades_dia)} -{" "}
        {dayjs(currentDataDia).format("DD/MM/YYYY")}
      </DialogTitle>
      <DialogContent>
        {currentDadesDia.length > 0 ? (
          <>
            <FormGroup>
              <Grid container spacing={1} p={1} sx={{ maxWidth: "100%" }}>
                <Grid size={11}>
                  <FormControl sx={{ width: "100%" }} size={"small"}>
                    <InputLabel>{t($ => $.calendari.indicadors)}</InputLabel>
                    <Select
                      multiple
                      value={indicadorsShow}
                      onChange={(event) => {
                        // On autofill we get a stringified value.
                        const value = typeof event.target.value === 'string' ? event.target.value.split(',') : event.target.value;
                        if (value.includes("all")) {
                          if (indicadorsShow.length === indicadorsCodis.length) {
                            setIndicadorsShow([]);
                          } else {
                            setIndicadorsShow(indicadorsCodis);
                          }
                        } else {
                          setIndicadorsShow(value);
                        }
                      }}
                      input={<OutlinedInput label={t($ => $.calendari.indicadors)} />}
                      renderValue={(selected) => selected.join(", ")}
                    >
                      <MenuItem key="all" value="all">
                        <Checkbox
                          checked={indicadorsShow.length === indicadorsCodis.length}
                          indeterminate={
                            indicadorsShow.length > 0 &&
                            indicadorsShow.length < indicadorsCodis.length
                          }
                        />
                        <ListItemText primary="Seleccionar todo" />
                      </MenuItem>

                      {indicadors.map((indicador: any) => (
                        <MenuItem key={indicador.codi} value={indicador.codi}>
                          <Checkbox checked={indicadorsShow.includes(indicador.codi)} />
                          <ListItemText primary={indicador.nom} />
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                <Grid size={1} display={"flex"} justifyContent={"center"}>
                  <IconButton
                    title={t($ => $.components.clear)}
                    onClick={() => {
                      setIndicadorsShow([]);
                      setFilterForm({});
                    }}
                  >
                    <Icon>filter_alt_off</Icon>
                  </IconButton>
                  <IconButton
                    title={t($ => $.page.avisos.filter.more)}
                    onClick={() => setOpen((prev) => !prev)}
                  >
                    <Icon>filter_list</Icon>
                  </IconButton>
                </Grid>

                {dimensions.map((dimension: any) => (
                  <Grid size={3} hidden={open}>
                    <TextField
                      id={`textField-${dimension.codi}`}
                      label={dimension.nom}
                      variant="outlined"
                      value={filterForm[dimension.codi] || ""}
                      size={"small"}
                      fullWidth
                      onChange={(event) => {
                        setFilterForm({
                          ...filterForm,
                          [dimension.codi]: event.target.value,
                        });
                      }}
                    />
                  </Grid>
                ))}
              </Grid>
            </FormGroup>

            <DadesTable
              filteredData={currentDadesDiaFiltered}
              dimensions={dimensions}
              indicadors={indicadors}
              indicadorsShow={indicadorsShow}
            />
          </>
        ) : (
          <Typography variant="body1">{t($ => $.calendari.sense_dades)}</Typography>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={() => setDadesDiaModalOpen(false)}>
          {t($ => $.calendari.tancar)}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default React.memo(CalendariDadesDialog);
