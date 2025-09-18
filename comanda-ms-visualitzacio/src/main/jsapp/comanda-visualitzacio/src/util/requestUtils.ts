// TODO Añadir este export a lib
import {
    ResourceApiFindArgs,
    ResourceApiFindResponse,
} from '../../lib/components/ResourceApiProvider.tsx';

export const findOptions = async (find: (args: ResourceApiFindArgs) => Promise<ResourceApiFindResponse>, descriptionKey: string, quickFilter: string, filter?: string) => {
    const response = await find({ page: 0, size: 5, filter, quickFilter });
    return {
        options: response.rows.map(row => ({
            id: row.id,
            description: row[descriptionKey],
        })),
        page: response.page,
    }
};
