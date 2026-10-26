import { render } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BaseAppContext } from '../BaseAppContext';

// El formulari es substitueix per un element buit: només interessa la persistència.
vi.mock('./Form', () => ({
    default: () => null,
    useFormApiRef: () => ({ current: null }),
}));
vi.mock('../AuthContext', () => ({
    useAuthContext: () => ({ isAuthenticated: false, getUserId: () => 'u' }),
}));

import { Filter } from './Filter';

const baseApp: any = { code: 'app' };

const renderFilters = (extra: any = {}, codes: string[] = ['A', 'B']) =>
    render(
        <BaseAppContext.Provider value={baseApp}>
            {codes.map((code) => (
                <Filter
                    key={code}
                    resourceName="res"
                    code={code}
                    persistentStateActive
                    springFilterBuilder={() => undefined}
                    initialData={{ v: code }}
                    {...extra}
                />
            ))}
        </BaseAppContext.Provider>
    );

const storedKeys = () =>
    Array.from({ length: sessionStorage.length }, (_, i) => sessionStorage.key(i) as string)
        .filter((k) => k.includes('_FLT_'))
        .sort();

describe('Filter clau de persistència', () => {
    beforeEach(() => {
        sessionStorage.clear();
        localStorage.clear();
    });
    it('per defecte dos filtres del mateix recurs comparteixen la clau', () => {
        renderFilters();
        expect(storedKeys()).toEqual(['app_FLT_RES']);
    });
    it('amb persistentStateKeyIncludesCode la clau inclou el codi', () => {
        renderFilters({ persistentStateKeyIncludesCode: true });
        expect(storedKeys()).toEqual(['app_FLT_RES:A', 'app_FLT_RES:B']);
        expect(JSON.parse(sessionStorage.getItem('app_FLT_RES:A')!)).toEqual({ v: 'A' });
        expect(JSON.parse(sessionStorage.getItem('app_FLT_RES:B')!)).toEqual({ v: 'B' });
    });
    it('amb persistentStateKey explícita i el codi', () => {
        renderFilters({ persistentStateKeyIncludesCode: true, persistentStateKey: 'k' }, ['A']);
        expect(storedKeys()).toEqual(['app_FLT_K:A']);
    });
});
