package org.hiranoaiku.mikrolab.controller;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import org.hiranoaiku.mikrolab.model.Clock;

/**
 * Created by Hikaru on 05.03.2016.
 */
public class AudioSpeaker {

    private AudioTrack audioTrack;
    private Clock clock;
    private byte[] data;
    private int previousIndex, index;
    private long startTime;

    public static int SAMPLE_RATE = 44000;
    public static int BUFFER_SIZE = 22000;

    public AudioSpeaker() {
        data = new byte[AudioSpeaker.BUFFER_SIZE];
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, BUFFER_SIZE, AudioTrack.MODE_STREAM);
        flushBuffer();
        audioTrack.play();
    }

    public void assignClock(Clock clock) {
        this.clock = clock;
    }

    public void flushBuffer() {
        // запись последнего состояния до конца буфера
        for(index=previousIndex; index < data.length; index++) data[index] = data[previousIndex];
        previousIndex = 0;
        // дамп буфера в поток воспроизведения
        audioTrack.write(data, 0, data.length);
        // сброс таймера начала времени семплирования
        if(clock != null) startTime = clock.getCount();
        else startTime = 0;
    }

    public void sampleBit(int bit) {
        index = (int) ((clock.getCount()-startTime) * SAMPLE_RATE / Clock.BASE_FREQUENCY);
        if(index < BUFFER_SIZE && index >= 0 && index >= previousIndex) {
            for(int i = previousIndex; i < index; i++) data[i] = data[previousIndex];
            data[index] = (byte) (bit != 0 ? 127 : 0);
            previousIndex = index;
        } else {
            flushBuffer();
        }
    }
}
