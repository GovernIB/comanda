import { Card, CardContent, CardHeader, Grid, SxProps, Typography } from '@mui/material';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import { ReactNode } from 'react';

const cardHeader = { backgroundColor: '#f5f5f5', borderBottom: '1px solid #e3e3e3' };
const iconButton = {
    p: 0.5,
    borderRadius: '5px',
    maxWidth: 'max-content',
    border: '1px solid grey',
};

type CardButtonProps = {
    text: string;
    icon: string;
    onClick: () => void;
    flex: number;
    buttonProps: SxProps;
    hidden: boolean;
};

const CardButton = (props: CardButtonProps) => {
    const { text, icon, onClick, flex, buttonProps, hidden } = props;

    if (hidden) {
        return <></>;
    }

    return (
        <Grid size={flex ?? 12} sx={{ display: 'flex', justifyContent: 'end', }} >
            <IconButton sx={{ ...iconButton, ...buttonProps }} title={text} onClick={onClick}>
                <Typography
                    sx={{ display: 'flex', alignItems: 'center' }}
                    variant={'caption'}
                    color={'textPrimary'}
                >
                    {icon && <Icon fontSize={'inherit'}>{icon}</Icon>}
                    {text}
                </Typography>
            </IconButton>
        </Grid>
    );
};

export const UserProfileCardData = (props: {
    icon: ReactNode;
    title: string;
    header?: ReactNode;
    children?: ReactNode;
    xs?: number;
    hidden?: boolean;
    buttons?: CardButtonProps[];
    cardProps?: SxProps;
    headerProps?: SxProps;
}) => {
    const {
        icon,
        title,
        header,
        children,
        xs,
        hidden,
        buttons,
        cardProps,
        headerProps = cardHeader,
        ...other
    } = props;

    if (hidden) {
        return <></>;
    }

    return (
        <Grid size={xs ?? 12}>
            <Card elevation={2} sx={{...cardProps,}} >
                {title && (
                    <CardHeader
                        title={
                            <Typography variant="body1" sx={{ display: 'flex' }}>
                                {icon && icon}
                                {title}
                            </Typography>
                        }
                        sx={headerProps}
                    />
                )}
                {header && <CardContent sx={headerProps}>{header}</CardContent>}

                <CardContent hidden={!children}>
                    <Grid container columnSpacing={1} rowSpacing={1} size={12} {...other}>
                        {children}
                        {buttons?.map((button) => (
                            <CardButton key={button?.text} {...button} />
                        ))}
                    </Grid>
                </CardContent>
            </Card>
        </Grid>
    );
};

export const CardPage = (props: {
    title: string;
    header?: ReactNode;
    headerProps?: { backgroundColor: string; borderBottom: string } | undefined;
    children?: ReactNode;
}) => {
    const { title, header, headerProps = cardHeader, children } = props;
    return (
        <Card elevation={2} sx={{height: '100%', display: 'flex', flexDirection: 'column',}} >
            {title && <CardHeader title={title} sx={headerProps} />}
            {header && <CardContent sx={headerProps}>{header}</CardContent>}
            <CardContent sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                {children}
            </CardContent>
        </Card>
    );
};

export const ContenidoData = (props: {
    title: string;
    titleXs: number;
    children: ReactNode;
    textXs: number;
    xs: number;
    componentTitleProps: SxProps;
    componentTextProps: SxProps;
    hidden: boolean;
}) => {
    const {
        title,
        titleXs,
        children,
        textXs,
        xs,
        componentTitleProps,
        componentTextProps,
        hidden,
        ...other
    } = props;

    if (hidden) {
        return <></>;
    }

    return (
        <Grid container direction={'row'} columnSpacing={1} size={xs ?? 12} {...other}>
            <Grid size={titleXs ?? 4}>
                <Typography variant={'body1'} color={'black'} sx={componentTitleProps}>
                    {title}
                </Typography>
            </Grid>
            <Grid size={textXs ?? 8}>
                <Typography variant={'inherit'} color={'textSecondary'} sx={componentTextProps}>
                    {children}
                </Typography>
            </Grid>
        </Grid>
    );
};
