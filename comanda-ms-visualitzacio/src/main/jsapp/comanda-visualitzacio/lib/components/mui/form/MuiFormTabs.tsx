import React from 'react';
import Tabs from '@mui/material/Tabs';
import Tab, { TabProps } from '@mui/material/Tab';
import Box from '@mui/material/Box';
import { useBaseAppContext } from '../../BaseAppContext';
import { useFormContext } from '../../form/FormContext';

export type FormTabsContextType = {
    index: number;
};

export const FormTabsContext = React.createContext<FormTabsContextType | undefined>(undefined);

export const useFormTabsContext = () => {
    const context = React.useContext(FormTabsContext);
    if (context === undefined) {
        throw new Error('useFormTabsContext must be used within a FormTabsProvider');
    }
    return context;
};

/**
 * Propietats del component MuiFormTabContent.
 */
interface FormTabContentProps {
    /** Índex de la pipella */
    index: number;
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
    /** Índexos de les pipelles con contenen graelles (per a que l'alçada ocupi el 100%) */
    tabIndexesWithGrids?: number[];
    /** Event que es llença quan es canvia l'índex de la pipella activa */
    onIndexChange?: (index: number) => void;
    /** Contingut de la pipella */
    children?: React.ReactNode;
}

/**
 * Contingut de cada pipella de formulari.
 *
 * @param props - Propietats del component.
 * @returns Element JSX del contingut de les pipelles.
 */
export const MuiFormTabContent: React.FC<FormTabContentProps> = (props) => {
    const { index, showOnCreate, children, ...other } = props;
    const { index: currentIndex } = useFormTabsContext();
    const { id } = useFormContext();
    return id != null ? (
        <div
            role="tabpanel"
            hidden={currentIndex !== index}
            id={`tabpanel-${index}`}
            aria-labelledby={`tab-${index}`}
            style={{ height: '100%' }}
            {...other}>
            {currentIndex === index && <Box sx={{ pt: 3, pb: 2, height: '100%' }}>{children}</Box>}
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
    const { tabs, tabIndexesWithGrids, onIndexChange, children } = props;
    const { id } = useFormContext();
    const [index, setIndex] = React.useState<number>(0);
    const { setContentExpandsToAvailableHeight } = useBaseAppContext();
    const gridCheck = (index: number) => {
        if (tabIndexesWithGrids != null) {
            const isGridTab = tabIndexesWithGrids?.includes(index) ?? false;
            setContentExpandsToAvailableHeight(isGridTab);
        }
    };
    const handleIndexChange = (_event: React.SyntheticEvent, value: any) => {
        value != null && gridCheck(value);
        setIndex(value);
        onIndexChange?.(value);
    };
    React.useEffect(() => {
        index && gridCheck(index);
    }, [index]);
    const tabsHeightFix = { minHeight: '48px' };
    const context = { index };
    return (
        <FormTabsContext.Provider value={context}>
            {id != null ? (
                <Tabs
                    value={index}
                    onChange={handleIndexChange}
                    sx={{ borderBottom: '1px solid rgba(0, 0, 0, 0.23)' }}>
                    {tabs.map((t, i) => {
                        if (typeof t === 'string') {
                            return <Tab key={i} value={i} label={t} sx={tabsHeightFix} />;
                        } else {
                            return <Tab {...(t as any)} key={i} value={i} sx={tabsHeightFix} />;
                        }
                    })}
                </Tabs>
            ) : null}
            {children}
        </FormTabsContext.Provider>
    );
};

export default MuiFormTabs;
