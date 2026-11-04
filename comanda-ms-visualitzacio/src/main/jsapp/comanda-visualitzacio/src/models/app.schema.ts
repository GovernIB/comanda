import * as z from 'zod';

export const AppGrupRefSchema = z.object({
    id: z.number().optional(),
    description: z.string().optional(),
});

export const AppSchema = z.object({
    id: z.number(),
    codi: z.string(),
    nom: z.string(),
    descripcio: z.string().nullish(),
    activa: z.boolean(),
    logo: z.string().nullish(),
    ordre: z.number().nullish(),
    grup: AppGrupRefSchema.nullish(),
    grupColorClar: z.string().nullish(),
    grupColorFosc: z.string().nullish(),
});

export type AppType = z.infer<typeof AppSchema>;

export const AppGrupSchema = z.object({
    id: z.number(),
    nom: z.string(),
    colorClar: z.string().nullish(),
    colorFosc: z.string().nullish(),
    ordre: z.number().nullish(),
});

export type AppGrupType = z.infer<typeof AppGrupSchema>;
