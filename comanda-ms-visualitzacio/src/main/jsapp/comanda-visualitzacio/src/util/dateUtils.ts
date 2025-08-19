import dayjs from 'dayjs';

export const formatDate = (date: string, format: string = 'DD/MM/YYYY HH:mm:ss'): string | null => {
    return date ? dayjs(date).format(format) : null;
}

export const formatIso = (date: string) =>{
    return formatDate(date, "YYYY-MM-DDTHH:mm:ss")
}

export const formatStartOfDay = (date: string) =>{
    return formatDate(date, "YYYY-MM-DDT00:00:00")
}

export const formatEndOfDay = (date: string) =>{
    return formatDate(date, "YYYY-MM-DDT23:59:59")
}