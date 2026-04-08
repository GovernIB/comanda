import React, {useMemo} from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Box, Typography, List, ListItem, ListItemText, Link, Divider, Icon } from '@mui/material';
import { useTranslation } from 'react-i18next';
import PageTitle from '../components/PageTitle.tsx';
import {useAppEntries} from "../App.tsx"
import {MenuEntry} from "reactlib";

const SitemapItem = ({id, icon, title, to, children}: MenuEntry) => {
    return <>
        {to && <ListItem key={id} disablePadding>
            <ListItemText
                primary={
                    <Link component={RouterLink} to={to} display={'flex'} alignItems={'center'} underline="hover">
                        {icon && <Icon sx={{mr: 1}}>{icon}</Icon>}
                        {title}
                    </Link>
                }
            />
        </ListItem>}
        {children?.map?.((item:MenuEntry) => (
            <SitemapItem key={item.id} {...item}/>
        ))}
    </>
}

const Sitemap: React.FC = () => {
    const { t } = useTranslation();

    const {caibMenuEntries} = useAppEntries();

    const sitemap:any[] = useMemo(()=> {
        return [
            ...caibMenuEntries,
            // additional
            { id: 'accessibilitat', title: t($ => $.menu.accessibilitat), icon: 'info', to: '/accessibilitat' }
        ]
    },[caibMenuEntries, t])

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
                {sitemap.map((site:MenuEntry) => (
                    <SitemapItem key={site.id} {...site}/>
                ))}
            </List>
        </Box>
    );
};

export default Sitemap;
