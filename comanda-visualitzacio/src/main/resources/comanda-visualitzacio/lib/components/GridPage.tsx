import React from 'react';
import { useBaseAppContext } from './BaseAppContext';

type GridPageProps = React.PropsWithChildren & {
    disableMargins?: true;
    style?: React.CSSProperties;
};

export const GridPage: React.FC<GridPageProps> = (props) => {
    const { disableMargins = false, style, children } = props;
    const {
        setMarginsDisabled,
        setContentExpandsToAvailableHeight
    } = useBaseAppContext();
    React.useEffect(() => {
        setMarginsDisabled(disableMargins);
        return () => setMarginsDisabled(false);
    }, [disableMargins]);
    React.useEffect(() => {
        setContentExpandsToAvailableHeight(true);
        return () => setContentExpandsToAvailableHeight(false);
    }, []);
    return <div style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        ...style
    }}>
        {children}
    </div>;
}

export default GridPage;