// TODO Añadir este export a lib
import {
    ResourceApiFindArgs,
    ResourceApiFindResponse,
} from '../../lib/components/ResourceApiProvider.tsx';
import { getEnvApiUrl } from '../main.tsx';

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

/**
 * Builds a URL based on the current environment's API URL and the provided URL fragment.
 * @param urlFragment - The URL fragment to append to the API URL. Do not include leading slashes (e.g., 'users/findAll').
 */
export const buildHref = (urlFragment: string) => {
    const baseUrl = getEnvApiUrl() as string;
    return `${baseUrl}${baseUrl.endsWith('/') ? '' : '/'}${urlFragment}`;
};
