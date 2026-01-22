import { FallbackNs, useTranslation, UseTranslationOptions } from 'react-i18next';
import type { FlatNamespace, i18n, KeyPrefix } from 'i18next';
import { useCallback } from 'react';

type UseTranslationResponse = {
    t: (key: string) => string;
    i18n: i18n;
    ready: boolean;
};

type $Tuple<T> = readonly [T?, ...T[]];
interface UseTranslationLegacy {
    <
        const Ns extends FlatNamespace | $Tuple<FlatNamespace> | undefined = undefined,
        const KPrefix extends KeyPrefix<FallbackNs<Ns>> = undefined,
    >(
        ns?: Ns,
        options?: UseTranslationOptions<KPrefix>
    ): UseTranslationResponse;
}

const useTranslationStringKey: UseTranslationLegacy = (...params) => {
    const { t, ...rest } = useTranslation(...params);
    // @ts-expect-error useTranslation TFunction still accepts a string key as a valid parameter.
    // i18n/i18n.test.ts is used to make sure this functionality isn't broken on a future release.
    const modifiedT = useCallback((key: string) => t(key), [t]);
    return {
        t: modifiedT,
        ...rest,
    };
};

export default useTranslationStringKey;
