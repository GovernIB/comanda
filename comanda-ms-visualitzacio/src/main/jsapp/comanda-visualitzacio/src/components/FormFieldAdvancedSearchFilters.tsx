import React from 'react';
import { MuiFilter, useFormContext } from 'reactlib';
import FormFieldCustomAdvancedSearch, {
    FormFieldCustomAdvancedSearchProps,
} from './FormFieldCustomAdvancedSearch';

const useAdvancedSearchFilterAdditionalRow = ({
    advancedSearchFilterCode,
    advancedSearchFilterResourceName,
    advancedSearchFilterContent,
    advancedSearchFilterBuilder,
}: AdvancedSearchFilterProps) => {
    const { resourceName } = useFormContext();
    return (
        <MuiFilter
            resourceName={advancedSearchFilterResourceName ?? resourceName}
            code={advancedSearchFilterCode}
            springFilterBuilder={advancedSearchFilterBuilder}
        >
            {advancedSearchFilterContent}
        </MuiFilter>
    );
};

type AdvancedSearchFilterProps = {
    advancedSearchFilterCode: string;
    advancedSearchFilterResourceName?: string;
    advancedSearchFilterContent: React.ReactNode;
    advancedSearchFilterBuilder: (data: any) => string | undefined;
};

type FormFieldAdvancedSearchFiltersProps = FormFieldCustomAdvancedSearchProps &
    AdvancedSearchFilterProps;

const FormFieldAdvancedSearchFilters: React.FC<FormFieldAdvancedSearchFiltersProps> = props => {
    const {
        advancedSearchFilterCode,
        advancedSearchFilterResourceName,
        advancedSearchFilterContent,
        advancedSearchFilterBuilder,
        advancedSearchDataGridProps,
    } = props;
    const toolbarAdditionalRow = useAdvancedSearchFilterAdditionalRow({
        advancedSearchFilterCode,
        advancedSearchFilterResourceName,
        advancedSearchFilterContent,
        advancedSearchFilterBuilder,
    });
    return (
        <FormFieldCustomAdvancedSearch
            {...props}
            advancedSearchDataGridProps={{
                toolbarAdditionalRow,
                ...advancedSearchDataGridProps,
            }}
        >
            {props.children}
        </FormFieldCustomAdvancedSearch>
    );
};

export default FormFieldAdvancedSearchFilters;
