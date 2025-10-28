import {BaseEntity, IBaseEntity} from "./base-entity.model.ts";

export const NUM_ELEMENT_PAGE_OPTIONS = ['AUTOMATIC', '_20', '_50', '_100', '_200'] as const;

export interface IUsuari extends IBaseEntity {
    codi: string;
    nom: string;
    nif: string | undefined;
    email: string | undefined;
    emailAlternatiu: string | undefined;
    idioma: LanguageEnum;
    temaObscur: boolean | undefined;
    rols: string[] | undefined;

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
    static readonly TEMA_OBSCUR: keyof UsuariModel = "temaObscur";
    static readonly ROLS: keyof UsuariModel = "rols";
    static readonly NUM_ELEMENTS_PAGINA: keyof UsuariModel = "numElementsPagina";

    codi: string;
    nom: string;
    nif: string | undefined;
    email: string | undefined;
    emailAlternatiu: string | undefined;
    idioma: LanguageEnum;
    temaObscur: boolean | undefined;
    rols: string[] | undefined;
    numElementsPagina: typeof NUM_ELEMENT_PAGE_OPTIONS[number];

    constructor(usuari: IUsuari) {
        super(usuari);
        this.codi = usuari.codi;
        this.nom = usuari.nom;
        this.idioma = usuari.idioma;
        this.numElementsPagina = usuari.numElementsPagina;
        Object.assign(this, usuari);
    }
}

export enum LanguageEnum {
    CA = "CA",
    ES = "ES"
}
