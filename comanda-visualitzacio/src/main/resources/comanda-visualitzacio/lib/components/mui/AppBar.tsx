import React from 'react';
import MuiAppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import AuthButton from './AuthButton';
import { useOptionalAuthContext } from '../AuthContext';
import { toolbarBackgroundStyle } from '../../util/toolbar';

type AppBarProps = {
    title: string;
    version?: string;
    logo?: string;
    logoStyle?: any;
    menuButton: React.ReactNode,
    additionalComponents?: React.ReactElement | React.ReactElement[];
    style?: any;
    backgroundColor?: string;
    backgroundImg?: string;
};

export const AppBar: React.FC<AppBarProps> = (props) => {
    const {
        title,
        version,
        logo,
        logoStyle,
        menuButton,
        additionalComponents,
        style,
        backgroundColor,
        backgroundImg,
    } = props;
    const authContext = useOptionalAuthContext();
    const authButton = authContext != null ? <AuthButton /> : null;
    const backgroundStyle = backgroundColor ? toolbarBackgroundStyle(backgroundColor, backgroundImg) : {};
    return <MuiAppBar position="sticky" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar style={{ ...style, ...backgroundStyle }}>
            {menuButton}
            {logo ? <Box sx={{ mr: 2, pt: 1, pr: 2, cursor: 'pointer', ...logoStyle }}>
                <img src={logo} alt="logo" />
            </Box> : null}
            <Typography
                variant="h6"
                component="div"
                title={title}
                sx={{ flexGrow: 1 }}>
                {title}
                {version && <Typography variant="caption">&nbsp;v{version}</Typography>}
            </Typography>
            {additionalComponents}
            {authButton}
        </Toolbar>
    </MuiAppBar>;
}

export default AppBar;