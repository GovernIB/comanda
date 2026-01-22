export interface IBaseEntity
{
    id: number;
    links: any[] | null;
}


// -----------------------------------------------------------------------------------------------------
// @ BaseEntity
// -----------------------------------------------------------------------------------------------------
export class BaseEntity implements Required<IBaseEntity>
{

    // Claves estáticas para poder usarlas como keyof
    static readonly LINKS: keyof BaseEntity = "links";

    id: number;
    links: any[] | null;

    /**
     * Constructor
     */
    constructor(baseEntity: IBaseEntity)
    {
        this.id = baseEntity.id;
        this.links = baseEntity?.links;
        // Object.assign(this, baseEntity);
    }
}
