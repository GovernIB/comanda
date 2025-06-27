import * as React from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Divider from '@mui/material/Divider';
import Typography from '@mui/material/Typography';

type ContentDetailElementProps = {
    label?: string;
    boldLabel?: boolean;
    value?: string | number;
    contentValue?: React.ReactNode;
    subElements?: ContentDetailElementProps[];
};

type ContentDetailProps = {
    title?: string;
    elements?: ContentDetailElementProps[];
    handleClose?: () => void;
};

export const ContentDetail: React.FC<ContentDetailProps> = (props) => {
    const { title, elements } = props;
    return <Box sx={{ minHeight: '100%', p: {xs: 2, sm:4} }}>
        {title && <Typography variant="h5" fontWeight="bold" gutterBottom>
            {title}
        </Typography>}
        <Grid container spacing={1}>
            {elements?.
                filter(item => (item.value !== undefined && item.value !== '') || item.contentValue || (item.subElements !== undefined && item.subElements.length>0)).
                map((item, index, filteredArray) => <React.Fragment key={index}>
                    {!!item.label && <Grid size={{xs:12, sm:4}}>
                        <Typography variant="subtitle1" fontWeight={item.boldLabel ?? true ? "bold" : "medium"} >
                            {item.label}
                        </Typography>
                    </Grid>}
                    <Grid size={{xs:12, sm:(item.label ? 8 : 12)}} >
                        {item.contentValue ?
                            <Box textAlign="justify">{item.contentValue}</Box> :
                            <Typography variant="body1" sx={{ textAlign: 'justify' }}>{item.value}</Typography>}
                    </Grid>
                    {item.subElements?.
                    filter(sub => (sub.value !== undefined && sub.value !== '') || sub.contentValue).
                    map((sub, subIndex) => (
                            <React.Fragment key={`${index}-sub-${subIndex}`}>
                                <Grid size={{xs:12, sm:4}}>
                                <Typography variant="subtitle1" fontWeight={sub.boldLabel ?? false ? "bold" : "medium"} fontStyle='italic' color="text.secondary" sx={{ pl: 2, pt: {xs: (sub.boldLabel ?? false ? 2 : 0)} }} >
                                    {sub.label}
                                </Typography>
                                </Grid>
                                <Grid size={{xs:12, sm:8}} sx={{ pl: {xs:2, sm:0}, pb: {xs: 0, sm: 0}, pt: {xs: 0, sm: (sub.boldLabel ?? false ? 2 : 0),}}}>
                                {sub.contentValue ?
                                    <Box textAlign="justify">{sub.contentValue}</Box> :
                                    <Typography variant="body1" sx={{ textAlign: 'justify' }}>{sub.value}</Typography>}
                                </Grid>
                            </React.Fragment>
                            ))}
                    {(index !== (filteredArray.length - 1)) &&
                    <Grid size={12}>
                        <Divider sx={{ my: 1 }} />
                    </Grid>}
                </React.Fragment>)}
        </Grid>
    </Box>;
}

export default ContentDetail;
