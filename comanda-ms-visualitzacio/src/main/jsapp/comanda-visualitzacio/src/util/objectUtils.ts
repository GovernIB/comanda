export const numericObjectKeys = <T extends { [key: number]: unknown }>(object: T): number[] =>
    Object.keys(object).map(Number);

export const filterNumericObjectKeys = <T extends { [key: number]: unknown }>(
    object: T,
    filterFunc: (key: string) => boolean
) => {
    return Object.keys(object)
        .filter(filterFunc)
        .reduce((acc, key) => {
            acc[Number(key)] = object[Number(key)];
            return acc;
        }, {} as T);
};


