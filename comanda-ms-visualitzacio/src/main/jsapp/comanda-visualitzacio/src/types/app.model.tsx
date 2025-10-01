import {BaseEntity, IBaseEntity} from "./base-entity.model.ts";

export interface IEntornAppModel extends IBaseEntity {
    app    : IAppRef;
    entorn : IEntornRef;

    infoUrl?    : string;
    infoData?   : string;
    versio?     : string;
    activa?     : boolean;

    salutUrl?   : string;

    integracioCount?: number;
    subsistemaCount?: number;

    integracions?   : IAppIntegracio[];
    subsistemes?    : IAppSubsistema[];
    contexts?       : IAppContext[];

    estadisticaInfoUrl? : string | null
    estadisticaUrl?     : string | null
    estadisticaCron?    : string | null

    compactable?              : boolean;
    compactacioSetmanalMesos? : number | null;
    compactacioMensualMesos?  : number | null;
    eliminacioMesos?          : number | null;
}

export interface IAppRef {
    id?             : number;
    description?    : string;
}

export interface IEntornRef {
    id?             : number;
    description?    : string;
}

export interface IIntegracioRef {
    id?             : number;
    description?    : string;
}

export interface IAppIntegracio {
    id?         : number;
    app?        : IAppRef;
    codi?       : string;
    integracio? : IIntegracioRef;
    logo?       : string;
    activa?     : boolean;
}

export interface IAppSubsistema {
    id?     : number;
    codi?   : string;
    nom?    : string;
    actiu?  : boolean;

}

export interface IAppManual {
    id?     : number;
    nom?    : string;
    path?   : string;
}

export interface IAppContext{
    id?     : number;
    codi?   : string;
    nom?    : string;
    path?   : string;
    manuals?: IAppManual[];
    api?    : string;
    actiu?  : boolean;

}

export class EntornAppModel extends BaseEntity implements Partial<IEntornAppModel>{

    app    : IAppRef;
    entorn : IEntornRef;

    infoUrl?    : string;
    infoData?   : string;
    versio?     : string;
    activa?     : boolean;

    salutUrl?   : string;

    integracioCount?: number;
    subsistemaCount?: number;

    integracions?   : IAppIntegracio[];
    subsistemes?    : IAppSubsistema[];
    contexts?       : IAppContext[];

    estadisticaInfoUrl? : string | null
    estadisticaUrl?     : string | null
    estadisticaCron?    : string | null

    compactable?              : boolean;
    compactacioSetmanalMesos? : number | null;
    compactacioMensualMesos?  : number | null;
    eliminacioMesos?          : number | null;


    /**
     * Constructor
     */
    constructor(app: IEntornAppModel) {
        super(app);
        this.app = app.app;
        this.entorn = app.entorn;
        Object.assign(this, app);
    }
}

export interface IAppModel extends IBaseEntity {
    id          : number;
    codi        : string;
    nom         : string;
    descripcio? : string;
    activa?     : boolean;
    logo?       : string;
    entornApps? : EntornAppModel[];
}

export class AppModel extends BaseEntity implements Partial<IAppModel> {

    id          : number;
    codi        : string;
    nom         : string;
    descripcio? : string;
    activa?     : boolean;
    logo?       : string;
    entornApps? : EntornAppModel[];

    /**
     * Constructor
     */
    constructor(app: IAppModel) {
        super(app);
        this.id = app.id;
        this.codi = app.codi;
        this.nom = app.nom;
        Object.assign(this, app);
    }
}