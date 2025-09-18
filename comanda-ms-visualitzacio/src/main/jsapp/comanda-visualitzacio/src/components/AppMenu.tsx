import React from 'react';
import { MenuEntry } from '../../lib/components/mui/Menu';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import SideMenu from "./SideMenu.tsx";

const menuIcon = 'menu';

export interface AppMenuProps {
  menuEntries: MenuEntry[];
}

export const AppMenu: React.FC<AppMenuProps> = ({ menuEntries }) => {
  const [open, setOpen] = React.useState(false);

  const toggleMenu = () => {
    setOpen(!open);
  };

  return (
    <>
      <IconButton
        color="default"
        aria-label="open menu"
        onClick={toggleMenu}
        edge="start"
        sx={{ mr: 2 }}
      >
        <Icon sx={{ fontSize: '24px'}} fontSize="medium">{menuIcon}</Icon>
      </IconButton>
      {open && <SideMenu
          entries={menuEntries}
          drawerWidth={350}
          iconClicked={open}
          onTitleClose={() => setOpen(false)}
          onClose={() => setOpen(false)}
      />}
    </>
  );
};

export default AppMenu;
