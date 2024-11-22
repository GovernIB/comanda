import React from 'react';
import Form from './Form';
import {
    FilterApi,
    FilterApiRef,
    FilterContext,
    useFilterContext
} from './FilterContext';
import { FormApiRef, FormApi } from './FormContext';

export type FilterProps = React.PropsWithChildren & {
    resourceName: string;
    code: string;
    buttonControlled?: boolean,
    apiRef?: FilterApiRef;
    formApiRef?: FormApiRef;
    springFilterBuilder: (data: any) => string | undefined;
    additionalData?: any;
    initOnChangeRequest?: boolean;
    commonFieldComponentProps?: any;
    onDataChange?: (data: any) => void;
    onSpringFilterChange?: (springFilter: string | undefined) => void;
    debug?: boolean;
};

export const useFilterApiRef: () => React.MutableRefObject<FilterApi> = () => {
    const filterApiRef = React.useRef<FilterApi | any>({});
    return filterApiRef;
};

export const useFilterApiContext: () => FilterApiRef = () => {
    const filterContext = useFilterContext();
    return filterContext.apiRef;
};

export const Filter: React.FC<FilterProps> = (props) => {
    const {
        resourceName,
        code,
        buttonControlled,
        springFilterBuilder,
        onDataChange,
        onSpringFilterChange,
        apiRef: apiRefProp,
        formApiRef: formApiRefProp,
        children,
        ...otherFormProps
    } = props;
    const [nextDataChangeAsUncontrolled, setNextDataChangeAsUncontrolled] = React.useState(false);
    const apiRef = React.useRef<FilterApi>();
    const formApiRef = React.useRef<FormApi | any>({});
    if (formApiRefProp != null) {
        formApiRefProp.current = formApiRef.current;
    }
    const filter = (data?: any) => {
        const formData = data ?? formApiRef.current?.getData();
        const springFilter = springFilterBuilder(formData);
        onSpringFilterChange?.(springFilter);
        onDataChange?.(formData);
    }
    const clear = () => {
        setNextDataChangeAsUncontrolled(true);
        formApiRef.current?.revert(true);
    }
    const handleDataChange = (data: any) => {
        if (nextDataChangeAsUncontrolled) {
            setNextDataChangeAsUncontrolled(false);
            filter(data);
        } else {
            !buttonControlled && filter(data);
        }
    }
    const fieldTypeMap = new Map<string, string>([
        ['datetime-local', 'date'],
        ['checkbox', 'checkbox-select']
    ]);
    apiRef.current = {
        clear,
        filter,
    };
    if (apiRefProp) {
        if (apiRefProp.current) {
            apiRefProp.current.clear = clear;
            apiRefProp.current.filter = filter;
        } else {
            console.warn('apiRef prop must be initialized with an empty object');
        }
    }
    const context = {
        resourceName,
        code,
        apiRef,
    };
    return <FilterContext.Provider value={context}>
        <Form
            resourceName={resourceName}
            resourceType='filter'
            resourceTypeCode={code}
            onDataChange={handleDataChange}
            fieldTypeMap={fieldTypeMap}
            apiRef={formApiRef}
            {...otherFormProps}>
            {children}
        </Form>
    </FilterContext.Provider>;
}

export default Filter;