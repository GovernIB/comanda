import '@testing-library/jest-dom';
import { beforeEach } from 'vitest';

// Node.js introduced Web Storage experimentally in v22.4 (behind `--experimental-webstorage`) and enabled
// `globalThis.localStorage` / `globalThis.sessionStorage` by default starting in Node.js v25.
// Without `--localstorage-file`, this native global shadows JSDOM's implementation and lacks functional storage methods (e.g. `clear()`).
// These mocks provide a robust in-memory Storage implementation on `window` and `globalThis` to ensure test compatibility across all Node versions.
class MemoryStorage implements Storage {
    private store = new Map<string, string>();

    getItem(key: string): string | null {
        return this.store.get(String(key)) ?? null;
    }

    setItem(key: string, value: string): void {
        this.store.set(String(key), String(value));
    }

    removeItem(key: string): void {
        this.store.delete(String(key));
    }

    clear(): void {
        this.store.clear();
    }

    key(index: number): string | null {
        return Array.from(this.store.keys())[index] ?? null;
    }

    get length(): number {
        return this.store.size;
    }

    has(key: string): boolean {
        return this.store.has(key);
    }

    keys(): string[] {
        return Array.from(this.store.keys());
    }
}

// Com el Web Storage real, `Object.keys(sessionStorage)` retorna les claus desades.
const createStorage = (): Storage => {
    const storage = new MemoryStorage();
    return new Proxy(storage, {
        get: (target, prop, receiver) => {
            const value = Reflect.get(target, prop, receiver);
            return typeof value === 'function' ? value.bind(target) : value;
        },
        ownKeys: (target) => target.keys(),
        getOwnPropertyDescriptor: (target, prop) =>
            typeof prop === 'string' && target.has(prop)
                ? { value: target.getItem(prop), enumerable: true, configurable: true, writable: true }
                : undefined,
    });
};

const localStorageInstance = createStorage();
const sessionStorageInstance = createStorage();

const setupStorage = () => {
    Object.defineProperty(globalThis, 'localStorage', {
        value: localStorageInstance,
        writable: true,
        configurable: true,
    });
    Object.defineProperty(globalThis, 'sessionStorage', {
        value: sessionStorageInstance,
        writable: true,
        configurable: true,
    });
    if (typeof window !== 'undefined') {
        Object.defineProperty(window, 'localStorage', {
            value: localStorageInstance,
            writable: true,
            configurable: true,
        });
        Object.defineProperty(window, 'sessionStorage', {
            value: sessionStorageInstance,
            writable: true,
            configurable: true,
        });
    }
};

setupStorage();
beforeEach(() => {
    setupStorage();
});
