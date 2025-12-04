/// <reference types="vite/client" />
/// <reference types="vite-plugin-svgr/client" />

interface ImportMetaEnv {
    readonly VITE_API_URL: string;
    readonly VITE_API_PUBLIC_URL: string;
    readonly VITE_API_BASE_URL: string;
    readonly VITE_API_SUFFIX: string;
    readonly VITE_AUTH_PROVIDER_URL: string;
    readonly VITE_AUTH_PROVIDER_REALM: string;
    readonly VITE_AUTH_PROVIDER_CLIENTID: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
