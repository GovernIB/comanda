import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Box, Typography, List, ListItem, ListItemText, Link, Divider } from '@mui/material';
import { useTranslation } from 'react-i18next';
import PageTitle from '../components/PageTitle.tsx';

const Sitemap: React.FC = () => {
    const { t } = useTranslation();
    const sitemapLinks = [
        { label: t($ => $.menu.salut), path: '/' },
        { label: t($ => $.menu.dashboard), path: '/dashboard' },
        { label: t($ => $.menu.estadistiques), path: '/estadistiques' },
        { label: t($ => $.menu.app), path: '/app' },
        { label: t($ => $.menu.entorn), path: '/entorn' },
        { label: t($ => $.menu.versionsEntorn), path: '/versionsEntorn' },
        { label: t($ => $.menu.monitor), path: '/monitor' },
        { label: t($ => $.menu.cache), path: '/cache' },
        { label: t($ => $.menu.integracio), path: '/integracio' },
        { label: t($ => $.menu.dimensio), path: '/dimensio' },
        { label: t($ => $.menu.indicador), path: '/indicador' },
        { label: t($ => $.menu.widget), path: '/estadisticaWidget' },
        { label: t($ => $.menu.calendari), path: '/calendari' },
        { label: t($ => $.menu.tasca), path: '/tasca' },
        { label: t($ => $.menu.avis), path: '/avis' },
        { label: t($ => $.menu.broker), path: '/broker' },
        { label: t($ => $.menu.parametre), path: '/parametre' },
        { label: t($ => $.menu.accessibilitat), path: '/accessibilitat' },
    ];

    return (
        <Box sx={{ maxWidth: 600, mx: 'auto', p: 3 }}>
            <PageTitle title={t($ => $.page.sitemap.title)} />
            <Typography variant="h4" component="h1" gutterBottom>
                {t($ => $.page.sitemap.title)}
            </Typography>

            <Typography variant="body1" color="text.secondary" gutterBottom>
                {t($ => $.page.sitemap.subtitle)}
            </Typography>

            <Divider sx={{ my: 2 }} />

            <List>
                {sitemapLinks.map(({ label, path }) => (
                    <ListItem key={path} disablePadding>
                        <ListItemText
                            primary={
                                <Link component={RouterLink} to={path} underline="hover">
                                    {label}
                                </Link>
                            }
                        />
                    </ListItem>
                ))}
            </List>
        </Box>
    );
};

export default Sitemap;
