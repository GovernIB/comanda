import {BaseEntity, IBaseEntity} from "./base-entity.model.ts";

export const NUM_ELEMENT_PAGE_OPTIONS = ['AUTOMATIC', '_10', '_20', '_50', '_100', '_200'] as const;
function isNumElementPageOption(value: unknown): value is typeof NUM_ELEMENT_PAGE_OPTIONS[number] {
    // Se transforma NUM_ELEMENT_PAGE_OPTIONS a un array de strings para poder usar includes contra
    // un valor de tipo string cualquiera
    const optionsAsStringArray: readonly string[] = NUM_ELEMENT_PAGE_OPTIONS;
    return typeof value === 'string' && (optionsAsStringArray).includes(value);
}

export interface IUsuari extends IBaseEntity {
    codi: string;
    nom: string;
    nif: string | undefined;
    email: string | undefined;
    emailAlternatiu: string | undefined;
    idioma: LanguageEnum;
    temaAplicacio: TemaAplicacio | undefined;
    estilMenu: MenuEstil;
    rols: string[] | undefined;
    alarmaMail: boolean | undefined;
    alarmaMailAgrupar: boolean | undefined;

    numElementsPagina: typeof NUM_ELEMENT_PAGE_OPTIONS[number];
}

export class UsuariModel extends BaseEntity implements Required<IUsuari> {

    // Claves estáticas para poder usarlas como keyof
    static readonly CODI: keyof UsuariModel = "codi";
    static readonly NOM: keyof UsuariModel = "nom";
    static readonly NIF: keyof UsuariModel = "nif";
    static readonly EMAIL: keyof UsuariModel = "email";
    static readonly EMAIL_ALTERNATIU: keyof UsuariModel = "emailAlternatiu";
    static readonly IDIOMA: keyof UsuariModel = "idioma";
    static readonly TEMA_APLICACIO: keyof UsuariModel = "temaAplicacio";
    static readonly ESTIL_MENU: keyof UsuariModel = "estilMenu";
    static readonly ROLS: keyof UsuariModel = "rols";
    static readonly NUM_ELEMENTS_PAGINA: keyof UsuariModel = "numElementsPagina";
    static readonly ALARMA_MAIL: keyof UsuariModel = "alarmaMail";
    static readonly ALARMA_MAIL_AGRUPAT: keyof UsuariModel = "alarmaMailAgrupar";

    codi: string;
    nom: string;
    nif: string | undefined;
    email: string | undefined;
    emailAlternatiu: string | undefined;
    idioma: LanguageEnum;
    temaAplicacio: TemaAplicacio | undefined;
    estilMenu!: MenuEstil;
    rols: string[] | undefined;
    numElementsPagina: typeof NUM_ELEMENT_PAGE_OPTIONS[number];
    alarmaMail: boolean | undefined;
    alarmaMailAgrupar: boolean | undefined;

    constructor(usuari: IUsuari) {
        super(usuari);
        this.codi = usuari.codi;
        this.nom = usuari.nom;
        this.idioma = usuari.idioma;
        this.numElementsPagina = usuari.numElementsPagina;
        this.alarmaMail = usuari.alarmaMail;
        this.alarmaMailAgrupar = usuari.alarmaMailAgrupar;
        Object.assign(this, usuari);
    }
}

export enum LanguageEnum {
    CA = "CA",
    ES = "ES"
}

export enum TemaAplicacio {
    CLAR = "CLAR",
    OBSCUR = "OBSCUR",
    DRACULA = "DRACULA",
    SISTEMA = "SISTEMA",
}

export enum MenuEstil {
    TEMA = "TEMA",
    TEMA_INVERTIT = "TEMA_INVERTIT",
    PEU = "PEU",
}

export function isIUsuari(obj: unknown): obj is IUsuari {
    if (typeof obj !== 'object' || obj === null) return false;
    const u = obj as Record<string, unknown>;
    return (
        typeof u.id === 'number' &&
        typeof u.codi === 'string' &&
        typeof u.nom === 'string' &&
        (u.nif === undefined || typeof u.nif === 'string') &&
        (u.email === undefined || typeof u.email === 'string') &&
        (u.emailAlternatiu === undefined || typeof u.emailAlternatiu === 'string') &&
        Object.values(LanguageEnum).includes(u.idioma as LanguageEnum) &&
        (u.temaAplicacio === undefined || Object.values(TemaAplicacio).includes(u.temaAplicacio as TemaAplicacio)) &&
        Object.values(MenuEstil).includes(u.estilMenu as MenuEstil) &&
        (u.rols === undefined || (Array.isArray(u.rols) && u.rols.every(r => typeof r === 'string'))) &&
        (u.alarmaMail === undefined || typeof u.alarmaMail === 'boolean') &&
        (u.alarmaMailAgrupar === undefined || typeof u.alarmaMailAgrupar === 'boolean') &&
        isNumElementPageOption(u.numElementsPagina)
    );
}
