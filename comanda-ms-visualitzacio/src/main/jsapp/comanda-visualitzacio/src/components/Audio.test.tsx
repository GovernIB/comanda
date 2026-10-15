import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { playAudio, useAudio } from './Audio';

describe('Audio', () => {
    let mockPlay: any;
    let mockPause: any;

    beforeEach(() => {
        vi.clearAllMocks();
        mockPlay = vi.fn().mockResolvedValue(undefined);
        mockPause = vi.fn();

        (global as any).Audio = vi.fn().mockImplementation(() => ({
            play: mockPlay,
            pause: mockPause,
            currentTime: 0,
        }));
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    describe('playAudio', () => {
        it('playAudio_creaInstanciaDeAudioYReproduce', () => {
            playAudio('SOUND_01');
            expect((global as any).Audio).toHaveBeenCalledWith(expect.stringContaining('sound-01.mp3'));
            expect(mockPlay).toHaveBeenCalled();
        });

        it('playAudio_usaElSrcDirecto_siNoEstaEnElMapa', () => {
            playAudio('https://example.com/custom.mp3');
            expect((global as any).Audio).toHaveBeenCalledWith('https://example.com/custom.mp3');
            expect(mockPlay).toHaveBeenCalled();
        });
    });

    describe('useAudio', () => {
        it('useAudio_play_inicializaYReproduce', () => {
            const { result } = renderHook(() => useAudio('SOUND_05'));
            act(() => { result.current.play(); });
            expect((global as any).Audio).toHaveBeenCalledWith(expect.stringContaining('sound-05.mp3'));
            expect(mockPlay).toHaveBeenCalled();
        });

        it('useAudio_play_reiniciaElTiempoAntesDeReproducir', () => {
            const { result } = renderHook(() => useAudio('SOUND_00'));
            const mockAudioInstance = { play: mockPlay, pause: mockPause, currentTime: 15 };
            ((global as any).Audio).mockReturnValue(mockAudioInstance);

            act(() => { result.current.play(); });
            expect(mockAudioInstance.currentTime).toBe(0);
            expect(mockPlay).toHaveBeenCalled();
        });

        it('useAudio_pause_detieneLaReproduccion', () => {
            const { result } = renderHook(() => useAudio('SOUND_10'));
            act(() => { result.current.play(); });
            act(() => { result.current.pause(); });
            expect(mockPause).toHaveBeenCalled();
        });

        it('useAudio_stop_detieneYReiniciaElTiempo', () => {
            const { result } = renderHook(() => useAudio('SOUND_16'));
            act(() => { result.current.play(); });
            act(() => { result.current.stop(); });
            expect(mockPause).toHaveBeenCalled();

            const audioInstance = ((global as any).Audio).mock.results[0].value;
            expect(audioInstance.currentTime).toBe(0);
        });
    });
});
