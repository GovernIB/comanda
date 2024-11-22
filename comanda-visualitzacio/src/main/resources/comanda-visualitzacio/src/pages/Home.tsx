import * as React from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Typography from '@mui/material/Typography';
import { BasePage } from 'reactlib';
import { DashboardWidget } from '../components/DashboardWidget';

const Home: React.FC = () => {
    return <BasePage>
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: 'calc(100vh - 80px)',
            }}>
            <Typography variant="h2" sx={{ mb: 2 }}>Home</Typography>
            <Grid container spacing={2}>
                <Grid size={3}>
                    <DashboardWidget
                        resourceName="app"
                        to="/"
                        icon="home" />
                </Grid>
                <Grid size={3}>
                    <DashboardWidget
                        resourceName="app"
                        to="/"
                        icon="home" />
                </Grid>
                <Grid size={3}>
                    <DashboardWidget
                        resourceName="app"
                        to="/ape"
                        icon="home" />
                </Grid>
                <Grid size={3}>
                    <DashboardWidget
                        resourceName="app"
                        to="/api"
                        icon="home" />
                </Grid>
            </Grid>
        </Box>

    </BasePage>;
}

export default Home;
