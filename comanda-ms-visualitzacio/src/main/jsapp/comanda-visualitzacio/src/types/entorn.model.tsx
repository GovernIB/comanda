import {BaseEntity, IBaseEntity} from "./base-entity.model.ts";

export interface IEntornModel extends IBaseEntity {
    codi: string;
    nom?: string;
}

export class EntornModel extends BaseEntity implements Partial<IEntornModel>{
    codi: string;
    nom?: string;

    /**
     * Constructor
     */
    constructor(entorn: IEntornModel) {
        super(entorn);
        this.codi = entorn.codi;
        Object.assign(this, entorn);
    }
}