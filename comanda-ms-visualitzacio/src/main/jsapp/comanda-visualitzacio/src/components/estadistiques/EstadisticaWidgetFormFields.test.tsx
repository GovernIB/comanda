import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaWidgetFormFields, { DimensionsFields, PersonalitzatFields, VoraGraphicalFormEditor, hasVisualOverridesTitol } from './EstadisticaWidgetFormFields';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
    useResourceApiServiceMock: vi.fn(),
    findOptionsMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        typeof selector === 'function'
            ? selector({
                common: {
                    cancel: 'Cancel·lar',
                },
                generic: {
                    dimensio: 'Dimensió',
                },
                page: {
                    widget: {
                        form: {
                            periode: 'Període',
                            help: {
                                relatiuPuntReferencia: 'Ajuda punt de referència',
                                relatiuCount: 'Ajuda quantitat',
                                relatiueUnitat: 'Ajuda unitat',
                            },
                        },
                        editor: {
                            showBorderTop: 'Mostrar vora superior',
                            borderColorTop: 'Color vora superior',
                            borderWidthTop: 'Amplada vora superior',
                            voraTop: 'Vora superior',
                            showBorderRight: 'Mostrar vora dreta',
                            borderColorRight: 'Color vora dreta',
                            borderWidthRight: 'Amplada vora dreta',
                            voraRight: 'Vora dreta',
                            showBorderBottom: 'Mostrar vora inferior',
                            borderColorBottom: 'Color vora inferior',
                            borderWidthBottom: 'Amplada vora inferior',
                            voraBottom: 'Vora inferior',
                            showBorderLeft: 'Mostrar vora esquerra',
                            borderColorLeft: 'Color vora esquerra',
                            borderWidthLeft: 'Amplada vora esquerra',
                            voraLeft: 'Vora esquerra',
                        },
                    },
                },
            })
            : selector
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    FormField: ({
        name,
        disabled,
    }: {
        name: string;
        disabled?: boolean;
    }) => (
        <div data-testid={`form-field-${name}`} data-disabled={disabled ? 'true' : 'false'}>
            {name}
        </div>
    ),
    useFormContext: () => mocks.useFormContextMock(),
    useResourceApiService: () => mocks.useResourceApiServiceMock(),
}));

vi.mock('../FormFieldAdvancedSearchFilters.tsx', () => ({
    default: ({ name, advancedSearchColumns }: { name: string; advancedSearchColumns?: any }) => (
        <div data-testid={`advanced-search-${name}`} data-columns={JSON.stringify(advancedSearchColumns)}>{name}</div>
    ),
}));

vi.mock('../../util/requestUtils.ts', () => ({
    findOptions: (...args: unknown[]) => mocks.findOptionsMock(...args),
}));

const renderComponent = (data: unknown, children: React.ReactNode = <div>Contingut extra</div>) => {
    mocks.useFormContextMock.mockReturnValue({ data });
    return render(<EstadisticaWidgetFormFields>{children}</EstadisticaWidgetFormFields>);
};

describe('EstadisticaWidgetFormFields', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaWidgetFormFields_quanLaplicacioNoEstaLlesta_noRenderitzaRes', () => {
        // Comprova que el formulari espera que el recurs d'aplicacions estigui inicialitzat.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: false,
            find: vi.fn(),
        });

        const { container } = renderComponent({});

        expect(container).toBeEmptyDOMElement();
    });

    it('EstadisticaWidgetFormFields_quanNoHiHaAplicacio_mostraNomesElsCampsGenerals', () => {
        // Verifica que sense aplicació seleccionada només es mostren els camps base del formulari.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({});

        expect(screen.getByTestId('form-field-aplicacio')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-titol')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-descripcio')).toBeInTheDocument();
        expect(screen.queryByTestId('advanced-search-dimensionsValor')).not.toBeInTheDocument();
        expect(screen.queryByText('Contingut extra')).not.toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsModePresetAmbComptador_mostraElsCampsDependents', () => {
        // Comprova que el mode PRESET mostra el període, el comptador i els fills addicionals.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'PRESET',
            presetPeriode: 'DARRERS_N_DIES',
        });

        expect(screen.getByTestId('advanced-search-dimensionsValor')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-periodeMode')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-presetPeriode')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-presetCount')).toBeInTheDocument();
        expect(screen.getByText('Contingut extra')).toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsModeRelatiu_mostraElsCampsRelatius', () => {
        // Verifica que el mode RELATIU activa els controls específics de referència i unitat.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'RELATIU',
        });

        expect(screen.getByTestId('form-field-relatiuPuntReferencia')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-relatiuCount')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-relatiueUnitat')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-relatiuAlineacio')).toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsModeAbsolutDateRange_mostraLesDatesDIniciIFi', () => {
        // Comprova que el mode ABSOLUT amb rang de dates exposa els dos camps de data.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'ABSOLUT',
            absolutTipus: 'DATE_RANGE',
        });

        expect(screen.getByTestId('form-field-absolutTipus')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutDataInici')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutDataFi')).toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsPeriodeEspecificAmbAnyFix_habilitaElCampDAnyValor', () => {
        // Verifica que el camp de l'any específic queda habilitat quan el tipus de referència ho permet.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'ABSOLUT',
            absolutTipus: 'SPECIFIC_PERIOD_OF_YEAR',
            absolutAnyReferencia: 'SPECIFIC_YEAR',
        });

        expect(screen.getByTestId('form-field-absolutAnyReferencia')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutAnyValor')).toHaveAttribute('data-disabled', 'false');
        expect(screen.getByTestId('form-field-absolutPeriodeUnitat')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutPeriodeInici')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutPeriodeFi')).toBeInTheDocument();
    });
});

describe('PersonalitzatFields', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    const renderPersonalitzatFields = (hasOverrides: boolean) => {
        mocks.useFormContextMock.mockReturnValue({ data: {}, apiRef: { current: { setFieldValue: vi.fn() } } });
        return render(
            <PersonalitzatFields
                personalitzatLabel="Personalitzat"
                personalitzatHelp="Ajuda"
                personalitzatBadge="Hi ha elements personalitzats"
                hasOverrides={hasOverrides}
                onExpandedChange={vi.fn()}
            />
        );
    };

    it('PersonalitzatFields_quanNoHiHaCampsEmplenats_noMostraLaMarca', () => {
        // Reprodueix el bug reportat: la marca no s'ha de mostrar si l'usuari no ha emplenat cap camp.
        renderPersonalitzatFields(false);

        expect(screen.queryByTestId('personalitzat-badge')).not.toBeInTheDocument();
    });

    it('PersonalitzatFields_quanHiHaCampsEmplenats_mostraLaMarca', () => {
        // Verifica que la marca apareix quan el pare detecta valors que sobreescriuen la plantilla.
        renderPersonalitzatFields(true);

        expect(screen.getByTestId('personalitzat-badge')).toBeInTheDocument();
    });

    it('PersonalitzatFields_quanEsPlegaLaSeccio_laMarcaEsManteSiHiHaOverrides', () => {
        // Comprova que plegar la secció de personalització no amaga la marca ni esborra els overrides.
        renderPersonalitzatFields(true);

        fireEvent.click(screen.getByRole('button', { name: /Personalitzat/i }));

        expect(screen.getByTestId('personalitzat-badge')).toBeInTheDocument();
    });

    it('PersonalitzatFields_senseOverrides_noMostraElBotoDEliminarDisseny', () => {
        renderPersonalitzatFields(false);

        expect(screen.queryByRole('button', { name: 'Eliminar disseny' })).not.toBeInTheDocument();
    });

    it('PersonalitzatFields_ambOverrides_mostraElBotoDEliminarDissenyIEnClicarNetejaCadaCampPropi', () => {
        const setFieldValueMock = vi.fn();
        mocks.useFormContextMock.mockReturnValue({ data: {}, apiRef: { current: { setFieldValue: setFieldValueMock } } });
        render(
            <PersonalitzatFields
                personalitzatLabel="Personalitzat"
                personalitzatHelp="Ajuda"
                personalitzatBadge="Hi ha elements personalitzats"
                resetLabel="Eliminar disseny"
                hasOverrides
                overrideFields={['colorTitol', 'midaFontTitol']}
                onExpandedChange={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Eliminar disseny' }));

        expect(setFieldValueMock).toHaveBeenCalledWith('colorTitol', undefined);
        expect(setFieldValueMock).toHaveBeenCalledWith('midaFontTitol', undefined);
    });

    it('PersonalitzatFields_ambResetApiRef_netejaElsCampsAlFormulariIndicatEnLlocDelPropi', () => {
        // Als widgets Simple/Gràfic/Taula, PersonalitzatFields viu al formulari de dashboardItem però els
        // camps visuals viuen al formulari específic del widget: cal poder indicar quin apiRef netejar.
        const localSetFieldValueMock = vi.fn();
        const widgetSetFieldValueMock = vi.fn();
        mocks.useFormContextMock.mockReturnValue({ data: {}, apiRef: { current: { setFieldValue: localSetFieldValueMock } } });
        const widgetFormApiRef = { current: { setFieldValue: widgetSetFieldValueMock } };

        render(
            <PersonalitzatFields
                personalitzatLabel="Personalitzat"
                personalitzatHelp="Ajuda"
                personalitzatBadge="Hi ha elements personalitzats"
                resetLabel="Eliminar disseny"
                hasOverrides
                overrideFields={['colorText']}
                resetApiRef={widgetFormApiRef}
                onExpandedChange={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Eliminar disseny' }));

        expect(widgetSetFieldValueMock).toHaveBeenCalledWith('colorText', undefined);
        expect(localSetFieldValueMock).not.toHaveBeenCalled();
    });
});

describe('hasVisualOverridesTitol', () => {
    it('hasVisualOverridesTitol_senseCapCampEmplenat_retornaFals', () => {
        expect(hasVisualOverridesTitol({})).toBe(false);
        expect(hasVisualOverridesTitol({ titol: 'Text', tipusTitol: 'TIPUS_1', plantilla: { id: 1 } })).toBe(false);
    });

    it('hasVisualOverridesTitol_ambColorSubtitolEmplenat_retornaCert', () => {
        expect(hasVisualOverridesTitol({ colorSubtitol: '#123456' })).toBe(true);
    });

    it('hasVisualOverridesTitol_ambMidaFontSubtitolEmplenat_retornaCert', () => {
        expect(hasVisualOverridesTitol({ midaFontSubtitol: 16 })).toBe(true);
    });

    it('hasVisualOverridesTitol_ambMostrarVoraBottomAlSeuValorPerDefecte_retornaFals', () => {
        // mostrarVoraBottom=true és el valor per defecte de la columna (vora inferior visible per
        // defecte a totes les entitats persistides): per si sol no és una personalització real.
        expect(hasVisualOverridesTitol({ mostrarVoraBottom: true })).toBe(false);
    });

    it('hasVisualOverridesTitol_ambMostrarVoraBottomDiferentDelPerDefecte_retornaCert', () => {
        expect(hasVisualOverridesTitol({ mostrarVoraBottom: false })).toBe(true);
    });

    it('hasVisualOverridesTitol_ambNomesValorsPerDefecteDeTotsElsCampsDeVoraISubtitol_retornaFals', () => {
        expect(hasVisualOverridesTitol({
            posicioSubtitol: 'SOTA',
            separacioSubtitol: 0,
            mostrarVoraTop: false,
            ampleVoraTop: 1,
            mostrarVoraRight: false,
            ampleVoraRight: 1,
            mostrarVoraBottom: true,
            ampleVoraBottom: 1,
            mostrarVoraLeft: false,
            ampleVoraLeft: 1,
        })).toBe(false);
    });

    it('hasVisualOverridesTitol_ambQualsevolCostatDeVoraEmplenat_retornaCert', () => {
        expect(hasVisualOverridesTitol({ mostrarVoraTop: true })).toBe(true);
        expect(hasVisualOverridesTitol({ colorVoraRight: '#123456' })).toBe(true);
        expect(hasVisualOverridesTitol({ ampleVoraLeft: 2 })).toBe(true);
    });

    it('hasVisualOverridesTitol_ambPosicioONSeparacioDeSubtitolEmplenada_retornaCert', () => {
        expect(hasVisualOverridesTitol({ posicioSubtitol: 'COSTAT' })).toBe(true);
        expect(hasVisualOverridesTitol({ separacioSubtitol: 12 })).toBe(true);
    });

    describe('amb initialData (edició d\'un títol existent)', () => {
        // Bug: revertir posicioSubtitol a 'SOTA' (el valor per defecte de la columna) des d'un valor
        // personalitzat anterior (p.ex. 'COSTAT') no es detectava com a canvi perquè 'SOTA' coincideix
        // amb el valor de referència fix, i sense cap altre camp modificat el flag "personalitzat" no
        // s'actualitzava mai: el backend continuava aplicant sempre la plantilla i el canvi es perdia.
        it('hasVisualOverridesTitol_quanEsRevertLaPosicioAlValorPerDefecteDesDunAltreValorInicial_retornaCert', () => {
            expect(hasVisualOverridesTitol(
                { posicioSubtitol: 'SOTA' },
                { posicioSubtitol: 'COSTAT' }
            )).toBe(true);
        });

        it('hasVisualOverridesTitol_quanElValorNoHaCanviatRespecteLInicial_retornaFals', () => {
            expect(hasVisualOverridesTitol(
                { posicioSubtitol: 'SOTA' },
                { posicioSubtitol: 'SOTA' }
            )).toBe(false);
        });

        it('hasVisualOverridesTitol_quanElValorPerDefecteEsFixaExplicitamentDesDUnValorBuitInicial_retornaCert', () => {
            expect(hasVisualOverridesTitol(
                { posicioSubtitol: 'SOTA' },
                { posicioSubtitol: undefined }
            )).toBe(true);
        });

        it('hasVisualOverridesTitol_quanUnAltreCampSiCanviaRespecteLInicial_retornaCertIgualment', () => {
            expect(hasVisualOverridesTitol(
                { mostrarVoraBottom: false },
                { mostrarVoraBottom: true }
            )).toBe(true);
        });

        it('hasVisualOverridesTitol_senseInitialDataPeroAmbValorDiferentDelPerDefecte_retornaCertIgualment', () => {
            // El comportament previ (sense initialData, p.ex. en mode creació) es manté intacte.
            expect(hasVisualOverridesTitol({ posicioSubtitol: 'COSTAT' }, undefined)).toBe(true);
        });
    });
});

describe('DimensionsFields', () => {
    it('DimensionsFields_quanHiHaAplicacio_passaColumnesAmbDimensioTraduit', () => {
        mocks.useFormContextMock.mockReturnValue({
            data: { aplicacio: { id: 1 } },
        });
        render(<DimensionsFields />);
        const el = screen.getByTestId('advanced-search-dimensionsValor');
        expect(el).toBeInTheDocument();
        expect(el.getAttribute('data-columns')).toContain('Dimensió');
    });

    it('DimensionsFields_quanNoHiHaAplicacio_noMostraElCamp', () => {
        mocks.useFormContextMock.mockReturnValue({
            data: {},
        });
        const { container } = render(<DimensionsFields />);
        expect(container).toBeEmptyDOMElement();
    });
});

describe('VoraGraphicalFormEditor', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('VoraGraphicalFormEditor_quanEsRenderitza_mostraLes4ZonesClicablesSenseModalOberta', () => {
        mocks.useFormContextMock.mockReturnValue({ data: {} });
        render(<VoraGraphicalFormEditor />);

        expect(screen.getByTestId('vora-zone-Top')).toBeInTheDocument();
        expect(screen.getByTestId('vora-zone-Right')).toBeInTheDocument();
        expect(screen.getByTestId('vora-zone-Bottom')).toBeInTheDocument();
        expect(screen.getByTestId('vora-zone-Left')).toBeInTheDocument();
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('VoraGraphicalFormEditor_quanEsClicaUnaZona_obreLaModalNomesAmbElMostrarDAquellCostat', () => {
        mocks.useFormContextMock.mockReturnValue({ data: {} });
        render(<VoraGraphicalFormEditor />);

        fireEvent.click(screen.getByTestId('vora-zone-Top'));

        expect(screen.getByRole('dialog')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-mostrarVoraTop')).toBeInTheDocument();
        // Sense mostrarVoraTop actiu, no s'han de mostrar els camps de color/gruix.
        expect(screen.queryByTestId('form-field-colorVoraTop')).not.toBeInTheDocument();
        expect(screen.queryByTestId('form-field-ampleVoraTop')).not.toBeInTheDocument();
        // I no s'han de mostrar els camps d'altres costats.
        expect(screen.queryByTestId('form-field-mostrarVoraLeft')).not.toBeInTheDocument();
    });

    it('VoraGraphicalFormEditor_quanElCostatClicatTeMostrarActiu_tambeMostraColorIGruix', () => {
        mocks.useFormContextMock.mockReturnValue({ data: { mostrarVoraTop: true } });
        render(<VoraGraphicalFormEditor />);

        fireEvent.click(screen.getByTestId('vora-zone-Top'));

        expect(screen.getByTestId('form-field-colorVoraTop')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-ampleVoraTop')).toBeInTheDocument();
    });

    it('VoraGraphicalFormEditor_quanEsClicaUnAltreCostat_mostraElSeuPropiMostrar', () => {
        mocks.useFormContextMock.mockReturnValue({ data: { mostrarVoraTop: true } });
        render(<VoraGraphicalFormEditor />);

        fireEvent.click(screen.getByTestId('vora-zone-Left'));

        expect(screen.getByTestId('form-field-mostrarVoraLeft')).toBeInTheDocument();
        // No s'ha de veure el costat Top encara que estigui actiu: la modal només mostra un costat.
        expect(screen.queryByTestId('form-field-mostrarVoraTop')).not.toBeInTheDocument();
    });
});

