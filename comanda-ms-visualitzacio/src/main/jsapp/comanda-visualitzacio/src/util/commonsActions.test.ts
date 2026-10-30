import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { iniciaDescarga, iniciaDescargaBlob, iniciaDescargaJSON, iniciaDescargaCSV } from './commonsActions';

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

describe('iniciaDescargaCSV', () => {
    let appendChildSpy: any;
    let removeChildSpy: any;
    let createObjectUrlSpy: any;
    let revokeObjectUrlSpy: any;
    let clickSpy: any;

    beforeEach(() => {
        appendChildSpy = vi.spyOn(document.body, 'appendChild');
        removeChildSpy = vi.spyOn(document.body, 'removeChild');
        clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
        if (!URL.createObjectURL) {
            Object.defineProperty(URL, 'createObjectURL', {
                value: vi.fn(),
                writable: true,
            });
        }
        createObjectUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:http://localhost/csv');
        revokeObjectUrlSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {});
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('iniciaDescargaCSV_quanRepArrayBytesSignatsJava_descodificaUtf8MultibyteIInclouBom', async () => {
        const textOriginal = "Data;Estat\n2026-03-13;Avís;Caiguda;Degradada";
        const utf8Bytes = new TextEncoder().encode(textOriginal);
        // Simula bytes signats com els que retorna Java byte[] serialitzat (-128 a 127)
        const signedBytes = Array.from(utf8Bytes).map(b => (b > 127 ? b - 256 : b));

        iniciaDescargaCSV({
            name: 'informe.csv',
            content: signedBytes,
        });

        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        // Comprova la presència del BOM UTF-8 (EF BB BF)
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);
        const textDecoded = new TextDecoder('utf-8').decode(bytes.slice(3));
        expect(textDecoded).toBe(textOriginal);

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('informe.csv');
        expect(clickSpy).toHaveBeenCalledTimes(1);
        expect(removeChildSpy).toHaveBeenCalledWith(link);
        expect(revokeObjectUrlSpy).toHaveBeenCalledWith('blob:http://localhost/csv');
    });

    it('iniciaDescargaCSV_quanNoRepNom_utilitzaElNomPerDefecteExportCsv', async () => {
        iniciaDescargaCSV({
            content: 'col1;col2\nval1;val2',
        });

        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);
        const textDecoded = new TextDecoder('utf-8').decode(bytes.slice(3));
        expect(textDecoded).toBe('col1;col2\nval1;val2');

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('export.csv');
    });

    it('iniciaDescargaCSV_quanElTextJaConteBom_noDuplicaElBom', async () => {
        iniciaDescargaCSV({
            fileName: 'amb_bom.csv',
            content: '\uFEFFjaTeBom;val',
        });

        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);
        // Assegura que no s'ha duplicat el BOM (no hi ha un segon EF BB BF)
        expect(Array.from(bytes.slice(3, 6))).not.toEqual([0xEF, 0xBB, 0xBF]);
        const textDecoded = new TextDecoder('utf-8').decode(bytes.slice(3));
        expect(textDecoded).toBe('jaTeBom;val');

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('amb_bom.csv');
    });

    it('iniciaDescargaCSV_quanRepUnBlob_hiAfegeixElBomIIniciaLaDescarga', async () => {
        const rawBlob = new Blob(['fila1;fila2'], { type: 'text/csv' });

        iniciaDescargaCSV({
            name: 'dades_blob.csv',
            blob: rawBlob,
        });

        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);
        const textDecoded = new TextDecoder('utf-8').decode(bytes.slice(3));
        expect(textDecoded).toBe('fila1;fila2');

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('dades_blob.csv');
        expect(clickSpy).toHaveBeenCalledTimes(1);
    });

    it('iniciaDescargaCSV_quanRepBlobComAString_generaBlobAmbBom', async () => {
        iniciaDescargaCSV({
            blob: 'textDesDeBlob',
        });

        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);
        const textDecoded = new TextDecoder('utf-8').decode(bytes.slice(3));
        expect(textDecoded).toBe('textDesDeBlob');

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('export.csv');
    });

    it('iniciaDescargaCSV_quanRepDirectamentUnBlob_hiAfegeixBomIUtilitzaExportCsv', async () => {
        const directBlob = new Blob(['colA;colB\n1;2'], { type: 'text/csv' });

        iniciaDescargaCSV(directBlob);

        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);
        const textDecoded = new TextDecoder('utf-8').decode(bytes.slice(3));
        expect(textDecoded).toBe('colA;colB\n1;2');

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('export.csv');
    });

    it('iniciaDescargaCSV_quanRepUint8Array_descodificaCorrectament', async () => {
        const textOriginal = 'capçalera;detall\nval;català';
        const bytesInput = new TextEncoder().encode(textOriginal);

        iniciaDescargaCSV({
            fileName: 'uint8.csv',
            content: bytesInput,
        });

        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);
        const textDecoded = new TextDecoder('utf-8').decode(bytes.slice(3));
        expect(textDecoded).toBe(textOriginal);

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('uint8.csv');
    });

    it('iniciaDescargaCSV_quanElNomConteCometes_lesEliminaCorrectament', () => {
        iniciaDescargaCSV({
            fileName: '"cometes.csv"',
            content: 'a;b',
        });

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('cometes.csv');
    });

    it('iniciaDescargaCSV_quanResultEsNullOUndefined_descarregaFitxerBuitAmbNomPerDefecte', async () => {
        iniciaDescargaCSV(null);

        expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
        const generatedBlob = createObjectUrlSpy.mock.calls[0][0] as Blob;
        const bytes = new Uint8Array(await generatedBlob.arrayBuffer());
        expect(Array.from(bytes.slice(0, 3))).toEqual([0xEF, 0xBB, 0xBF]);

        const link = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
        expect(link.download).toBe('export.csv');
    });
});

