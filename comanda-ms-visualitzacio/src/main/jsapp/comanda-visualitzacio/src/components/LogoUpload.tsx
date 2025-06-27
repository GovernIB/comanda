// Custom component for logo upload with image resizing
import * as React from "react";
import {useTranslation} from "react-i18next";
import {useFormContext} from "reactlib";
import {FormFieldDataActionType} from "../../lib/components/form/FormContext.tsx";
import {Box, Button, Typography} from "@mui/material";

const LogoUpload: React.FC = () => {
    const {t} = useTranslation();
    const {data, dataDispatchAction} = useFormContext();
    const [previewUrl, setPreviewUrl] = React.useState<string | null>(null);
    const fileInputRef = React.useRef<HTMLInputElement>(null);

    // Initialize preview if logo exists
    React.useEffect(() => {
        if (data?.logo) {
            setPreviewUrl(`data:image/png;base64,${data.logo}`);
        }
    }, [data?.logo]);

    const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                // Resize image to max height of 64px
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');

                // Calculate new dimensions maintaining aspect ratio
                let width = img.width;
                let height = img.height;

                if (height > 64) {
                    width = Math.floor(width * (64 / height));
                    height = 64;
                }

                canvas.width = width;
                canvas.height = height;

                // Draw resized image to canvas
                ctx?.drawImage(img, 0, 0, width, height);

                // Convert canvas to base64 (without data:image/png;base64, prefix)
                const base64 = canvas.toDataURL('image/png').split(',')[1];

                // Update preview
                setPreviewUrl(`data:image/png;base64,${base64}`);

                // Update form data
                dataDispatchAction({
                    type: FormFieldDataActionType.FIELD_CHANGE,
                    payload: {fieldName: "logo", value: base64}
                });
                // updateData({ ...data, logo: base64 });
            };
            img.src = e.target?.result as string;
        };
        reader.readAsDataURL(file);
    };

    const handleButtonClick = () => {
        fileInputRef.current?.click();
    };

    const handleRemoveLogo = () => {
        setPreviewUrl(null);
        dataDispatchAction({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: {fieldName: "logo", value: null}
        });
        // updateData({ ...data, logo: null });
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    return (
        <Box>
            <Typography variant="subtitle1">{t('page.apps.logo')}</Typography>
            <Box sx={{display: 'flex', alignItems: 'center', mt: 1, mb: 2}}>
                {previewUrl && (
                    <Box sx={{mr: 2, border: '1px solid #ccc', p: 1, borderRadius: 1}}>
                        <img src={previewUrl} alt="Logo" style={{maxHeight: '64px'}}/>
                    </Box>
                )}
                <Box>
                    <input
                        type="file"
                        accept="image/*"
                        style={{display: 'none'}}
                        ref={fileInputRef}
                        onChange={handleFileSelect}
                    />
                    <Button
                        variant="contained"
                        onClick={handleButtonClick}
                        sx={{mr: 1}}
                    >
                        {previewUrl ? t('page.apps.changeLogo') : t('page.apps.uploadLogo')}
                    </Button>
                    {previewUrl && (
                        <Button
                            variant="outlined"
                            color="error"
                            onClick={handleRemoveLogo}
                        >
                            {t('page.apps.removeLogo')}
                        </Button>
                    )}
                </Box>
            </Box>
        </Box>
    );
};

export default LogoUpload;