// Custom component for logo upload with image resizing
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useFormContext } from 'reactlib';
import { Box, IconButton, SxProps, Typography } from '@mui/material';
import Avatar from '@mui/material/Avatar';
import Icon from '@mui/material/Icon';
import { Theme } from '@mui/material/styles';

type ImageFieldProps = {
    imageSrc: string | null;
    label?: string | null;
    editable: boolean;
    onChange: (event?: React.ChangeEvent<HTMLInputElement>) => void;
    onClear: () => void;
    onDownloadClick: () => void;
    hideDownloadButton?: boolean;
    tooltip?: string;
    placeholder?: React.ReactNode;
    avatarProps?: React.ComponentProps<typeof Avatar>;
    sx?: SxProps<Theme>;
};

const ImageField: React.FC<ImageFieldProps> = ({
    imageSrc,
    label,
    editable,
    onChange,
    onClear,
    onDownloadClick,
    hideDownloadButton,
    tooltip,
    placeholder,
    avatarProps,
    sx,
}) => {
    const buttonAvatarId = React.useId();
    const inputRef = React.useRef<HTMLInputElement>(null);
    const { t } = useTranslation();

    React.useEffect(() => {
        if(!imageSrc && inputRef.current != null) {
            inputRef.current.value = "";
        }
    }, [imageSrc])

    return (
        <>
            {label && <Typography variant="subtitle1">{label}</Typography>}
            <Box
                title={tooltip}
                sx={{
                    borderWidth: 1,
                    borderStyle: 'solid',
                    borderColor: 'grey.400',
                    position: 'relative',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    width: '128px',
                    height: '128px',
                    borderRadius: '9999px',
                    overflow: 'hidden',
                    ...sx,
                }}
            >
                <Box
                    sx={{
                        position: 'absolute',
                        inset: 0,
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        backgroundColor: 'rgba(35,35,35,0.5)',
                        zIndex: '20',
                        opacity: 0,
                        transition: 'opacity 0.5s',
                        '&:hover': {
                            opacity: 1,
                        },
                    }}
                >
                    {editable && (
                        <div title={t($ => $.form.field.file.edit)}>
                            <label
                                htmlFor={buttonAvatarId}
                                style={{
                                    display: 'flex',
                                    padding: '8px',
                                    cursor: 'pointer',
                                }}
                            >
                                <input
                                    type="file"
                                    accept="image/*"
                                    ref={inputRef}
                                    style={{ display: 'none' }}
                                    id={buttonAvatarId}
                                    onChange={onChange}
                                />
                                <Icon sx={{ color: '#ffffff' }}>edit</Icon>
                            </label>
                        </div>
                    )}
                    {!hideDownloadButton && (
                        <div title={t($ => $.form.field.file.download)}>
                            <IconButton onClick={onDownloadClick} sx={{ color: '#ffffff' }}>
                                <Icon>file_download</Icon>
                            </IconButton>
                        </div>
                    )}
                    {editable && (
                        <div title={t($ => $.form.field.file.clear)}>
                            <IconButton
                                onClick={() => {
                                    onClear();
                                }}
                                sx={{ color: '#ffffff' }}
                            >
                                <Icon>delete</Icon>
                            </IconButton>
                        </div>
                    )}
                </Box>
                <Avatar
                    src={imageSrc ?? undefined}
                    variant="square"
                    {...avatarProps}
                    sx={{
                        backgroundColor: 'background.default',
                        color: 'text.secondary',
                        objectFit: 'cover',
                        width: '100%',
                        height: '100%',
                        ...avatarProps?.sx,
                    }}
                >
                    {placeholder ? placeholder : <Icon fontSize="large">image_not_supported</Icon>}
                </Avatar>
            </Box>
        </>
    );
};

type LogoUploadProps = {
    name: string;
    label?: string;
};

const LogoUpload: React.FC<LogoUploadProps> = ({ name = 'logo', label }) => {
    const { data, apiRef } = useFormContext();
    const previewUrl = data[name] ? `data:image/png;base64,${data[name]}` : null;

    const handleFileSelect = (event?: React.ChangeEvent<HTMLInputElement>) => {
        const file = event?.target.files?.[0];
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

                // Update form data
                apiRef.current?.setFieldValue(name, base64);
            };
            img.src = e.target?.result as string;
        };
        reader.readAsDataURL(file);
    };

    const handleRemoveLogo = () => {
        apiRef.current?.setFieldValue(name, null);
    };

    return (
        <ImageField
            imageSrc={previewUrl}
            label={label}
            onChange={handleFileSelect}
            onClear={handleRemoveLogo}
            onDownloadClick={() => {
                if (previewUrl) {
                    const link = document.createElement('a');
                    link.href = previewUrl;
                    link.download = 'logo.png';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                }
            }}
            editable={true}
            hideDownloadButton={previewUrl == null}
            sx={{
                borderRadius: '10px',
            }}
            avatarProps={{
                imgProps: {
                    sx: {
                        objectFit: 'contain',
                    },
                },
            }}
        />
    );
};

export default LogoUpload;
