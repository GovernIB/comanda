import { countSpringFilterTerms } from '../lot2';

/**
 * Combina diversos Spring Filters (o fragments) en una cadena que `FilterCount` compta com la SUMA
 * dels termes de cadascun. No s'envia al servidor: només serveix per al recompte de filtres aplicats
 * quan una pantalla té més d'un filtre. Torna `undefined` si no hi ha cap terme.
 */
export const sumSpringFilterTerms = (...filters: Array<string | undefined>): string | undefined => {
    const total = filters.reduce((sum, f) => sum + countSpringFilterTerms(f), 0);
    return total === 0 ? undefined : Array.from({ length: total }, (_, i) => `t${i}:1`).join(' AND ');
};
