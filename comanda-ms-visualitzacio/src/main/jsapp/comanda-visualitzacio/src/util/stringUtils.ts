/**
 * Attempts to merge two arrays of log lines by detecting a strict sequential overlap.
 *
 * @param baseline - The original array of strings.
 * @param incoming - The new array of strings to stitch into the baseline.
 * @returns The merged log lines if a strict overlap is found; otherwise `incoming`.
 */
export function mergeSequentialStringArrays(baseline: string[], incoming: string[]): string[] {
    if (incoming.length === 0) return [...baseline];
    if (baseline.length === 0) return [...incoming];

    for (let i = 0; i < baseline.length; i++) {
        if (baseline[i] !== incoming[0]) continue;

        let b = i;
        let n = 0;

        while (b < baseline.length && n < incoming.length) {
            if (baseline[b] !== incoming[n]) {
                // This potential overlap failed — continue searching for next match
                b = -1;
                break;
            }
            b++;
            n++;
        }

        if (b !== -1) {
            if (b === baseline.length) {
                // Extension match: this adds information or is a perfect suffix.
                // We return this immediately because it's the most complete result.
                return [...baseline.slice(0, i), ...incoming];
            }
        }
    }

    // No extension match found.
    // If we found a contained match (entire incoming matched within baseline),
    // the user now expects to return 'incoming'.
    // If no match found at all, also return 'incoming'.
    return [...incoming];
}
