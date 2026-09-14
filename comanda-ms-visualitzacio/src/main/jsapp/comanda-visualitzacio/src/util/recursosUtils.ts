export const parseStorageSizeToMB = (valor: string | null | undefined): number => {
    if (!valor) return 0;
    let text = valor.trim().toUpperCase();
    const lastCommaIndex = text.lastIndexOf(',');
    const lastDotIndex = text.lastIndexOf('.');
    if (lastCommaIndex > lastDotIndex) {
        text = text.replace(/\./g, "").replace(",", ".");
    } else {
        text = text.replace(/,/g, "");
    }
    const amount = parseFloat(text);
    if (!Number.isFinite(amount)) {
        return 0;
    }
    const unit = text.replace(/^[\d.\s]+/, "");
    switch (unit) {
        case "TB":
        case "TIB":
            return amount * 1024 * 1024;
        case "GB":
        case "GIB":
            return amount * 1024;
        case "MB":
        case "MIB":
        case "":
            return amount;
        case "KB":
        case "KIB":
            return amount / 1024;
        case "B":
            return amount / (1024 * 1024);
        default:
            return 0;
    }
};

export const calcularPercentatgeUs = (totalStr: string | undefined, disponibleStr: string | undefined): number => {
    const total = parseStorageSizeToMB(totalStr);
    const disponible = parseStorageSizeToMB(disponibleStr);
    if (total === 0) return 0;
    const percentatge = 100 - (disponible * 100 / total);
    return Math.min(100, Math.max(0, Math.round(percentatge)));
};

export const getColorForPercentage = (percentatge: number): 'success' | 'warning' | 'error' => {
    if (percentatge < 70) {
        return 'success';
    } else if (percentatge < 90) {
        return 'warning';
    } else {
        return 'error';
    }
};