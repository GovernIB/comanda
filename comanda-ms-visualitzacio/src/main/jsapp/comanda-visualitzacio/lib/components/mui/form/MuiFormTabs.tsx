import React from 'react';
import Tabs from '@mui/material/Tabs';
import Tab, { TabProps } from '@mui/material/Tab';
import Box from '@mui/material/Box';
import { useBaseAppContext } from '../../BaseAppContext';
import { useFormContext } from '../../form/FormContext';

/**
 * Propietats del component MuiFormTabContent.
 */
interface FormTabContentProps {
    /** Índex de la pipella */
    index: number;
    /** Índex de la pipella actual */
    currentIndex: number;
    /** Indica si aquesta pipella s'ha de mostrar en els formularis de creació */
    showOnCreate?: boolean;
    /** Contingut de la pipella */
    children?: React.ReactNode;
}

export type FormTabsValue = number | string | TabProps;

/**
 * Propietats del component MuiFormTabs.
 */
interface FormTabsProps {
    /** Pipelles disponibles */
    tabs: FormTabsValue[];
    /** Índex de la pipella actualment activa */
    currentIndex?: number;
    /** Event que es llença quan es canvia l'índex de la pipella activa */
    onIndexChange?: (index: number) => void;
    /** Índexos de les pipelles con contenen graelles (per a que l'alçada ocupi el 100%) */
    tabIndexesWithGrids?: number[];
}

/**
 * Contingut de cada pipella de formulari.
 *
 * @param props - Propietats del component.
 * @returns Element JSX del contingut de les pipelles.
 */
export const MuiFormTabContent: React.FC<FormTabContentProps> = (props) => {
    const { index, currentIndex, showOnCreate, children, ...other } = props;
    const { id } = useFormContext();
    return id != null ? (
        <div
            role="tabpanel"
            hidden={currentIndex !== index}
            id={`tabpanel-${index}`}
            aria-labelledby={`tab-${index}`}
            style={{ height: '100%' }}
            {...other}>
            {currentIndex === index && (
                <Box sx={{ pt: 3, pb: 2, height: '100%' }}>{children}</Box>
            )}
        </div>
    ) : showOnCreate ? (
        children
    ) : null;
};

/**
 * Pipelles de formulari.
 *
 * @param props - Propietats del component.
 * @returns Element JSX de les pipelles.
 */
export const MuiFormTabs: React.FC<FormTabsProps> = (props) => {
    const { tabs, currentIndex, onIndexChange, tabIndexesWithGrids } = props;
    const { id } = useFormContext();
    const { setContentExpandsToAvailableHeight } = useBaseAppContext();
    const gridCheck = (index: number) => {
        if (tabIndexesWithGrids != null) {
            const isGridTab = tabIndexesWithGrids?.includes(index) ?? false;
            setContentExpandsToAvailableHeight(isGridTab);
        }
    };
    const handleIndexChange = (_event: React.SyntheticEvent, value: any) => {
        value && gridCheck(value);
        onIndexChange?.(value);
    };
    React.useEffect(() => {
        currentIndex && gridCheck(currentIndex);
    }, [currentIndex]);
    const tabsHeightFix = { minHeight: '48px' };
    return id != null ? (
        <Tabs
            value={currentIndex}
            onChange={handleIndexChange}
            sx={{ borderBottom: '1px solid rgba(0, 0, 0, 0.23)' }}>
            {tabs.map((t, i) => {
                if (typeof t === 'string') {
                    return (
                        <Tab key={i} value={i} label={t} sx={tabsHeightFix} />
                    );
                } else {
                    return (
                        <Tab
                            {...(t as any)}
                            key={i}
                            value={i}
                            sx={tabsHeightFix}
                        />
                    );
                }
            })}
        </Tabs>
    ) : null;
};

export default MuiFormTabs;
