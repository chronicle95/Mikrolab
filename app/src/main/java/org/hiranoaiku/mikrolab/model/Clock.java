package org.hiranoaiku.mikrolab.model;

/**
 * Created by Hikaru on 28.02.2016.
 */
public class Clock implements Runnable {

    public static int BASE_FREQUENCY = 2000000;
    public static int DELAY_PERIOD = 200000;
    public static int DELAY_TIME = 10;

    private Processor processor;
    private long counter, stepIrqCounter;
    private boolean running, stepInterruptEnabled, stepInterruptRequest;
    private long subCounter;

    private int delayTime, delayPeriod;

    public Clock(Processor processor) {
        this.processor = processor;
        running = false;
        counter = 0;
        delayTime = Clock.DELAY_TIME;
        delayPeriod = Clock.DELAY_PERIOD;
        subCounter = delayPeriod;
        stepInterruptEnabled = false;
    }

    public void enableStepMode(boolean q)
    {
        stepInterruptEnabled = q;
    }

//    public void stepIRQ()
//    {
//        stepInterruptRequest = true;
//    }

    public void run() {
        running = true;
        while(running) {
            processor.step();
            counter++;
//            if (stepInterruptEnabled && stepInterruptRequest)
//            {
//                processor.interrupt(0xFF);
//                stepInterruptRequest = false;
//            }

            try {
                subCounter--;
                if(subCounter == 0) {
                    Thread.sleep(delayTime);
                    subCounter = delayPeriod;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        counter = 0;
        running = false;
    }

    public long getCount() {
        return counter;
    }
}
