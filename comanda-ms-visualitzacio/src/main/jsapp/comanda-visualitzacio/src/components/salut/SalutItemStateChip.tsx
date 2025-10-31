import {SalutChipTooltip, SalutField} from "./SalutChipTooltip.tsx";
import Chip from "@mui/material/Chip";
import {
    ENUM_APP_ESTAT_PREFIX,
    getColorByStatEnum,
    getMaterialIconByState,
    SalutEstatEnum,
    TITLE
} from "../../types/salut.model.tsx";
import {ChipColor} from "../../util/colorUtil.ts";
import {ChipProps, Typography} from "@mui/material";
import useTranslationStringKey from '../../hooks/useTranslationStringKey';

interface ItemStateChipProps extends ChipProps {
    salutField: SalutField;
    salutStatEnum: SalutEstatEnum | undefined;
    date?: string;
}

export const ItemStateChip: React.FC<ItemStateChipProps> = ({ salutField, salutStatEnum, date, sx,...props }) => {
    const { t } = useTranslationStringKey();

    const renderChip = (state: SalutEstatEnum) => (
        <SalutChipTooltip stateEnum={state} salutField={salutField}>
            <Chip
                sx={[
                    {
                        bgcolor: getColorByStatEnum(state),
                        color: ChipColor.WHITE,
                        "& .MuiChip-label": {
                            fontSize: "0.7rem !important",
                        },
                    },
                    ...(Array.isArray(sx) ? sx : [sx]), // mergea con el sx que le pases desde fuera
                ]}
                icon={getMaterialIconByState(state)}
                label={t(ENUM_APP_ESTAT_PREFIX + state + TITLE)}
                size="small"
                {...props} // se pasan el resto de props (onClick, disabled, etc.)
            />
        </SalutChipTooltip>
    );
    return (
        <>
            {renderChip(salutStatEnum ?? SalutEstatEnum.UNKNOWN)}

            {date && (
                <>
                    <br />
                    <Typography variant="caption">{date}</Typography>
                </>
            )}
        </>
    );
};
