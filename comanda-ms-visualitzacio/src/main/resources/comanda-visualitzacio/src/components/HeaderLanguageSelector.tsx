import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import { useBaseAppContext } from 'reactlib';

type LanguageOptionProps = {
    locale: string;
    label: string;
    isActive: boolean;
    onClick: () => void;
    sx?: any;
};

const LanguageOption: React.FC<LanguageOptionProps> = ({ label, isActive, onClick, sx }) => {

    return (
        <Typography
            sx={{
                fontWeight: isActive ? 'bold' : 'normal',
                fontSize: '18px',
                '&:hover': {
                    cursor: isActive ? 'default' : 'pointer',
                    textDecoration: isActive ? 'none' : 'underline',
                },
                ...sx
            }}
            onClick={isActive ? undefined : onClick}
        >
            {label}
        </Typography>
    );
};

type HeaderLanguageSelectorProps = {
    languages?: string[];
    onLanguageChange?: (language?: string) => void;
    sx?: any;
    optionSx?: any;
};

const HeaderLanguageSelector: React.FC<HeaderLanguageSelectorProps> = (props) => {
    const {
        languages,
        onLanguageChange,
        sx,
        optionSx,
        ...otherProps
    } = props;

    const { currentLanguage, setCurrentLanguage } = useBaseAppContext();

    React.useEffect(() => {
        onLanguageChange?.(currentLanguage);
    }, [currentLanguage, onLanguageChange]);

    const changeLanguage = (locale: string) => {
        setCurrentLanguage(locale);
    };

    // Get the two-character language code
    const currentLanguageTwoChars = currentLanguage?.substring(0, 2).toLowerCase();

    if (!languages || languages.length === 0) {
        return null;
    }

    return (
        <Box
            sx={{
                display: "flex",
                gap: 1,
                mr: 4,
                color: "black",
                ...sx
            }}
            {...otherProps}
        >
            {languages.map((language: string, index: number) => {
                const languageTwoChars = language.substring(0, 2).toLowerCase();
                const isLast = index === languages.length - 1;

                return (
                    <React.Fragment key={language}>
                        <LanguageOption
                            locale={language}
                            label={languageTwoChars.toUpperCase()}
                            isActive={currentLanguageTwoChars === languageTwoChars}
                            onClick={() => changeLanguage(language)}
                            sx={optionSx}
                        />
                        {!isLast && <Divider orientation="vertical" flexItem />}
                    </React.Fragment>
                );
            })}
        </Box>
    );
};

export default HeaderLanguageSelector;
