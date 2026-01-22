import React, { useMemo } from 'react';
import { useResourceApiContext } from 'reactlib';
import { Box, CircularProgress, Typography, Fade } from '@mui/material';
import { useTranslation } from 'react-i18next';

interface ProtectedRouteProps {
  /** Un o diversos resources necessaris (s'han de complir tots) */
  resourceName: string | string[];
  children: React.ReactNode;
}

const FullscreenCenter: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <Box
    sx={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      bgcolor: 'background.default',
      zIndex: 1200,
    }}
  >
    {children}
  </Box>
);

/** Mostrarà el contingut si té els resources sol·licitats. */
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ resourceName, children }) => {
    const { t } = useTranslation();
    const { indexState } = useResourceApiContext();

    const hasAccess = useMemo(() => {
    if (!indexState?.links) return undefined;
    const required = Array.isArray(resourceName) ? resourceName : [resourceName];
        return required.every(p => indexState.links.has(p));
    }, [indexState, resourceName]);

    if (hasAccess === undefined)
        return <FullscreenCenter><Fade in><CircularProgress /></Fade></FullscreenCenter>;

    if (!hasAccess)
        return <FullscreenCenter><Typography variant="h3">{t($ => $.page.noPermissions)}</Typography></FullscreenCenter>;

    return <>{children}</>;
};

export default ProtectedRoute;
