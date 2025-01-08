import React from 'react';
import { parseTemplate } from 'url-template';
import {
    Client,
    Resource,
    State,
    Action,
    Links,
    Link,
    Problem
} from 'ketting';
import useLogConsole, { LogConsoleType } from '../util/useLogConsole';
import useControlledUncontrolledState from '../util/useControlledUncontrolledState';
import { useOptionalAuthContext } from './AuthContext';
import ResourceApiContext, {
    useResourceApiContext,
    OpenAnswerRequiredDialogFn,
    ResourceApiUserSessionValuePair,
} from './ResourceApiContext';

const LOG_PREFIX = 'RAPI';
const OFFLINE_CHECK_TIMEOUT = 5000; // en milisegons

type ResourceApiMethods = {
    find: (args: ResourceApiFindArgs) => Promise<ResourceApiFindResponse>;
    getOne: (id: any, args?: ResourceApiGetOneArgs) => Promise<any>;
    create: (args: ResourceApiRequestArgs) => Promise<any>;
    update: (id: any, args: ResourceApiRequestArgs) => Promise<any>;
    patch: (id: any, args: ResourceApiRequestArgs) => Promise<any>;
    delette: (id: any, args?: ResourceApiRequestArgs) => Promise<void>;
    onChange: (id: any, args: ResourceApiOnChangeArgs) => Promise<void>;
    artifacts: (args: ResourceApiRequestArgs) => Promise<State>;
    action: (args: ResourceApiActionArgs) => Promise<any>;
    report: (args: ResourceApiReportArgs) => Promise<any[]>;
}

export type ResourceApiService = {
    isLoading: boolean;
    isReady: boolean;
    currentState?: State;
    currentFields?: any[];
    currentError?: Error;
    currentRefresh: (args?: ResourceApiRequestArgs) => void;
    request: ResourceApiGenericRequest;
    getLink: (link?: string, state?: State) => Promise<Link | undefined>;
    currentLinks?: any;
    currentActions?: any;
} & ResourceApiMethods;

export type ResourceApiCallbacks = {
    state?: (state: State) => void;
    links?: (linksMap: any) => void;
    error?: (error: any) => void;
};

export type ResourceApiGenericRequest = (
    link: string,
    id?: any,
    args?: ResourceApiRequestArgs,
    state?: State,
    linkIsHref?: boolean) => Promise<State>;

export type ResourceApiActionSubmitArgs = {
    data?: any;
    headers?: any;
    urlData?: any;
};

export type ResourceApiRequestArgs = ResourceApiActionSubmitArgs & {
    callbacks?: ResourceApiCallbacks;
    refresh?: boolean;
    getOneData?: any;
};

export type ResourceApiFindCommonArgs = ResourceApiRequestArgs & {
    page?: number | undefined;
    size?: number | undefined;
    unpaged?: boolean;
    sorts?: string[]; // field[,asc/desc]
    quickFilter?: string;
    filter?: string;
    namedQueries?: string[];
    perspectives?: string[];
};

export type ResourceApiFindArgs = ResourceApiFindCommonArgs & {
    includeLinksInRows?: boolean;
};

export type ResourceApiFindResponse = {
    rows: any[];
    page: any;
};

export type ResourceApiBlobResponse = {
    blob: Blob;
    fileName: string;
};

export type ResourceApiGetOneArgs = ResourceApiRequestArgs & {
    perspectives?: string[];
    includeLinks?: boolean;
};

export type ResourceApiOnChangeArgs = ResourceApiRequestArgs & {
    fieldName?: string;
    fieldValue?: any;
    previous?: any;
    action?: string;
    report?: string;
    filter?: string;
};

export type ResourceApiActionArgs = ResourceApiRequestArgs & {
    code: string;
};

export type ResourceApiReportArgs = ResourceApiRequestArgs & {
    code: string;
    //outputFormat?: 'PDF' | 'XLS' | 'CSV' | 'ODS' | 'XLSX' | 'ODT' | 'RTF' | 'DOCX' | 'PPTX';
};

export type ResourceApiProviderProps = React.PropsWithChildren & {
    apiUrl: string;
    defaultLanguage?: string;
    currentLanguage?: string;
    onCurrentLanguageChange?: (currentLanguage?: string) => void;
    userSessionActive?: boolean;
    defaultUserSession?: any;
    offlineAutoCheck?: boolean;
    debug?: boolean;
    debugRequests?: boolean;
    debugAvailableServices?: boolean;
};

export type ResourceApiError = Problem & {
    errors?: any[];
    validationErrors?: any[];
    modificationCanceledError?: boolean;
};

export const processStateLinks = (links?: Links) => {
    return links?.getAll().reduce((acc: any, curr: Link) => (acc[curr.rel] = curr, acc), {});
}
export const processStateActions = (actions?: Action[]) => {
    return actions?.reduce((acc: any, curr: Action) => (acc[curr.name ?? ''] = curr, acc), {});
}

// Clone of Ketting's Action.submit() function (https://github.com/badgateway/ketting/blob/version-7.x/src/action.ts#L109)
// with a new parameter to allow custom HTTP request headers.
const kettingActionSubmit = async (action: Action, args?: ResourceApiActionSubmitArgs | undefined) => {
    const { data, headers, urlData } = args ?? {};
    const uri = new URL(action.uri);
    if (action.method === 'GET') {
        uri.search = new URLSearchParams(data).toString();
        const resource = (action as any).client.go(uri.toString());
        return resource.get();
    }
    let body;
    switch (action.contentType) {
        case 'application/x-www-form-urlencoded':
            body = new URLSearchParams(data).toString();
            break;
        case 'application/json':
            body = JSON.stringify(data);
            break;
        default:
            throw new Error(`Serializing mimetype ${action.contentType} is not yet supported in actions`);
    }
    let urlSearchParams: URLSearchParams | null = null;
    if (urlData != null) {
        urlSearchParams = new URLSearchParams();
        for (const [key, value] of Object.entries(urlData)) {
            value && urlSearchParams.append(key, '' + value);
        }
    }
    const uriParams = urlSearchParams != null ? '?' + urlSearchParams.toString() : '';
    const response = await (action as any).client.fetcher.fetchOrThrow(uri.toString() + uriParams, {
        method: action.method,
        body,
        headers: {
            'Content-Type': action.contentType,
            ...headers,
        },
    });
    return (action as any).client.getStateForResponse(uri.toString(), response);
}

const getStateAction = (state: State, action: string) => {
    try {
        return state.actions().length > 0 ? state.action(action) : undefined;
    } catch (error) { }
}
const getPromiseFromResourceLink = async (resource: Resource, link?: string, refresh?: boolean): Promise<State> => {
    if (link) {
        const followedResource = await resource.follow(link);
        return refresh ? followedResource.refresh() : followedResource.get();
    } else {
        return refresh ? resource.refresh() : resource.get();
    }
}
const getPromiseFromStateLink = (state: State, link: string, args?: ResourceApiActionSubmitArgs | undefined, refresh?: boolean): Promise<State> => {
    try {
        const stateAction = getStateAction(state, link);
        if (stateAction) {
            return kettingActionSubmit(stateAction as Action, args);
        } else {
            const { data } = args ?? {};
            const stateFollow = state.follow(link, data);
            return refresh ? stateFollow.refresh() : stateFollow.get();
        }
    } catch (ex) {
        return new Promise((_resolve, reject) => {
            reject(ex);
        });
    }
}

const callRequestExecFn = (
    state: State,
    link: string,
    args: ResourceApiRequestArgs | undefined,
    resolve: (state: State) => void,
    reject: (reason?: any) => void,
    resourceName: string,
    id: any,
    debugRequests: boolean | undefined,
    logConsole: LogConsoleType) => {
    if (debugRequests) {
        const stateAction = getStateAction(state, link);
        const isUpdateAction = stateAction && ['POST', 'PUT', 'PATCH', 'DELETE'].includes(stateAction.method);
        const messagePrefix = isUpdateAction ? 'Executing action' : 'Sending request';
        const messageSuffix = id != null ? 'with id ' + id : '';
        logConsole.debug('[>] ' + messagePrefix + ' \'' + link + '\' on resource \'' + resourceName + '\' ' + messageSuffix);
        if (args) {
            logConsole.debug('\t args:', args);
        }
    }
    const refresh = args?.refresh != null ? args?.refresh : true;
    getPromiseFromStateLink(state, link, args, refresh).
        then((state: State) => {
            if (debugRequests) {
                const messageSuffix = id != null ? 'with id ' + id : '';
                logConsole.debug('[<] Response received for request \'' + link + '\' on resource \'' + resourceName + '\' ' + messageSuffix);
                if (state) {
                    logConsole.debug('\t response:', state);
                }
            }
            args?.callbacks?.state?.(state);
            args?.callbacks?.links?.(processStateLinks(state.links));
            resolve(state);
        }).
        catch((error: Problem) => {
            debugRequests && logConsole.debug('[x] Request error', error);
            args?.callbacks?.error?.(error);
            reject(error);
        });
}

const processAnswerRequiredError = (
    error: ResourceApiError,
    id: any,
    args: ResourceApiRequestArgs | undefined,
    callback: (id: any, args: any) => Promise<any>,
    openAnswerRequiredDialog?: OpenAnswerRequiredDialogFn): Promise<any> => {
    if (error.body) {
        if (error.status === 422 && error.body.answerRequiredError && openAnswerRequiredDialog) {
            const {
                answerCode,
                question,
                trueFalseAnswerRequired,
                availableAnswers
            } = error.body.answerRequiredError;
            return new Promise((resolve, reject) => {
                openAnswerRequiredDialog(
                    undefined,
                    question,
                    trueFalseAnswerRequired,
                    availableAnswers).
                    then((answer: string) => {
                        const answersHeader = args?.headers?.['Bb-Answers'];
                        const answers = answersHeader ? JSON.parse(answersHeader) : {};
                        const updatedAnswers = { ...answers, [answerCode]: trueFalseAnswerRequired || !availableAnswers ? answer === 'true' : answer };
                        const currentHeaders = args?.headers ? args.headers : {};
                        const updatedHeaders = { ...currentHeaders, ['Bb-Answers']: JSON.stringify(updatedAnswers) };
                        const updatedArgs = { ...args, headers: updatedHeaders };
                        callback(id, updatedArgs).
                            then((data: any) => resolve(data)).
                            catch((error: Error) => reject(error));
                    }).
                    catch((error: Error) => {
                        return new Promise((_resolve, reject) => reject(error));
                    });
            });
        } else {
            return new Promise((_resolve, reject) => reject(error));
        }
    } else {
        console.error('[' + LOG_PREFIX + '] Error response type not application/problem+json');
        return new Promise((_resolve, reject) => reject(error));
    }
}

const generateResourceApiMethods = (request: Function, getOpenAnswerRequiredDialog: Function): ResourceApiMethods => {
    const find = React.useCallback((args?: ResourceApiFindArgs): Promise<ResourceApiFindResponse> => {
        const pageArgs = args?.unpaged ? { page: 'UNPAGED' } : { page: args?.page, size: args?.size };
        const requestArgs = {
            ...args,
            data: {
                ...pageArgs,
                sort: args?.sorts,
                filter: args?.filter,
                quickFilter: args?.quickFilter,
                namedQuery: args?.namedQueries,
                perspective: args?.perspectives,
            },
            refresh: args?.refresh ?? true,
        };
        return new Promise((resolve, reject) => {
            request('find', null, requestArgs).
                then((state: State) => {
                    const rows = state.getEmbedded().map((e: any) => {
                        if (args?.includeLinksInRows) {
                            return {
                                ...e.data,
                                '_links': processStateLinks(e.links),
                                '_actions': processStateActions(e.actions()),
                            };
                        } else {
                            return e.data;
                        }
                    });
                    const page = state.data.page;
                    resolve({ rows, page });
                }).
                catch((error: ResourceApiError) => {
                    reject(error);
                });
        });
    }, [request]);
    const getOne = React.useCallback((id: any, args?: ResourceApiGetOneArgs): Promise<any> => {
        const argsData = args?.data;
        const requestArgs = {
            ...args,
            data: {
                id,
                perspective: args?.perspectives,
                ...argsData,
            },
            refresh: args?.refresh ?? true,
        };
        return new Promise((resolve, reject) => {
            request('getOne', null, requestArgs).
                then((state: State) => {
                    if (args?.includeLinks) {
                        resolve({
                            ...state.data,
                            '_links': processStateLinks(state.links),
                            '_actions': processStateActions(state.actions()),
                        });
                    } else {
                        resolve(state.data);
                    }
                }).
                catch((error: ResourceApiError) => {
                    reject(error);
                });
        });
    }, [request]);
    const create = React.useCallback((args?: ResourceApiRequestArgs): Promise<any> => {
        const requestArgs = {
            ...args,
            data: args?.data,
        };
        return new Promise((resolve, reject) => {
            request('create', null, requestArgs).
                then((state: State) => {
                    resolve(state.data);
                }).
                catch((error: ResourceApiError) => {
                    processAnswerRequiredError(
                        error,
                        null,
                        args,
                        create,
                        getOpenAnswerRequiredDialog()).
                        then(resolve).
                        catch(reject);
                });
        });
    }, [request]);
    const update = React.useCallback((id: any, args?: ResourceApiRequestArgs): Promise<any> => {
        const requestArgs = {
            ...args,
            data: args?.data,
        };
        return new Promise((resolve, reject) => {
            request('update', id, requestArgs).
                then((state: State) => {
                    resolve(state.data);
                }).
                catch((error: ResourceApiError) => {
                    processAnswerRequiredError(
                        error,
                        id,
                        args,
                        update,
                        getOpenAnswerRequiredDialog()).
                        then(resolve).
                        catch(reject);
                });
        });
    }, [request]);
    const patch = React.useCallback((id: any, args?: ResourceApiRequestArgs): Promise<any> => {
        const requestArgs = {
            ...args,
            data: args?.data,
        };
        return new Promise((resolve, reject) => {
            request('patch', id, requestArgs).
                then((state: State) => {
                    resolve(state.data);
                }).
                catch((error: ResourceApiError) => {
                    processAnswerRequiredError(
                        error,
                        id,
                        args,
                        patch,
                        getOpenAnswerRequiredDialog()).
                        then(resolve).
                        catch(reject);
                });
        });
    }, [request]);
    const delette = React.useCallback((id: any, args?: ResourceApiRequestArgs): Promise<void> => {
        return new Promise((resolve, reject) => {
            request('delete', id, { ...args }).
                then(() => {
                    resolve();
                }).
                catch((error: ResourceApiError) => {
                    processAnswerRequiredError(
                        error,
                        id,
                        args,
                        delette,
                        getOpenAnswerRequiredDialog()).
                        then(resolve).
                        catch(reject);
                });
        });
    }, [request]);
    const onChange = React.useCallback((id: any, args: ResourceApiOnChangeArgs): Promise<any> => {
        const onChangeData = {
            type: args.action ? 'ACTION' : args.report ? 'REPORT' : args.filter ? 'FILTER' : undefined,
            typeCode: args.action ?? args.report ?? args.filter,
            id,
            previous: args.previous,
            fieldName: args.fieldName,
            fieldValue: args.fieldValue,
        }
        const requestArgs = {
            ...args,
            data: onChangeData,
        };
        return new Promise((resolve, reject) => {
            request('onChange', null, requestArgs).
                then((state: State) => {
                    resolve(state.data);
                }).
                catch((error: ResourceApiError) => {
                    processAnswerRequiredError(
                        error,
                        id,
                        args,
                        onChange,
                        getOpenAnswerRequiredDialog()).
                        then(resolve).
                        catch(reject);
                });
        });
    }, [request]);
    const artifacts = React.useCallback((args?: ResourceApiRequestArgs): Promise<State> => {
        return new Promise((resolve, reject) => {
            request('artifacts', null, { ...args }).then(resolve).catch(reject);
        });
    }, [request]);
    const action = React.useCallback((args?: ResourceApiActionArgs): Promise<any[]> => {
        return new Promise((resolve, reject) => {
            request('artifacts').
                then((state: State) => {
                    if (args?.code != null) {
                        const actionRel = 'exec_' + args.code;
                        const actionLink = state.links.get(actionRel);
                        if (actionLink != null) {
                            request(actionRel, null, { ...args }, state).
                                then((state: State) => {
                                    const result = state.data;
                                    resolve(result);
                                }).
                                catch((error: ResourceApiError) => {
                                    processAnswerRequiredError(
                                        error,
                                        null,
                                        args,
                                        onChange,
                                        getOpenAnswerRequiredDialog()).
                                        then(resolve).
                                        catch(reject);
                                });
                        } else {
                            reject('Action ' + args.code + ' not found in artifacts');
                        }
                    } else {
                        reject('Action code not specified')
                    }
                }).
                catch(reject);
        });
    }, [request]);
    const report = React.useCallback((args?: ResourceApiReportArgs): Promise<any[]> => {
        return new Promise((resolve, reject) => {
            request('artifacts').
                then((state: State) => {
                    if (args?.code != null) {
                        const reportRel = 'generate_' + args.code;
                        const reportLink = state.links.get(reportRel);
                        if (reportLink != null) {
                            request(reportRel, null, { ...args }, state).
                                then((state: State) => {
                                    const items = state.getEmbedded().map(e => e.data);
                                    resolve(items);
                                }).
                                catch((error: ResourceApiError) => {
                                    processAnswerRequiredError(
                                        error,
                                        null,
                                        args,
                                        onChange,
                                        getOpenAnswerRequiredDialog()).
                                        then(resolve).
                                        catch(reject);
                                });
                        } else {
                            reject('Report ' + args.code + ' not found in artifacts');
                        }
                    } else {
                        reject('Report code not specified')
                    }
                }).
                catch(reject);
        });
    }, [request]);
    return {
        find,
        getOne,
        create,
        update,
        patch,
        delette,
        onChange,
        artifacts,
        action,
        report,
    };
}

export const useResourceApiService = (resourceName?: string): ResourceApiService => {
    const logConsole = useLogConsole(LOG_PREFIX);
    const {
        isReady: indexIsReady,
        indexState,
        getOpenAnswerRequiredDialog,
        isDebugRequests,
    } = useResourceApiContext('useResourceApiService');
    const [isCurrentLoading, setIsCurrentLoading] = React.useState<boolean>(true);
    const [isCurrentLoaded, setIsCurrentLoaded] = React.useState<boolean>(false);
    const [currentState, setCurrentState] = React.useState<State | undefined>();
    const [currentFields, setCurrentFields] = React.useState<any[] | undefined>();
    const [currentError, setCurrentError] = React.useState<Error | undefined>();
    const isReady = indexIsReady && !isCurrentLoading && currentState != null && !currentError;
    const debugRequests = isDebugRequests();
    const currentLinks = processStateLinks(currentState?.links);
    const currentActions = processStateActions(currentState?.actions());
    const getLink = (link?: string, id?: any): Promise<Link | undefined> => new Promise((resolve, reject) => {
        if (link != null) {
            if (id != null) {
                if (currentState != null) {
                    getPromiseFromStateLink(currentState, 'getOne', { data: { resourceId: id } }, true).
                        then((state: State) => {
                            resolve(state.links.get(link));
                        }).
                        catch(reject);
                } else {
                    reject('[' + LOG_PREFIX + '] API not initalized');
                }
            } else {
                return resolve(currentLinks != null ? currentLinks[link] : undefined);
            }
        } else {
            resolve(undefined);
        }
    });
    const currentRefresh = (args?: ResourceApiRequestArgs) => {
        indexState != null && resourceName != null && getPromiseFromStateLink(indexState, resourceName, args, true).
            then((response: State) => {
                setCurrentState(response);
                setCurrentFields(response.action().fields);
                setIsCurrentLoading(false);
                !isCurrentLoaded && setIsCurrentLoaded(true);
            }).catch((error: Error) => {
                setCurrentError(error);
                setIsCurrentLoading(false);
                !isCurrentLoaded && setIsCurrentLoaded(true);
            });
    }
    React.useEffect(() => {
        if (indexIsReady && indexState && !currentState) {
            currentRefresh();
        } else if (!indexIsReady && isCurrentLoaded) {
            setIsCurrentLoading(true);
            setIsCurrentLoaded(false)
            setCurrentState(undefined);
            setCurrentFields(undefined);
            setCurrentError(undefined);
        }
    }, [indexIsReady]);
    React.useEffect(() => {
        if (currentError) {
            logConsole.error('Couldn\'t get API service \'' + resourceName + '\'', currentError);
        }
    }, [currentError]);
    const request: ResourceApiGenericRequest = React.useCallback((
        link: string,
        id?: any,
        args?: ResourceApiRequestArgs,
        state?: State): Promise<State> => {
        return new Promise((resolve, reject) => {
            const realState = state ?? currentState;
            if (resourceName && !isCurrentLoading && realState) {
                if (id != null) {
                    getPromiseFromStateLink(realState, 'getOne', { data: { id, ...args?.getOneData } }, true).
                        then((state: State) => {
                            callRequestExecFn(
                                state,
                                link,
                                args,
                                resolve,
                                reject,
                                resourceName,
                                id,
                                debugRequests,
                                logConsole);
                        }).
                        catch((error: Error) => {
                            args?.callbacks?.error?.(error);
                            reject(error);
                        });
                } else {
                    callRequestExecFn(
                        realState,
                        link,
                        args,
                        resolve,
                        reject,
                        resourceName,
                        null,
                        debugRequests,
                        logConsole);
                }
            } else {
                const error = {
                    name: 'ApiStillLoadingError',
                    message: 'Couldn\'t exec request to link/action \'' + link + '\' on resource \'' + resourceName + '\': API is still loading',
                }
                args?.callbacks?.error?.(error);
                reject(error);
            }
        });
    }, [resourceName, debugRequests, isCurrentLoading, currentState]);
    const resourceApiMethods = generateResourceApiMethods(
        request,
        getOpenAnswerRequiredDialog);
    return {
        isLoading: isCurrentLoading,
        isReady,
        currentState,
        currentFields,
        currentError,
        currentRefresh,
        request,
        ...resourceApiMethods,
        getLink,
        currentLinks,
        currentActions,
    };
}

export const ResourceApiProvider = (props: ResourceApiProviderProps) => {
    const {
        apiUrl,
        defaultLanguage,
        currentLanguage: currentLanguageProp,
        onCurrentLanguageChange,
        userSessionActive,
        defaultUserSession,
        offlineAutoCheck,
        debug,
        debugRequests,
        debugAvailableServices,
        children
    } = props;
    const logConsole = useLogConsole(LOG_PREFIX);
    const authContext = useOptionalAuthContext();
    const isAuthReady = authContext?.isReady;
    const isAuthenticated = authContext?.isAuthenticated;
    const getToken = authContext?.getToken;
    const kettingClientRef = React.useRef<Client>();
    const openAnswerRequiredDialogRef = React.useRef<OpenAnswerRequiredDialogFn>();
    const [userSession, setUserSession] = React.useState<any | undefined>(defaultUserSession);
    const [currentLanguage, setCurrentLanguage] = useControlledUncontrolledState<string | undefined>(
        defaultLanguage,
        currentLanguageProp,
        onCurrentLanguageChange);
    const [isIndexLoading, setIsIndexLoading] = React.useState<boolean>(true);
    const [indexState, setIndexState] = React.useState<State | undefined>();
    const [indexError, setIndexError] = React.useState<Error | undefined>();
    const [offline, setOffline] = React.useState<boolean>(false);
    const indexPath = new URL(apiUrl).pathname;
    const refreshKettingClient = (userSession: any, currentLanguage: string | undefined) => {
        const kettingClient = new Client(apiUrl);
        kettingClient.use((request, next) => {
            const token = getToken?.();
            if (isAuthenticated && token) {
                request.headers.set('Authorization', 'Bearer ' + token);
            }
            if (userSession && Object.keys(userSession).length > 0) {
                request.headers.set('Bb-Session', JSON.stringify(userSession));
            }
            if (currentLanguage && currentLanguage.length) {
                request.headers.set('Accept-Language', currentLanguage);
            }
            return next(request);
        });
        kettingClientRef.current = kettingClient;
    }
    const refreshApiIndex = React.useCallback(() => {
        if (kettingClientRef.current) {
            setIsIndexLoading(true);
            setIndexError(undefined);
            if (debug) {
                logConsole.debug((!indexState ? 'Connecting' : 'Reconnecting') + ' to API URL', indexPath);
            }
            setIndexState(undefined);
            getPromiseFromResourceLink(kettingClientRef.current.go(indexPath), undefined, true).
                then((response: State) => {
                    setIndexState(response);
                    setIsIndexLoading(false);
                    setOffline(false);
                }).catch((error: Error) => {
                    setIndexError(error);
                    setIsIndexLoading(false);
                    setOffline(true);
                });
        }
    }, []);
    const requestHref = React.useCallback((href: string, templateData?: any): Promise<State> => {
        if (kettingClientRef.current) {
            const processedHref = parseTemplate(href).expand(templateData);
            return getPromiseFromResourceLink(kettingClientRef.current.go(processedHref), undefined, true);
        } else {
            throw new Error('Ketting client not initialized');
        }
    }, []);
    React.useEffect(() => {
        if (offlineAutoCheck && !isIndexLoading && (indexState || indexError)) {
            const timeoutFn = () => {
                if (kettingClientRef.current) {
                    getPromiseFromResourceLink(kettingClientRef.current.go(indexPath + '/ping'), undefined, true).then(() => {
                        setOffline(false);
                        !indexState && refreshApiIndex();
                    }).catch(() => {
                        setOffline(true);
                    });
                }
            }
            const intervalId = setInterval(timeoutFn, OFFLINE_CHECK_TIMEOUT);
            return () => intervalId ? clearInterval(intervalId) : undefined;
        }
    }, [isIndexLoading, indexState, indexError, offlineAutoCheck]);
    React.useEffect(() => {
        const sessionInitialized = userSessionActive ? userSession != null : true;
        if (sessionInitialized && (authContext == null || isAuthReady)) {
            refreshKettingClient(userSession, currentLanguage);
            refreshApiIndex();
        }
    }, [isAuthReady, currentLanguage, userSession]);
    React.useEffect(() => {
        if (indexState && debug) {
            debugAvailableServices && logConsole.debug('Resource API services from index:');
            indexState.links.getAll().forEach((l: Link) => {
                debugAvailableServices && logConsole.debug('\tService ' + l.rel + ':' + l.href);
            });
        }
    }, [indexState]);
    const getKettingClient = React.useCallback(() => {
        return kettingClientRef.current;
    }, []);
    const isDebugRequests = React.useCallback(() => {
        return (debug == null && debugRequests != null && debugRequests) || debug;
    }, []);
    const setUserSessionAttributes = (attributeValuePairs: ResourceApiUserSessionValuePair[]): boolean => {
        const changedPairs = attributeValuePairs?.filter(p => p.value !== userSession?.[p.attribute]);
        if (changedPairs?.length) {
            const changes: any = {};
            changedPairs.forEach(c => changes[c.attribute] = c.value);
            setUserSession((s: any) => ({ ...s, ...changes }));
            refreshKettingClient({ ...userSession, ...changes }, currentLanguage);
            return true;
        } else {
            return false;
        }
    }
    const clearUserSession = React.useCallback(() => {
        setUserSession({});
        refreshApiIndex();
    }, []);
    const setOpenAnswerRequiredDialog = React.useCallback((oarDialog: OpenAnswerRequiredDialogFn) => {
        openAnswerRequiredDialogRef.current = oarDialog;
    }, []);
    const getOpenAnswerRequiredDialog = React.useCallback(() => {
        return openAnswerRequiredDialogRef.current;
    }, []);
    const setCurrentLanguageInternal = (currentLanguage?: string) => {
        refreshKettingClient(userSession, currentLanguage);
        setCurrentLanguage(currentLanguage);
    }
    const isReady = !isIndexLoading && !indexError && !offline;
    const context = {
        isLoading: isIndexLoading,
        isReady,
        apiUrl,
        offline,
        indexState,
        indexError,
        userSession,
        currentLanguage,
        refreshApiIndex,
        getKettingClient,
        requestHref,
        isDebugRequests,
        setUserSession,
        setUserSessionAttributes,
        clearUserSession,
        setCurrentLanguage: setCurrentLanguageInternal,
        setOpenAnswerRequiredDialog,
        getOpenAnswerRequiredDialog,
    };
    return <ResourceApiContext.Provider value={context}>
        {children}
    </ResourceApiContext.Provider>;
}
