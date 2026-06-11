import { useRef, useCallback } from 'react';
import notaDO from '../assets/alarma/DO.mp3';
import notaRE from '../assets/alarma/RE.mp3';
import notaMI from '../assets/alarma/MI.mp3';
import notaLA from '../assets/alarma/LA.mp3';

const AUDIO_MAP: Record<string, string> = {
    'DO': notaDO,
    'RE': notaRE,
    'MI': notaMI,
    'LA': notaLA,
};

export const playAudio = (src:string) => {
    new Audio(AUDIO_MAP[src] || src).play().catch(console.warn);
};

export const useAudio = (src: string) => {
    const audioRef = useRef<HTMLAudioElement | null>(null);

    const play = useCallback(() => {
        if (!audioRef.current) {
            audioRef.current = new Audio(AUDIO_MAP[src] || src);
        }
        audioRef.current.currentTime = 0;
        return audioRef.current.play().catch(console.warn);
    }, [src]);

    const pause = useCallback(() => {
        audioRef.current?.pause();
    }, []);

    const stop = useCallback(() => {
        if (audioRef.current) {
            audioRef.current.pause();
            audioRef.current.currentTime = 0;
        }
    }, []);

    return { play, pause, stop };
}