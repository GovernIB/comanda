import * as z from 'zod';

export const AppSchema = z.object({
    id: z.number(),
    codi: z.string(),
    nom: z.string(),
    descripcio: z.string().nullish(),
    activa: z.boolean(),
    logo: z.string().nullish(),
    ordre: z.number().nullish(),
});

export type AppType = z.infer<typeof AppSchema>;
