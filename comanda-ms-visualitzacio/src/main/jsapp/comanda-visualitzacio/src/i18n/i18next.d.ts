import type { caResourceType } from './i18n';

declare module 'i18next' {
    interface CustomTypeOptions {
        enableSelector: "optimize";
        resources: caResourceType;
    }
}
