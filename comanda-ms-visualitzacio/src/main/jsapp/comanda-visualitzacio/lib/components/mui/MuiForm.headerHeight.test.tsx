import React from 'react';
import { render } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MuiBaseAppContext, MuiBaseAppAppearance } from './MuiBaseAppContext';
import { BaseAppContext } from '../BaseAppContext';
import * as smallScreen from '../../util/useSmallScreen';

// Es mockegen els contextos de formulari/detall per renderitzar només la barra d'eines.
vi.mock('../form/Form', () => ({
    Form: ({ children }: any) => <>{children}</>,
    useFormApiContext: () => ({ current: undefined }),
}));
vi.mock('../form/FormContext', () => ({
    useFormContext: () => ({
        modified: false,
        isSaveActionPresent: false,
        isDeleteActionPresent: false,
    }),
}));
vi.mock('../detail/Detail', () => ({
    Detail: ({ children }: any) => <>{children}</>,
}));

import { MuiForm } from './form/MuiForm';
import { MuiDetail } from './detail/MuiDetail';

const baseApp: any = {
    t: (k: string) => k,
    goBack: () => {},
    anyHistoryEntryExist: () => false,
    contentExpandsToAvailableHeight: false,
};

const stickyTops = (container: HTMLElement) =>
    Array.from(container.querySelectorAll('*'))
        .filter((e) => getComputedStyle(e).position === 'sticky')
        .map((e) => getComputedStyle(e).top);

const renderWith = (el: React.ReactElement, appearance?: MuiBaseAppAppearance) =>
    render(
        <BaseAppContext.Provider value={baseApp}>
            <MuiBaseAppContext.Provider
                value={{ defaultMuiComponentProps: {}, appearance: appearance ?? {} }}>
                {el}
            </MuiBaseAppContext.Provider>
        </BaseAppContext.Provider>
    );

describe.each([
    ['MuiForm', <MuiForm key="f" id="1" resourceName="x" />],
    ['MuiDetail', <MuiDetail key="d" id="1" resourceName="x" />],
])('%s top sticky', (_name, element) => {
    it('per defecte 64px', () => {
        vi.spyOn(smallScreen, 'useSmallHeader').mockReturnValue(false);
        expect(stickyTops(renderWith(element).container)).toContain('64px');
    });
    it('per defecte 56px en pantalla petita', () => {
        vi.spyOn(smallScreen, 'useSmallHeader').mockReturnValue(true);
        expect(stickyTops(renderWith(element).container)).toContain('56px');
    });
    it('fa servir headerHeight indicat, també en pantalla petita', () => {
        vi.spyOn(smallScreen, 'useSmallHeader').mockReturnValue(true);
        expect(stickyTops(renderWith(element, { headerHeight: 74 }).container)).toContain('74px');
    });
});
