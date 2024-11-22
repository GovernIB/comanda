import React from 'react';

export const BasePage: React.FC<React.PropsWithChildren> = (props) => {
    const { children } = props;
    return <div>
        {children}
    </div>;
}

export default BasePage;