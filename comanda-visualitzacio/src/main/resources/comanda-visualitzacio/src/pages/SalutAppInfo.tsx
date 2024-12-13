import * as React from 'react';
import { useParams } from 'react-router-dom';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import {
    BasePage,
    Toolbar,
    useBaseAppContext,
} from 'reactlib';

const SalutAppInfo: React.FC = () => {
    const { code } = useParams();
    const { goBack } = useBaseAppContext();
    const toolbarElementsWithPositions = [{
        position: 0,
        element: <IconButton onClick={() => goBack('/')} sx={{ mr: 1 }}>
            <Icon>arrow_back</Icon>
        </IconButton>
    }];
    const toolbar = <Toolbar
        title={'Salut aplicació ' + code}
        elementsWithPositions={toolbarElementsWithPositions}
        upperToolbar />;
    return <BasePage toolbar={toolbar}>
    </BasePage>;
}

export default SalutAppInfo;
