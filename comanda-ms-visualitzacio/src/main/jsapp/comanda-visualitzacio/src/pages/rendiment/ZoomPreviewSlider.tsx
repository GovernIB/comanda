import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Box, Button, Slider, Typography } from '@mui/material';
import { alpha, useTheme } from '@mui/material/styles';
import { DatasourcePoolMetrics, JmsQueueMetrics, WorkerPoolMetrics, ZoomTimeRange } from './types';
import {
    buildPreviewChartModel,
    centerPctRange,
    formatLocalDateTime,
    formatTimeShort,
    getBucketCenterMs,
    panPctRange,
    parseTimestampMs,
    pctRangeToTimeRange,
    pctToTimestamp,
    PerformanceChartSeries,
    resolveTimeDomain,
    TimeDomain,
    timeRangeToPctRange,
} from './chartModel';

export interface ZoomPreviewSliderProps {
    datasources: DatasourcePoolMetrics[];
    workerPools?: WorkerPoolMetrics[];
    jmsQueues?: JmsQueueMetrics[];
    zoomRange: ZoomTimeRange | null;
    previewBucketSeconds?: number;
    windowStart?: string | null;
    windowEnd?: string | null;
    collectedAt?: string | null;
    onZoomChange: (range: ZoomTimeRange | null) => void;
    onResetZoom: () => void;
}

interface PreviewPanState {
    mode: 'pan' | 'brush';
    startClientX: number;
    anchorStartPct: number;
    anchorEndPct: number;
    latestRange: [number, number];
}

const SPARKLINE_WIDTH = 1000;
const SPARKLINE_HEIGHT = 56;
const SPARKLINE_TOP_PAD = 4;
const SPARKLINE_BOTTOM_PAD = 2;
const SPARKLINE_USABLE_HEIGHT = SPARKLINE_HEIGHT - SPARKLINE_TOP_PAD - SPARKLINE_BOTTOM_PAD;
const SPARKLINE_BASELINE_Y = SPARKLINE_HEIGHT - SPARKLINE_BOTTOM_PAD;

const PreviewSparklineSvg = React.memo<{
    series: PerformanceChartSeries[];
    timestamps: string[];
    domain: TimeDomain | null;
}>(({ series, timestamps, domain }) => {
    const paths = React.useMemo(() => {
        const pointCount = timestamps.length;
        let maxVal = 1;
        for (const s of series) {
            const data = s.data ?? [];
            for (let i = 0; i < data.length; i++) {
                const v = data[i];
                if (typeof v === 'number' && Number.isFinite(v) && v > maxVal) {
                    maxVal = v;
                }
            }
        }

        const xCoords: number[] = new Array<number>(pointCount);
        const spanMs = domain ? domain.endMs - domain.startMs : 0;
        for (let i = 0; i < pointCount; i++) {
            const tsMs = domain ? parseTimestampMs(timestamps[i]) : null;
            if (domain && tsMs != null && spanMs > 0) {
                const centerMs = getBucketCenterMs(tsMs, domain);
                xCoords[i] = Number((((centerMs - domain.startMs) / spanMs) * SPARKLINE_WIDTH).toFixed(2));
            } else {
                xCoords[i] = pointCount <= 1 ? 0 : Number(((i / (pointCount - 1)) * SPARKLINE_WIDTH).toFixed(2));
            }
        }
        const yForValue = (v: number): number =>
            Number((SPARKLINE_BASELINE_Y - (Math.max(0, v) / maxVal) * SPARKLINE_USABLE_HEIGHT).toFixed(2));

        const elements: React.ReactNode[] = [];
        const barWidth =
            domain && domain.bucketSeconds > 1 && spanMs > 0
                ? Math.max(1.5, Math.min(8, (((domain.bucketSeconds * 1000) / spanMs) * SPARKLINE_WIDTH) * 0.6))
                : Math.max(1.5, Math.min(8, (SPARKLINE_WIDTH / Math.max(1, pointCount)) * 0.45));

        for (let sIdx = 0; sIdx < series.length; sIdx++) {
            const s = series[sIdx];
            const data = s.data ?? [];
            const color = s.color ?? '#1976d2';
            const keyBase = String(s.id ?? sIdx);

            if (s.type === 'bar') {
                const segments: string[] = [];
                for (let i = 0; i < data.length; i++) {
                    const v = data[i];
                    if (typeof v === 'number' && Number.isFinite(v) && v > 0) {
                        const x = xCoords[i];
                        const y = yForValue(v);
                        segments.push(`M${x},${SPARKLINE_BASELINE_Y}V${y}`);
                    }
                }
                if (segments.length > 0) {
                    elements.push(
                        <path
                            key={`${keyBase}-bar`}
                            d={segments.join(' ')}
                            fill="none"
                            stroke={color}
                            strokeOpacity={0.65}
                            strokeWidth={barWidth}
                            vectorEffect="non-scaling-stroke"
                        />
                    );
                }
            } else {
                const pts: Array<{ x: number; y: number }> = [];
                for (let i = 0; i < data.length; i++) {
                    const v = data[i];
                    if (typeof v === 'number' && Number.isFinite(v)) {
                        pts.push({ x: xCoords[i], y: yForValue(v) });
                    }
                }
                if (pts.length >= 2) {
                    const lineCmds = pts.map((p, idx) => `${idx === 0 ? 'M' : 'L'}${p.x},${p.y}`).join(' ');
                    if (s.area) {
                        const areaD = `M${pts[0].x},${SPARKLINE_BASELINE_Y} ${pts.map(p => `L${p.x},${p.y}`).join(' ')} L${pts[pts.length - 1].x},${SPARKLINE_BASELINE_Y} Z`;
                        elements.push(
                            <path
                                key={`${keyBase}-area`}
                                d={areaD}
                                fill={color}
                                fillOpacity={0.22}
                            />
                        );
                    }
                    elements.push(
                        <path
                            key={`${keyBase}-line`}
                            d={lineCmds}
                            fill="none"
                            stroke={color}
                            strokeWidth={1.5}
                            vectorEffect="non-scaling-stroke"
                        />
                    );
                }
            }
        }
        return elements;
    }, [series, timestamps, domain]);

    return (
        <svg
            data-testid="zoom-preview-svg"
            aria-hidden="true"
            viewBox={`0 0 ${SPARKLINE_WIDTH} ${SPARKLINE_HEIGHT}`}
            preserveAspectRatio="none"
            width="100%"
            height="100%"
            style={{ display: 'block' }}
        >
            {paths}
        </svg>
    );
});

const ZoomPreviewSlider: React.FC<ZoomPreviewSliderProps> = ({
    datasources,
    workerPools = [],
    jmsQueues = [],
    zoomRange,
    previewBucketSeconds,
    windowStart,
    windowEnd,
    collectedAt,
    onZoomChange,
    onResetZoom,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();
    const primaryColor = theme.palette.primary.main;
    const errorColor = theme.palette.error.main;
    const successColor = theme.palette.success.main;

    const previewColors = React.useMemo(
        () => ({
            inUseCount: primaryColor,
            waitCount: errorColor,
            workerActiveCount: successColor,
            workerQueueSize: '#f4511e',
            jmsDeliveringCount: '#00897b',
            jmsPendingCount: '#ff8f00',
        }),
        [primaryColor, errorColor, successColor]
    );

    const preview = React.useMemo(
        () => buildPreviewChartModel(datasources, previewColors, workerPools, jmsQueues),
        [datasources, previewColors, workerPools, jmsQueues]
    );

    const previewDomain = React.useMemo(
        () =>
            resolveTimeDomain(preview.timestamps, {
                bucketSeconds: previewBucketSeconds,
                windowStart,
                windowEnd,
                fallbackEnd: collectedAt,
            }),
        [preview.timestamps, previewBucketSeconds, windowStart, windowEnd, collectedAt]
    );

    const committedPct = React.useMemo(
        () => timeRangeToPctRange(preview.timestamps, zoomRange, previewDomain),
        [preview.timestamps, zoomRange, previewDomain]
    );

    const [sliderValue, setSliderValue] = React.useState<[number, number]>(committedPct);
    const [isPanning, setIsPanning] = React.useState<boolean>(false);
    const draggingRef = React.useRef<boolean>(false);
    const previewBoxRef = React.useRef<HTMLDivElement | null>(null);
    const panStateRef = React.useRef<PreviewPanState | null>(null);

    React.useEffect(() => {
        if (!draggingRef.current) {
            setSliderValue(committedPct);
        }
    }, [committedPct]);

    if (preview.timestamps.length < 2) {
        return null;
    }

    const formatPctTime = (pct: number): string =>
        formatTimeShort(pctToTimestamp(preview.timestamps, pct, previewDomain));

    const startCaption = previewDomain
        ? formatTimeShort(formatLocalDateTime(previewDomain.startMs))
        : formatTimeShort(preview.timestamps[0]);
    const endCaption = previewDomain
        ? formatTimeShort(formatLocalDateTime(previewDomain.endMs))
        : formatTimeShort(preview.timestamps[preview.timestamps.length - 1]);

    const scrimBg = alpha(theme.palette.background.default, 0.65);
    const activeBorder = theme.palette.primary.main;
    const isWindowZoomed = sliderValue[1] - sliderValue[0] < 99.5;

    const handleSliderChange = (_event: Event, value: number | number[]) => {
        if (!Array.isArray(value) || value.length < 2) {
            return;
        }
        draggingRef.current = true;
        setSliderValue([value[0], value[1]]);
    };

    const handleSliderCommitted = (_event: Event | React.SyntheticEvent, value: number | number[]) => {
        draggingRef.current = false;
        if (!Array.isArray(value) || value.length < 2) {
            return;
        }
        const nextRange = pctRangeToTimeRange(preview.timestamps, value[0], value[1], previewDomain);
        onZoomChange(nextRange);
    };

    const handlePreviewPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
        if (e.pointerType === 'mouse' && e.button !== 0) {
            return;
        }
        const box = previewBoxRef.current;
        if (!box) {
            return;
        }
        const rect = box.getBoundingClientRect();
        if (rect.width <= 0) {
            return;
        }
        const clickPct = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100));
        try {
            e.currentTarget.setPointerCapture?.(e.pointerId);
        } catch {
            // Pointer capture may not be supported in synthetic test environments
        }

        const span = sliderValue[1] - sliderValue[0];
        if (span < 99.5) {
            const initialRange: [number, number] =
                clickPct >= sliderValue[0] && clickPct <= sliderValue[1]
                    ? [sliderValue[0], sliderValue[1]]
                    : centerPctRange(sliderValue[0], sliderValue[1], clickPct);
            setSliderValue(initialRange);
            panStateRef.current = {
                mode: 'pan',
                startClientX: e.clientX,
                anchorStartPct: initialRange[0],
                anchorEndPct: initialRange[1],
                latestRange: initialRange,
            };
        } else {
            const initialRange: [number, number] = [clickPct, clickPct];
            panStateRef.current = {
                mode: 'brush',
                startClientX: e.clientX,
                anchorStartPct: clickPct,
                anchorEndPct: clickPct,
                latestRange: initialRange,
            };
        }
        draggingRef.current = true;
        setIsPanning(true);
    };

    const handlePreviewPointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
        const state = panStateRef.current;
        const box = previewBoxRef.current;
        if (!state || !box) {
            return;
        }
        const rect = box.getBoundingClientRect();
        if (rect.width <= 0) {
            return;
        }
        let next: [number, number];
        if (state.mode === 'pan') {
            const deltaPct = ((e.clientX - state.startClientX) / rect.width) * 100;
            next = panPctRange(state.anchorStartPct, state.anchorEndPct, deltaPct);
        } else {
            const currentPct = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100));
            next = [
                Math.min(state.anchorStartPct, currentPct),
                Math.max(state.anchorStartPct, currentPct),
            ];
        }
        state.latestRange = next;
        setSliderValue(next);
    };

    const finishPreviewDrag = (e: React.PointerEvent<HTMLDivElement>, cancelled: boolean) => {
        const state = panStateRef.current;
        if (!state) {
            return;
        }
        panStateRef.current = null;
        draggingRef.current = false;
        setIsPanning(false);
        try {
            if (e.currentTarget.hasPointerCapture?.(e.pointerId)) {
                e.currentTarget.releasePointerCapture(e.pointerId);
            }
        } catch {
            // Pointer capture cleanup best-effort
        }
        if (cancelled) {
            setSliderValue(committedPct);
            return;
        }
        const [startPct, endPct] = state.latestRange;
        if (state.mode === 'brush' && endPct - startPct < 0.5) {
            setSliderValue(committedPct);
            return;
        }
        onZoomChange(pctRangeToTimeRange(preview.timestamps, startPct, endPct, previewDomain));
    };

    return (
        <Box sx={{ mt: 2, px: 1, flexShrink: 0 }} data-testid="zoom-preview-slider">
            <Box
                ref={previewBoxRef}
                data-testid="zoom-preview-bar"
                onPointerDown={handlePreviewPointerDown}
                onPointerMove={handlePreviewPointerMove}
                onPointerUp={e => finishPreviewDrag(e, false)}
                onPointerCancel={e => finishPreviewDrag(e, true)}
                sx={{
                    position: 'relative',
                    width: '100%',
                    height: 56,
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    overflow: 'hidden',
                    bgcolor: theme.palette.mode === 'light' ? 'grey.50' : 'grey.900',
                    touchAction: 'none',
                    userSelect: 'none',
                    cursor: isPanning ? 'grabbing' : isWindowZoomed ? 'grab' : 'crosshair',
                }}
            >
                <Box sx={{ width: '100%', height: '100%', pointerEvents: 'none' }}>
                    <PreviewSparklineSvg
                        series={preview.series}
                        timestamps={preview.timestamps}
                        domain={previewDomain}
                    />
                </Box>

                {/* Active zoomed window highlight */}
                {isWindowZoomed && (
                    <Box
                        sx={{
                            position: 'absolute',
                            top: 0,
                            bottom: 0,
                            left: `${sliderValue[0]}%`,
                            width: `${sliderValue[1] - sliderValue[0]}%`,
                            bgcolor: alpha(activeBorder, 0.08),
                            pointerEvents: 'none',
                        }}
                    />
                )}

                {/* Left dimmed mask */}
                {sliderValue[0] > 0 && (
                    <Box
                        sx={{
                            position: 'absolute',
                            top: 0,
                            bottom: 0,
                            left: 0,
                            width: `${sliderValue[0]}%`,
                            boxSizing: 'border-box',
                            bgcolor: scrimBg,
                            borderRight: `2px solid ${activeBorder}`,
                            pointerEvents: 'none',
                        }}
                    />
                )}
                {/* Right dimmed mask */}
                {sliderValue[1] < 100 && (
                    <Box
                        sx={{
                            position: 'absolute',
                            top: 0,
                            bottom: 0,
                            right: 0,
                            width: `${100 - sliderValue[1]}%`,
                            boxSizing: 'border-box',
                            bgcolor: scrimBg,
                            borderLeft: `2px solid ${activeBorder}`,
                            pointerEvents: 'none',
                        }}
                    />
                )}
            </Box>

            <Slider
                size="small"
                min={0}
                max={100}
                step={0.1}
                disableSwap
                value={sliderValue}
                onChange={handleSliderChange}
                onChangeCommitted={handleSliderCommitted}
                valueLabelDisplay="auto"
                valueLabelFormat={formatPctTime}
                getAriaLabel={() => t($ => $.page.rendiment.chart.zoomSliderLabel)}
                sx={{ py: 0.75, mt: 0.25 }}
            />

            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: 1,
                }}
            >
                <Typography variant="caption" color="text.secondary">
                    {startCaption}
                </Typography>
                {zoomRange && (
                    <Button size="small" variant="text" onClick={onResetZoom}>
                        {t($ => $.page.rendiment.chart.resetZoom)}
                    </Button>
                )}
                <Typography variant="caption" color="text.secondary">
                    {endCaption}
                </Typography>
            </Box>
        </Box>
    );
};

export default React.memo(ZoomPreviewSlider);
