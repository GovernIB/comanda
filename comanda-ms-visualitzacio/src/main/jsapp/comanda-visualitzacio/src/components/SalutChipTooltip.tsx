import * as React from "react";
import {Box, Tooltip, tooltipClasses, TooltipProps, Typography,} from "@mui/material";
import {styled} from "@mui/material/styles";
import {
    ENUM_APP_ESTAT_PREFIX,
    ENUM_BD_ESTAT_PREFIX,
    ENUM_INTEGRACIO_ESTAT_PREFIX,
    getColorByStatEnum,
    getMaterialIconByState,
    SalutEstatEnum,
    TITLE,
    TOOLTIP
} from "../types/salut.model.tsx";
import {Trans, useTranslation} from "react-i18next";

export enum SalutField {
  APP_ESTAT = "appEstat",
  BD_ESTAT = "bdEstat",
  INTEGRACIO_ESTAT = "integracioEstat"
}

// Tooltip con estilo
const StyledTooltip = styled(({ className, ...props }: TooltipProps) => (
    <Tooltip {...props} classes={{ popper: className }} />
))(({ theme }) => ({
    [`& .${tooltipClasses.tooltip}`]: {
        backgroundColor: "#f5f5f9",
        color: "rgba(0, 0, 0, 0.87)",
        maxWidth: 220,
        fontSize: theme.typography.pxToRem(12),
        border: "1px solid #dadde9",
    },
}));

interface SalutTooltipProps extends Omit<TooltipProps, "title"> {
    stateEnum: SalutEstatEnum;
    salutField: SalutField;
    children: React.ReactElement;
}

interface SalutGenericTooltipProps extends TooltipProps {
    children: React.ReactElement;
}

const getPrefixByField = (field: SalutField): string => {
  switch (field) {
    case SalutField.APP_ESTAT:
      return ENUM_APP_ESTAT_PREFIX;
    case SalutField.BD_ESTAT:
      return ENUM_BD_ESTAT_PREFIX;
    case SalutField.INTEGRACIO_ESTAT:
      return ENUM_INTEGRACIO_ESTAT_PREFIX;
    default:
      return ENUM_APP_ESTAT_PREFIX; // valor por defecto por si acaso
  }
};

export const SalutChipTooltip: React.FC<SalutTooltipProps> = ({ stateEnum, salutField, children, ...props }) => {
    const { t } = useTranslation();

    const prefix = getPrefixByField(salutField);
    const existTranslation: boolean = !(t(prefix + stateEnum + TOOLTIP) === (prefix + stateEnum + TOOLTIP));

    const titleContent: React.ReactNode = (
        <React.Fragment>
            <Box display="flex" alignItems="center" gap={1} color={getColorByStatEnum(stateEnum)}>
                {getMaterialIconByState(stateEnum)}
                <Typography color="inherit" fontWeight={800}>
                    {t(ENUM_APP_ESTAT_PREFIX + stateEnum + TITLE)}
                </Typography>
            </Box>
            {existTranslation &&
                <Trans
                    i18nKey={prefix + stateEnum + TOOLTIP}
                    components={{
                        bold: <b/>,
                        underline: <u/>,
                        italic: <em/>
                    }}
                />}
        </React.Fragment>
    );

    return (
        <StyledTooltip title={titleContent} {...props}>
            {children}
        </StyledTooltip>
    );
};

export const SalutGenericTooltip: React.FC<SalutGenericTooltipProps> = ({ children, ...props }) => {

    return (
        <StyledTooltip {...props}>
            {children}
        </StyledTooltip>
    );
};