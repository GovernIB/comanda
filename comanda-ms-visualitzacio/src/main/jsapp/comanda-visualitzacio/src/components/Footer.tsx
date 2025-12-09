import React, { useState, useEffect } from 'react';
import Toolbar from '@mui/material/Toolbar';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { toolbarBackgroundStyle } from 'reactlib';


type AppFootProps = {
    title?: string;
    logos?: string[];
    logoStyle?: any;
    style?: any;
    backgroundColor?: string;
    backgroundImg?: string;
};

export const Footer: React.FC<AppFootProps> = (props) => {
    const {
        title,
        logos,
        logoStyle,
        style,
        backgroundColor,
        backgroundImg,
    } = props;
    const toolbarRef = React.useRef<HTMLDivElement | null>(null);
    const [toolbarPosX, setToolbarPosX] = React.useState<number>();
    const [buildTimestamp, setBuildTimestamp] = useState<string | null>(null);
    const [scmRevision, setScmRevision] = useState<string | null>(null);
    const [comandaVersion, setComandaVersion] = useState<string | null>(null);

    useEffect(() => {
        // Comprova si window.__MANIFEST__ ja està disponible
        if (window.__MANIFEST__) {
            setBuildTimestamp(window.__MANIFEST__["Build-Timestamp"]);
            setScmRevision(window.__MANIFEST__["Implementation-SCM-Revision"]);
            setComandaVersion(window.__MANIFEST__["Implementation-Version"]);
        } else {
            // Si no està disponible, espera a que l'script es carregui
            const checkManifestInterval = setInterval(() => {
                if (window.__MANIFEST__) {
                    clearInterval(checkManifestInterval);
                    setBuildTimestamp(window.__MANIFEST__["Build-Timestamp"]);
                    setScmRevision(window.__MANIFEST__["Implementation-SCM-Revision"]);
                    setComandaVersion(window.__MANIFEST__["Implementation-Version"]);
                }
            }, 100);

            // Estableix un temps límit per aturar la comprovació després d'un temps raonable (5 segons)
            const timeoutId = setTimeout(() => {
                clearInterval(checkManifestInterval);
                // Si el manifest encara no està disponible, podem establir valors per defecte o deixar-ho com a null
                if (!window.__MANIFEST__) {
                    console.warn('Manifest no disponible després del temps d\'espera');
                }
            }, 5000);

            // Neteja l'interval i el temps límit quan el component es desmunta
            return () => {
                clearInterval(checkManifestInterval);
                clearTimeout(timeoutId);
            };
        }
    }, []);

    useEffect(() => {
        const element = toolbarRef.current;
        if (element) {
            const checkPosition = () => {
                const rect = element.getBoundingClientRect();
                setToolbarPosX(rect.left)
            };
            checkPosition();
            const resizeObserver = new ResizeObserver(checkPosition);
            resizeObserver.observe(element);
            const mutationObserver = new MutationObserver(checkPosition);
            mutationObserver.observe(document.body, { childList: true, subtree: true });
            return () => {
                resizeObserver.disconnect();
                mutationObserver.disconnect();
            };
        }
    }, []);

    const backgroundStyle = backgroundColor ? toolbarBackgroundStyle(backgroundColor, backgroundImg) : {};
    return <Toolbar
        ref={toolbarRef}
        style={{
            ...style,
            ...backgroundStyle,
            width: 'calc(100vw - ' + (toolbarPosX ?? 0) + 'px)'
        }}
        sx={{
            minHeight: '36px !important',
            lineHeight: '0.5em',
            zIndex: (theme) => theme.zIndex.drawer + 100
        }}>
        <Typography
            variant="caption"
            component="div"
            title={title + (comandaVersion ? ' v' + comandaVersion : '')}
            sx={{
                flexGrow: 1,
                alignSelf: 'flex-start',
                fontSize: '14px',
                fontWeight: 'bold',
                mt: 1,
                color: '#F6F6F6',
            }}>
            {(title ? title : '') + (comandaVersion ? ' v' + comandaVersion : '')}
            <span id="versioData" style={{ color: backgroundColor, marginLeft: '16px' }}>
                ({buildTimestamp} | Revisió: {scmRevision})
            </span>
        </Typography>
        {logos && logos.map((logo) =>
            <Box sx={{ mr: 0, pt: 0, pr: 0, height: '36px', cursor: 'pointer', ...logoStyle }} key={logo}>
                <img src={logo} alt="foot_logo" style={{maxHeight: '36px'}}/>
            </Box>)
        }
    </Toolbar>;
}

export default Footer;
