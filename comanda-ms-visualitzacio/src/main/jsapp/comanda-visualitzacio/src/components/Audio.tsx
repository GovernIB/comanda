import { useRef, useCallback } from 'react';
import sound00 from '../assets/alarma/sound-00.mp3';
import sound01 from '../assets/alarma/sound-01.mp3';
import sound02 from '../assets/alarma/sound-02.mp3';
import sound03 from '../assets/alarma/sound-03.mp3';
import sound04 from '../assets/alarma/sound-04.mp3';
import sound05 from '../assets/alarma/sound-05.mp3';
import sound06 from '../assets/alarma/sound-06.mp3';
import sound07 from '../assets/alarma/sound-07.mp3';
import sound08 from '../assets/alarma/sound-08.mp3';
import sound09 from '../assets/alarma/sound-09.mp3';
import sound10 from '../assets/alarma/sound-10.mp3';
import sound11 from '../assets/alarma/sound-11.mp3';
import sound12 from '../assets/alarma/sound-12.mp3';
import sound13 from '../assets/alarma/sound-13.mp3';
import sound14 from '../assets/alarma/sound-14.mp3';
import sound15 from '../assets/alarma/sound-15.mp3';
import sound16 from '../assets/alarma/sound-16.mp3';

const AUDIO_MAP: Record<string, string> = {
    'SOUND_00': sound00,
    'SOUND_01': sound01,
    'SOUND_02': sound02,
    'SOUND_03': sound03,
    'SOUND_04': sound04,
    'SOUND_05': sound05,
    'SOUND_06': sound06,
    'SOUND_07': sound07,
    'SOUND_08': sound08,
    'SOUND_09': sound09,
    'SOUND_10': sound10,
    'SOUND_11': sound11,
    'SOUND_12': sound12,
    'SOUND_13': sound13,
    'SOUND_14': sound14,
    'SOUND_15': sound15,
    'SOUND_16': sound16,
};

export const playAudio = (src:string) => {
    new Audio(AUDIO_MAP[src] || src).play().catch(console.warn);
};

export const useAudio = (src: string) => {
    const audioRef = useRef<HTMLAudioElement | null>(null);

    const play = useCallback(() => {
        audioRef.current ??= new Audio(AUDIO_MAP[src] || src);
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