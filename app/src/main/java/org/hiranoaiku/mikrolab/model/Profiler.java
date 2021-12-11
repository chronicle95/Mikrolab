package org.hiranoaiku.mikrolab.model;

import org.hiranoaiku.mikrolab.controller.AudioSpeaker;

/**
 * Created by Hikaru on 05.03.2016.
 */
public class Profiler implements Runnable {

    private int seconds;
    private Clock clock;
    private int frequency;

    private boolean running;

    public Profiler(Clock clock) {
        this.clock = clock;
        this.frequency = 0;
        this.seconds = 2;
    }

    public void run() {
        running = true;
        while(running) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reconfigure();
            System.out.println("Clock ticks ::: " + this.frequency);
            seconds += 2;
        }
    }

    public void stop(){
        running = false;
    }

    private void reconfigure() {
        frequency = (int) (clock.getCount() / seconds);
    }

}
