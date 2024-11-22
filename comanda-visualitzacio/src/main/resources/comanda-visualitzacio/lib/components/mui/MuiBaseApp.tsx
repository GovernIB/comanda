import React from 'react';
import { useBaseAppContext } from '../BaseAppContext';
import { useAnswerRequiredDialogButtons } from '../AppButtons';
import { BaseApp, BaseAppProps } from '../BaseApp';
import { useOptionalResourceApiContext } from '../ResourceApiContext';
import { useMessageDialog } from './Dialog';
import { useTemporalMessage } from './TemporalMessage';
import AppBar from './AppBar';
import Menu, { MenuEntry } from './Menu';
import OfflineMessage from './OfflineMessage';
import { useToolbarMenuIcon } from './ToolbarMenuIcon';

export type MuiBaseAppProps = Omit<BaseAppProps, 'contentComponentSlots'> & {
    title: string;
    version?: string;
    logo?: string;
    logoStyle?: any;
    menuTitle?: string;
    menuEntries?: MenuEntry[];
    menuOnTitleClose?: () => void;
    menuShrinkDisabled?: boolean;
    menuWidth?: number,
    additionalHeaderComponents?: React.ReactElement | React.ReactElement[];
    appbarStyle?: any;
    appbarBackgroundColor?: string;
    appbarBackgroundImg?: string;
};

const MuiComponentsConfigurer: React.FC = () => {
    const [messageDialogShow, messageDialogComponent] = useMessageDialog();
    const [temporalMessageShow, temporalMessageComponent] = useTemporalMessage();
    const resourceApiContext = useOptionalResourceApiContext();
    const {
        setMessageDialogShow,
        setTemporalMessageShow,
    } = useBaseAppContext();
    const getAnswerRequiredButtons = useAnswerRequiredDialogButtons();
    const openAnswerRequiredDialog = (
        title: string | undefined,
        question: string,
        trueFalseAnswerRequired: boolean,
        availableAnswers?: string[]) => {
        return messageDialogShow(
            title ?? 'Atenció',
            question,
            getAnswerRequiredButtons(trueFalseAnswerRequired, availableAnswers));
    }
    React.useEffect(() => {
        setMessageDialogShow(messageDialogShow);
        setTemporalMessageShow(temporalMessageShow);
        if (resourceApiContext != null) {
            resourceApiContext.setOpenAnswerRequiredDialog(openAnswerRequiredDialog);
        }
    }, [])
    return <>
        {messageDialogComponent}
        {temporalMessageComponent}
    </>;
}

export const MuiBaseApp: React.FC<MuiBaseAppProps> = (props) => {
    const {
        title,
        version,
        logo,
        logoStyle,
        menuTitle,
        menuEntries,
        menuOnTitleClose,
        menuShrinkDisabled,
        menuWidth,
        appbarStyle,
        appbarBackgroundColor,
        appbarBackgroundImg,
        additionalHeaderComponents,
        children,
        ...otherProps
    } = props;
    const { shrink, buttonComponent: menuButton } = useToolbarMenuIcon();
    const appbarComponent = <AppBar
        title={title}
        version={version}
        logo={logo}
        logoStyle={logoStyle}
        menuButton={!menuShrinkDisabled && menuEntries != null ? menuButton : undefined}
        additionalComponents={additionalHeaderComponents}
        style={appbarStyle}
        backgroundColor={appbarBackgroundColor}
        backgroundImg={appbarBackgroundImg} />;
    const menuComponent = menuEntries != null ? <Menu
        title={menuTitle}
        entries={menuEntries}
        onTitleClose={menuOnTitleClose}
        drawerWidth={menuWidth}
        shrink={shrink} /> : undefined;
    const offlineComponent = <OfflineMessage />;
    return <BaseApp
        {...otherProps}
        contentComponentSlots={{
            appbar: appbarComponent,
            menu: menuComponent,
            offline: offlineComponent,
        }}>
        <>
            <MuiComponentsConfigurer />
            {children}
        </>
    </BaseApp>;
}

export default MuiBaseApp;