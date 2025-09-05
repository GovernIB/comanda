import {BaseEntity, IBaseEntity} from "./base-entity.model.ts";

export interface IUsuari extends IBaseEntity {
    codi: string;
    nom: string;
    nif: string | undefined;
    email: string | undefined;
    emailAlternatiu: string | undefined;
    idioma: LanguageEnum | undefined;
    rols: string[] | undefined;

    numElementsPagina: number | undefined;
}

export class UsuariModel extends BaseEntity implements Required<IUsuari> {

    // Claves estáticas para poder usarlas como keyof
    static readonly CODI: keyof UsuariModel = "codi";
    static readonly NOM: keyof UsuariModel = "nom";
    static readonly NIF: keyof UsuariModel = "nif";
    static readonly EMAIL: keyof UsuariModel = "email";
    static readonly EMAIL_ALTERNATIU: keyof UsuariModel = "emailAlternatiu";
    static readonly IDIOMA: keyof UsuariModel = "idioma";
    static readonly ROLS: keyof UsuariModel = "rols";
    static readonly NUM_ELEMENTS_PAGINA: keyof UsuariModel = "numElementsPagina";

    codi: string;
    nom: string;
    nif: string | undefined;
    email: string | undefined;
    emailAlternatiu: string | undefined;
    idioma: LanguageEnum | undefined;
    rols: string[] | undefined;
    numElementsPagina: number | undefined;

    constructor(usuari: IUsuari) {
        super(usuari);
        this.codi = usuari.codi;
        this.nom = usuari.nom;
        Object.assign(this, usuari);
    }
}

export enum LanguageEnum {
    CA = "CA",
    ES = "ES"
}