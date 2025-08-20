export enum ChipColor {
    WHITE = "white",
    INFO = "info"
}

export const createTransparentColor = (color: string, opacity: number = 0.8): string => {
    // If the color is in hex format (#RRGGBB), convert it to rgba
    if (color.startsWith('#')) {
        const r = parseInt(color.slice(1, 3), 16);
        const g = parseInt(color.slice(3, 5), 16);
        const b = parseInt(color.slice(5, 7), 16);
        return `rgba(${r}, ${g}, ${b}, ${opacity})`;
    }
    // If the color is already in rgba format, just adjust the opacity
    if (color.startsWith('rgba')) {
        return color.replace(/[\d.]+\)$/, `${opacity})`);
    }
    // If the color is in rgb format, convert it to rgba
    if (color.startsWith('rgb')) {
        return color.replace('rgb', 'rgba').replace(')', `, ${opacity})`);
    }
    // For named colors or other formats, return as is
    return color;
};

export const isWhiteColor = (color: string): boolean => {
    return color === '#FFFFFF' || color === '#fff' || color === 'white';
};