/**
 * Extracts and returns the error message from an unknown error object.
 *
 * @param {unknown} error - The error object to extract the message from. It can be an instance of Error or any object containing a `message` property.
 * @return {string} The error message as a string. If the error object does not have a recognizable message, it returns a stringified version of the error.
 */
export function getErrorMessage(error: unknown): string {
    if (error instanceof Error)
        return error.message;
    if (
        typeof error === 'object' &&
        error != null &&
        'message' in error
    ) {
        return String(error.message);
    }
    return String(error);
}
