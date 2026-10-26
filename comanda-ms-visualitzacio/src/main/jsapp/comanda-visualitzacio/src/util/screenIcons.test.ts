import { describe, expect, it } from 'vitest';
import { icons } from '../lot2/icons';
import { screenIcons } from './screenIcons';

describe('screenIcons', () => {
    it('screenIcons_avis_esLaIconaComunaDeLot2', () => {
        expect(screenIcons.avis).toBe(icons.avis);
        expect(screenIcons.avis).toBe('campaign');
    });
});
