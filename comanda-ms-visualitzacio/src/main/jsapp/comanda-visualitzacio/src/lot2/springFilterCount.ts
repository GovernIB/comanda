/**
 * Compta els operands de primer nivell d'una cadena Spring Filter: treu els parèntesis que envolten
 * tota l'expressió, ignora el contingut dels parèntesis interiors i dels literals entre cometes (el
 * text que ha escrit l'usuari) i compta els AND/OR que queden a fora.
 */
export const countSpringFilterTerms = (filter?: string): number => {
    if (!filter?.trim()) {
        return 0;
    }
    let str = filter.trim();
    if (str.startsWith('(') && str.endsWith(')')) {
        let balance = 0;
        let wrapsAll = true;
        for (let i = 0; i < str.length; i++) {
            balance += str[i] === '(' ? 1 : str[i] === ')' ? -1 : 0;
            if (balance === 0 && i < str.length - 1) {
                wrapsAll = false;
                break;
            }
        }
        if (wrapsAll) {
            str = str.slice(1, -1);
        }
    }
    let depth = 0;
    let quoted = false;
    let flat = '';
    for (const c of str) {
        if (c === "'") {
            quoted = !quoted;
        } else if (quoted) {
            continue;
        } else if (c === '(') {
            depth++;
        } else if (c === ')') {
            depth--;
        } else if (depth === 0) {
            flat += c;
        }
    }
    const matches = flat.match(/\s(AND|OR)\s/gi);
    return matches ? matches.length + 1 : 1;
};
