import * as React from 'react';
import { useTranslation } from 'react-i18next';

export type FasesCompactacioProps = {
  s?: number | null; // compactacioSetmanalMesos
  m?: number | null; // compactacioMensualMesos
  e?: number | null; // eliminacioMesos
  total?: number; // default 48
  style?: React.CSSProperties;
  className?: string;
};

const FasesCompactacio: React.FC<FasesCompactacioProps> = ({ s, m, e, total = 48, style, className }) => {
  const { t } = useTranslation();

  const clamp = React.useCallback((v: number) => Math.max(0, Math.min(total, v)), [total]);
  const isPos = (v: unknown) => v != null && Number(v as number | string) > 0 && !Number.isNaN(Number(v as number | string));

  const sC = isPos(s) ? clamp(Number(s)) : 0;
  const mC = isPos(m) ? clamp(Number(m)) : 0;
  const eRaw = e;
  const E = eRaw == null || Number(eRaw) <= 0 ? total : clamp(Number(eRaw));

  const segments: { from: number; to: number; color: string; title: string }[] = [];

  if (!isPos(s) && !isPos(m)) {
    // Case A: s<=0 and m<=0 -> Daily 0 -> E
    if (E > 0) segments.push({ from: 0, to: clamp(E), color: '#ffef99', title: t('page.apps.progress.diaries') });
  } else if (isPos(s) && !isPos(m)) {
    // Case B: s>0 and m<=0 -> Daily 0->s; Weekly s->E
    if (sC > 0) segments.push({ from: 0, to: sC, color: '#ffef99', title: t('page.apps.progress.diaries') });
    if (E > sC) segments.push({ from: sC, to: clamp(E), color: '#b6f2b6', title: t('page.apps.progress.weeklies') });
  } else if (!isPos(s) && isPos(m)) {
    // Case D: Daily 0->m; Monthly m->E
    if (mC > 0) segments.push({ from: 0, to: mC, color: '#ffef99', title: t('page.apps.progress.diaries') });
    if (E > mC) segments.push({ from: mC, to: clamp(E), color: '#b6e0ff', title: t('page.apps.progress.monthlies') });
  } else {
    // Case C: s>0 and m>0 -> Daily 0->s; Weekly s->m; Monthly m->E
    if (sC > 0) segments.push({ from: 0, to: sC, color: '#ffef99', title: t('page.apps.progress.diaries') });
    if (mC > sC) segments.push({ from: sC, to: mC, color: '#b6f2b6', title: t('page.apps.progress.weeklies') });
    if (E > mC) segments.push({ from: mC, to: clamp(E), color: '#b6e0ff', title: t('page.apps.progress.monthlies') });
  }

  const bars = segments
    .filter((sg) => sg.to > sg.from)
    .map((sg, idx) => {
      const widthPct = ((sg.to - sg.from) / total) * 100;
      return <div key={idx} title={sg.title} style={{ width: widthPct + '%', background: sg.color }} />;
    });

  const containerStyle: React.CSSProperties = {
    display: 'flex',
    height: 16,
    width: '100%',
    background: '#eee',
    borderRadius: 8,
    overflow: 'hidden',
    border: '1px solid #ddd',
    ...style,
  };

  return (
    <div>
      <div className={className} style={containerStyle}>{bars.length ? bars : null}</div>
      <div style={{ position: 'relative', height: 18, marginTop: 4 }}>
        <div style={{ position: 'absolute', top: 8, left: 0, right: 0, height: 1, background: '#ccc' }} />
        <div style={{ position: 'absolute', left: 0, top: 0, transform: 'translateX(-0%)', fontSize: 10, color: '#666' }}>0</div>
        {Array.from({ length: Math.floor(total / 3) }, (_, i) => (i + 1) * 3).map((mVal) => {
          const leftPct = (mVal / total) * 100;
          return (
            <div key={mVal} style={{ position: 'absolute', left: leftPct + '%', top: 0, transform: 'translateX(-50%)', textAlign: 'center' }}>
              <div style={{ width: 1, height: 8, background: '#999', margin: '0 auto' }} />
              <div style={{ fontSize: 10, color: '#666' }}>{mVal}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default FasesCompactacio;
