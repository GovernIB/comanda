import { describe, expect, it } from 'vitest';
import { getErrorMessage } from './exceptionUtils';

describe('getErrorMessage', () => {
    it('should return the message of an Error instance', () => {
        const error = new Error('Something went wrong');
        const result = getErrorMessage(error);
        expect(result).toBe('Something went wrong');
    });

    it('should return the message property of an object with a message field', () => {
        const error = { message: 'Unexpected error occurred' };
        const result = getErrorMessage(error);
        expect(result).toBe('Unexpected error occurred');
    });

    it('should return a stringified version of a non-object error', () => {
        const error = 'String error';
        const result = getErrorMessage(error);
        expect(result).toBe('String error');
    });

    it('should return a stringified version of null', () => {
        const error = null;
        const result = getErrorMessage(error);
        expect(result).toBe('null');
    });

    it('should return a stringified version of undefined', () => {
        const error = undefined;
        const result = getErrorMessage(error);
        expect(result).toBe('undefined');
    });

    it('should return a stringified version of an object without a message property', () => {
        const error = { code: 500 };
        const result = getErrorMessage(error);
        expect(result).toBe('[object Object]');
    });
});
