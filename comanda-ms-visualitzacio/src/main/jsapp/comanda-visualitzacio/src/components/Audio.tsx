import { useRef, useCallback } from 'react';
import nota from '../assets/la.mp3';

export const useAudio = (src: string) => {
    const audioRef = useRef<HTMLAudioElement | null>(null);

    const play = useCallback(() => {
        if (!audioRef.current) {
            audioRef.current = new Audio(src);
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

export const useDefaultAudio = () => useAudio(nota)