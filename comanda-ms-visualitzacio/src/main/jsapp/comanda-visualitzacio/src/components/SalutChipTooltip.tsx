import * as React from "react";
import {Box, Tooltip, tooltipClasses, TooltipProps, Typography,} from "@mui/material";
import {styled} from "@mui/material/styles";
import {
    ENUM_APP_ESTAT_PREFIX,
    ENUM_BD_ESTAT_PREFIX, getColorByStatEnum,
    getMaterialIconByState,
    SalutEstatEnum,
    SalutModel,
    TITLE,
    TOOLTIP
} from "../types/salut.model.tsx";
import {Trans, useTranslation} from "react-i18next";

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
    salutField: keyof SalutModel;
    children: React.ReactElement;
}

export const SalutChipTooltip: React.FC<SalutTooltipProps> = ({ stateEnum, salutField, children, ...props }) => {
    const { t } = useTranslation();

    const prefix = salutField === SalutModel.APP_ESTAT ? ENUM_APP_ESTAT_PREFIX : ENUM_BD_ESTAT_PREFIX;
    const existTranslation: boolean = !(t(prefix + stateEnum + TOOLTIP) === (prefix + stateEnum + TOOLTIP));

    const titleContent: React.ReactNode = (
        <React.Fragment>
            {/*<span className="material-icons">check_circle</span>*/}
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

export const SalutGenericTooltip: React.FC<SalutTooltipProps> = ({ stateEnum, salutField, children, ...props }) => {
    const { t } = useTranslation();

    const prefix = salutField === SalutModel.APP_ESTAT ? ENUM_APP_ESTAT_PREFIX : ENUM_BD_ESTAT_PREFIX;
    const existTranslation: boolean = !(t(prefix + stateEnum + TOOLTIP) === (prefix + stateEnum + TOOLTIP));

    const titleContent: React.ReactNode = (
        <React.Fragment>
            {/*<span className="material-icons">check_circle</span>*/}
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