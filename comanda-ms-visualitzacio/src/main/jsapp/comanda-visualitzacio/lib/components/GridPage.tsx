import React from 'react';
import { useBaseAppContext } from './BaseAppContext';

/**
 * Propietats del component GridPage.
 */
type GridPageProps = React.PropsWithChildren & {
    /** Indica que s'han de desactivar els marges */
    disableMargins?: boolean;
    /** Estils addicionals per l'element contenidor */
    style?: React.CSSProperties;
};

/**
 * Pàgina que conté un element de graella de dades.
 *
 * @param props - Propietats del component.
 * @returns Element JSX de la pàgina.
 */
export const GridPage: React.FC<GridPageProps> = (props) => {
    const { disableMargins = true, style, children } = props;
    const {
        setMarginsDisabled,
        contentExpandsToAvailableHeight,
        setContentExpandsToAvailableHeight,
    } = useBaseAppContext();
    const [proceed, setProceed] = React.useState<boolean>(
        contentExpandsToAvailableHeight
    );
    React.useEffect(() => {
        if (!proceed && contentExpandsToAvailableHeight) {
            setProceed(true);
        }
    }, [contentExpandsToAvailableHeight]);
    React.useEffect(() => {
        setMarginsDisabled(disableMargins);
        return () => setMarginsDisabled(false);
    }, [disableMargins]);
    React.useEffect(() => {
        setContentExpandsToAvailableHeight(true);
        return () => setContentExpandsToAvailableHeight(false);
    }, []);
    return (
        <div
            style={{
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                ...style,
            }}>
            {proceed && children}
        </div>
    );
};

export default GridPage;
