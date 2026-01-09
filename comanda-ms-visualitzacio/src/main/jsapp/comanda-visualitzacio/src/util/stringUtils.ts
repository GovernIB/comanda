/**
 * Attempts to merge two arrays of log lines by detecting a strict sequential overlap.
 *
 * If any mismatch occurs before one of the arrays runs out, the overlap is
 * considered invalid and the function returns the `incoming` array as-is.
 *
 * This strict behavior ensures log order integrity: if logs cannot be stitched
 * perfectly, they are not merged.
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
                // Overlap attempt failed — logs are incompatible
                return [...incoming];
            }
            b++;
            n++;
        }

        // Strict overlap succeeded — merge without duplication
        return [...baseline.slice(0, i), ...incoming];
    }

    // No overlap found at all
    return [...incoming];
}
