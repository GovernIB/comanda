import * as React from 'react';
import { GridPage, MuiGrid } from 'reactlib';

const Apps: React.FC = () => {
    const columns = [{
        field: 'codi',
        flex: 1,
    }, {
        field: 'nom',
        flex: 3,
    }];
    return <GridPage>
        <MuiGrid
            resourceName="app"
            columns={columns}
            toolbarType="upper"
            paginationActive
            //readOnly
            rowDetailLink="/dd"
            />
    </GridPage>;
}

export default Apps;
