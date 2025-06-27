import Button from '@mui/material/Button';
import MenuIcon from '@mui/icons-material/Menu';
import React, { PropsWithChildren, useState } from 'react';
import { Menu } from '@mui/material';

type ButtonMenuProps = {
    title: string;
    disabled?: boolean;
    buttonIcon?: React.ReactElement;
};

const ButtonMenu: React.FC<PropsWithChildren<ButtonMenuProps>> = ({ buttonIcon, title, disabled, children }) => {
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };
    return (
        <>
            <Button
                onClick={handleClick}
                endIcon={buttonIcon ?? <MenuIcon />}
                disabled={disabled}
            >
                {title}
            </Button>
            <Menu
                anchorEl={anchorEl}
                id="account-menu"
                open={open}
                onClose={handleClose}
                onClick={handleClose}
                >
                {children}
            </Menu>
        </>
    );
};

export default ButtonMenu;
