import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { useFormDialog } from './FormDialog';

const mocks = vi.hoisted(() => ({
    dialogProps: null as any,
}));

vi.mock('../../AppButtons', () => ({
    useFormDialogButtons: () => [
        { value: false, text: 'Cancelar' },
        { value: true, text: 'Guardar' },
    ],
    useCloseDialogButtons: () => [{ value: false, text: 'Tancar' }],
}));

vi.mock('../Dialog', () => ({
    __esModule: true,
    default: ({
        open,
        buttons,
        children,
    }: {
        open?: boolean;
        buttons?: Array<{ text: string }>;
        children?: React.ReactNode;
    }) => {
        mocks.dialogProps = { buttons };
        return open ? (
            <div>
                <div>{buttons?.map((button) => button.text).join('|')}</div>
                {children}
            </div>
        ) : null;
    },
}));

vi.mock('./MuiForm', () => ({
    __esModule: true,
    default: ({
        onSaveActionPresentChange,
        children,
    }: {
        onSaveActionPresentChange?: (isPresent: boolean) => void;
        children?: React.ReactNode;
    }) => {
        React.useEffect(() => {
            onSaveActionPresentChange?.(false);
        }, [onSaveActionPresentChange]);
        return <div>{children}</div>;
    },
}));

describe('FormDialog', () => {
    const TestComponent = () => {
        const [show, dialogComponent] = useFormDialog('entornApp');
        const shownRef = React.useRef(false);

        React.useEffect(() => {
            if (!shownRef.current) {
                shownRef.current = true;
                show(undefined).catch(() => {});
            }
        }, [show]);

        return dialogComponent;
    };

    it('useFormDialog_quanNoHiHaAccioDeGuardar_mostraNomesElBotoTancar', async () => {
        render(
            <TestComponent />
        );

        expect(await screen.findByText('Tancar')).toBeInTheDocument();
        expect(mocks.dialogProps.buttons).toEqual([{ value: false, text: 'Tancar' }]);
    });
});
