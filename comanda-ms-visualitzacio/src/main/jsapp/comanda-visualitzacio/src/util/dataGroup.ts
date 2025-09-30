import dayjs, {OpUnitType} from 'dayjs';

export const generateDataGroups = (dataInici: string, dataFi: string, agrupacio: string) => {
    const dataGroups: string[] = [];
    let truncateUnit: OpUnitType = 'minute';
    switch (agrupacio) {
        case 'ANY':
            truncateUnit = 'year';
            break;
        case 'MES':
            truncateUnit = 'month';
            break;
        case 'DIA':
        case 'DIA_SETMANA':
        case 'DIA_MES':
            truncateUnit = 'day';
            break;
        case 'HORA':
            truncateUnit = 'hour';
            break;
        }
    let djs = dayjs(dataInici).startOf(truncateUnit);

    let dataFiJs = dayjs(dataFi).startOf(truncateUnit);
    if (agrupacio == 'MINUTS_HORA') {
        dataFiJs = dataFiJs.add(1, truncateUnit);
    } else if (agrupacio == 'HORA') {
        djs = djs.add(1, truncateUnit);
    }

    do {
        dataGroups.push(djs.format('YYYY-MM-DDTHH:mm:ss'));
        if (agrupacio === 'ANY') {
            djs = djs.add(1, 'year');
        } else if (agrupacio === 'MES') {
            djs = djs.add(1, 'month');
        } else if (agrupacio === 'DIA' || agrupacio === 'DIA_SETMANA' || agrupacio === 'DIA_MES') {
            djs = djs.add(1, 'day');
        } else if (agrupacio === 'HORA') {
            djs = djs.add(1, 'hour');
        } else if (agrupacio === 'MINUTS_HORA') {
            djs = djs.add(4, 'minute');
        } else {    // MINUT (Valor per defecte
            djs = djs.add(1, 'minute');
        }
    } while (!djs.isAfter(dataFiJs));
    return dataGroups;
}

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
