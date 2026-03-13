import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { iniciaDescarga, iniciaDescargaBlob, iniciaDescargaJSON } from './commonsActions';

describe('iniciaDescarga', () => {
    if (!URL.revokeObjectURL) {
        Object.defineProperty(URL, 'revokeObjectURL', {
            value: vi.fn(),
            writable: true,
        });
    }

    let appendChildSpy: any;
    let removeChildSpy: any;
    let revokeObjectUrlSpy: any;

    beforeEach(() => {
        appendChildSpy = vi.spyOn(document.body, 'appendChild');
        removeChildSpy = vi.spyOn(document.body, 'removeChild');
        revokeObjectUrlSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {});
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('iniciaDescarga_quanRepUnaUrlICadenaNom_clickaLEnllacITancaElsRecursos', () => {
        // Comprova que la utilitat crea l'enllaç temporal, el clicka i neteja els recursos del DOM.
        const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

        iniciaDescarga('blob:http://localhost/fitxer', 'prova.txt');

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.href).toBe('blob:http://localhost/fitxer');
        expect(link.download).toBe('prova.txt');
        expect(clickSpy).toHaveBeenCalledTimes(1);
        expect(removeChildSpy).toHaveBeenCalledWith(link);
        expect(revokeObjectUrlSpy).toHaveBeenCalledWith('blob:http://localhost/fitxer');
    });
});

describe('iniciaDescargaBlob', () => {
    it('iniciaDescargaBlob_quanRepUnBlob_creaUnaObjectUrlIIniciaLaDescarga', () => {
        // Verifica que la utilitat genera una object URL abans d'invocar la descàrrega.
        if (!URL.createObjectURL) {
            Object.defineProperty(URL, 'createObjectURL', {
                value: vi.fn(),
                writable: true,
            });
        }
        const createObjectUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:http://localhost/blob');
        const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

        iniciaDescargaBlob({
            fileName: 'blob.txt',
            blob: new Blob(['hola'], { type: 'text/plain' }),
        });

        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        expect(clickSpy).toHaveBeenCalledTimes(1);
    });
});

describe('iniciaDescargaJSON', () => {
    it('iniciaDescargaJSON_quanRepUnObjecte_serialitzaElContingutIElDescarregaComAJson', () => {
        // Comprova que la utilitat converteix l'objecte a JSON abans de construir el blob de descàrrega.
        if (!URL.createObjectURL) {
            Object.defineProperty(URL, 'createObjectURL', {
                value: vi.fn(),
                writable: true,
            });
        }
        const createObjectUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:http://localhost/json');
        const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

        iniciaDescargaJSON({
            fileName: 'dades.json',
            blob: { id: 7, nom: 'alpha' },
        });

        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        expect(clickSpy).toHaveBeenCalledTimes(1);
    });
});
