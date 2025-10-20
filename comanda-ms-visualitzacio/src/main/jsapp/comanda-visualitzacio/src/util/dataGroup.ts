export const isDataInGroup = (data: string, group: string, agrupacio: string) => {
    if (agrupacio === 'ANY') {
        return data.substring(0, 4) === group.substring(0, 4);
    } else if (agrupacio === 'MES') {
        return data.substring(0, 7) === group.substring(0, 7);
    } else if (agrupacio === 'DIA' || agrupacio === 'DIA_SETMANA' || agrupacio === 'DIA_MES') {
        return data.substring(0, 10) === group.substring(0, 10);
    } else if (agrupacio === 'HORA') {
        return data.substring(0, 13) === group.substring(0, 13);
    } else if (agrupacio === 'MINUTS_HORA') {
        return data.substring(0, 16) === group.substring(0, 16);
    } else if (agrupacio === 'MINUT') {
        return data.substring(0, 16) === group.substring(0, 16);
    }
}

export const toXAxisDataGroups = (dataGroups: string[], agrupacio: string) => {
    return dataGroups?.map(g => {
        if (agrupacio === 'ANY') {
            return g.substring(0, 4);
        } else if (agrupacio === 'MES') {
            return g.substring(0, 7);
        } else if (agrupacio === 'DIA' || agrupacio === 'DIA_SETMANA' || agrupacio === 'DIA_MES') {
            return g.substring(0, 10);
        } else if (agrupacio === 'HORA') {
            return g.substring(8, 13) + ':00';
        } else if (agrupacio === 'MINUTS_HORA') {
            return g.substring(11, 16);
        } else if (agrupacio === 'MINUT') {
            return g.substring(11, 16);
        }
    });
}
