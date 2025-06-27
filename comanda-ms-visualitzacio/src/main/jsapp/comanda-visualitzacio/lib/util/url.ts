export const toAbsolutePath = (relativePath: string, envBaseUrl: string = '/') => {
    return window.location.origin + envBaseUrl + relativePath;
}

export const isCurrentPathMatching = (path: string, withParams: boolean | undefined = false) => {
    if (withParams) {
        const currentPath = window.location.origin + window.location.pathname + window.location.search;
        return path === currentPath;
    } else {
        const currentPath = window.location.origin + window.location.pathname;
        return path === currentPath;
    }
}