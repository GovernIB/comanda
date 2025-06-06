import React, { useState, useRef, useEffect } from "react";
import {
    TextField,
    Popover,
    ListItemIcon,
    ListItemText,
    MenuItem,
    Box,
    Typography,
} from "@mui/material";
import * as Icons from "@mui/icons-material";
import { FixedSizeList } from "react-window";
import { useFormContext, FormFieldDataActionType } from '../../lib/components/form/FormContext';

const allIconNames = Object.keys(Icons).filter(
    (name) => !name.match(/(Outlined|Rounded|Sharp|TwoTone)$/)
);

const ITEM_HEIGHT = 48;
const MAX_ITEMS_VISIBLE = 8;

interface IconAutocompleteSelectProps {
    name?: string;
    label?: string;
    onChange?: (value: any) => void;
}

const IconAutocompleteSelect: React.FC<IconAutocompleteSelectProps> = ({
        name = "icona",
        label = "Icona",
        onChange
}) => {
    const { data, dataDispatchAction, dataGetFieldValue } = useFormContext();
    const iconValue = dataGetFieldValue(name);

    const [selectedIcon, setSelectedIcon] = useState<string | null>(iconValue || null);
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [filter, setFilter] = useState("");
    const [inputWidth, setInputWidth] = useState<number>(300);
    const inputRef = useRef<HTMLInputElement>(null);
    const searchInputRef = useRef<HTMLInputElement>(null);
    const [iconAliases, setIconAliases] = useState<Record<string, string[]>>({});

    useEffect(() => {
        fetch("/mui_icon_aliases_complete.json")
            .then((res) => res.json())
            .then((data) => setIconAliases(data))
            .catch((err) => console.error("Error carregant àlies d'icones:", err));
    }, []);

    // Update selectedIcon when form context value changes
    useEffect(() => {
        const formValue = dataGetFieldValue(name);
        if (formValue !== selectedIcon) {
            setSelectedIcon(formValue || null);
        }
    }, [dataGetFieldValue, name, data]);

    const open = Boolean(anchorEl);

    useEffect(() => {
        if (anchorEl) {
            setInputWidth(anchorEl.clientWidth);
        }
    }, [anchorEl]);

    useEffect(() => {
        if (open) {
            const timeout = setTimeout(() => {
                if (searchInputRef.current) {
                    searchInputRef.current.focus();
                }
            }, 100); // petit delay per assegurar que el DOM està muntat

            return () => clearTimeout(timeout); // neteja si es tanca abans
        }
    }, [open]);

    // Afegim l'opció buida al principi
    const getMatchingIcons = (query: string): string[] => {
        const q = query.toLowerCase();
        const filtered = allIconNames.filter((name) => {
            const aliases = iconAliases[name] || [];
            return (
                name.toLowerCase().includes(q) ||
                aliases.some((alias) => alias.toLowerCase().includes(q))
            );
        });
        return ["", ...filtered]; // "" representa "Sense icona"
    };

    const matchingIcons = getMatchingIcons(filter);

    const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
        setFilter("");
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleSelect = (iconName: string) => {
        setSelectedIcon(iconName || null);

        // Update form context
        dataDispatchAction({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: { fieldName: name, field: name, value: iconName || null }
        });

        onChange?.(iconName);
        handleClose();
    };

    const renderRow = ({ index, style }: { index: number; style: React.CSSProperties }) => {
        const name = matchingIcons[index];
        const Icon = name ? Icons[name] : null;

        return (
            <MenuItem
                key={name || "none"}
                style={style}
                onClick={() => handleSelect(name)}
            >
                {Icon ? (
                    <>
                        <ListItemIcon><Icon /></ListItemIcon>
                        <ListItemText primary={name} />
                    </>
                ) : (
                    <ListItemText primary="(Sense icona)" />
                )}
            </MenuItem>
        );
    };

    const displayValue = selectedIcon || "";

    return (
        <>
            <TextField
                inputRef={inputRef}
                label={label}
                value={displayValue}
                onClick={handleOpen}
                InputProps={{
                    readOnly: true,
                    startAdornment: selectedIcon ? (
                        <Box display="flex" alignItems="center" mr={1}>
                            {React.createElement(Icons[selectedIcon])}
                        </Box>
                    ) : null,
                }}
                fullWidth
            />

            <Popover
                open={open}
                anchorEl={anchorEl}
                onClose={handleClose}
                anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
                transformOrigin={{ vertical: "top", horizontal: "left" }}
                PaperProps={{ style: { width: inputWidth } }} // 🟢 Amplada igual que l’input
            >
                <Box p={1}>
                    <TextField
                        inputRef={searchInputRef}
                        placeholder="Cerca icona..."
                        value={filter}
                        onChange={(e) => setFilter(e.target.value)}
                        fullWidth
                    />
                    {matchingIcons.length > 0 ? (
                        <FixedSizeList
                            height={Math.min(MAX_ITEMS_VISIBLE, matchingIcons.length) * ITEM_HEIGHT}
                            itemCount={matchingIcons.length}
                            itemSize={ITEM_HEIGHT}
                            width="100%"
                        >
                            {renderRow}
                        </FixedSizeList>
                    ) : (
                        <Box p={2}>
                            <Typography variant="body2" color="text.secondary">
                                No s'han trobat icones.
                            </Typography>
                        </Box>
                    )}
                </Box>
            </Popover>
        </>
    );
};

export default IconAutocompleteSelect;
