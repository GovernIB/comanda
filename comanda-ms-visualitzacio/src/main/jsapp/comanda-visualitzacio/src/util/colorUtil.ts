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

const HEX_COLOR_REGEX = /^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6})$/;
const RGB_COLOR_REGEX = /^rgba?\(([^)]+)\)$/;

const normalizeHexColor = (hex: string): string => {
    if (hex.length === 4) {
        const [, r, g, b] = hex;
        return `#${r}${r}${g}${g}${b}${b}`;
    }
    return hex;
};

const parseColorToRgb = (color: string): [number, number, number] | null => {
    if (!color) {
        return null;
    }

    const normalizedColor = color.trim();

    if (HEX_COLOR_REGEX.test(normalizedColor)) {
        const hex = normalizeHexColor(normalizedColor);
        const r = parseInt(hex.slice(1, 3), 16);
        const g = parseInt(hex.slice(3, 5), 16);
        const b = parseInt(hex.slice(5, 7), 16);
        return [r, g, b];
    }

    const rgbMatch = normalizedColor.match(RGB_COLOR_REGEX);
    if (rgbMatch?.[1]) {
        const [rRaw, gRaw, bRaw] = rgbMatch[1].split(',').map((value) => value.trim());
        const r = Number.parseFloat(rRaw);
        const g = Number.parseFloat(gRaw);
        const b = Number.parseFloat(bRaw);

        if ([r, g, b].every((value) => Number.isFinite(value))) {
            return [r, g, b];
        }
    }

    return null;
};

const srgbToLinear = (value: number): number => {
    const srgb = value / 255;
    return srgb <= 0.04045 ? srgb / 12.92 : Math.pow((srgb + 0.055) / 1.055, 2.4);
};

export const getRelativeLuminance = (color: string): number | null => {
    const rgb = parseColorToRgb(color);
    if (!rgb) {
        return null;
    }

    const [r, g, b] = rgb.map((channel) => Math.max(0, Math.min(255, channel)));
    const [rl, gl, bl] = [srgbToLinear(r), srgbToLinear(g), srgbToLinear(b)];

    return 0.2126 * rl + 0.7152 * gl + 0.0722 * bl;
};

export const isLightColor = (color: string, threshold: number = 0.5): boolean => {
    const luminance = getRelativeLuminance(color);
    if (luminance === null) {
        return isWhiteColor(color);
    }

    return luminance >= threshold;
};