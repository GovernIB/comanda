export interface IBaseEntity
{
    id: number;
}


// -----------------------------------------------------------------------------------------------------
// @ BaseEntity
// -----------------------------------------------------------------------------------------------------
export class BaseEntity implements Required<IBaseEntity>
{

    // Claves estáticas para poder usarlas como keyof
    static readonly ID: keyof BaseEntity = "id";

    id: number;

    /**
     * Constructor
     */
    constructor(baseEntity: IBaseEntity)
    {
        this.id = baseEntity.id;
        // Object.assign(this, baseEntity);
    }
}
