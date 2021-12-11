package org.hiranoaiku.mikrolab.controller;

import android.app.Activity;

import org.hiranoaiku.mikrolab.model.Memory;
import org.hiranoaiku.mikrolab.model.Ports;
import org.hiranoaiku.mikrolab.view.DataView;

import java.util.HashMap;

/**
 * Created by Hikaru on 01.03.2016.
 */
public class DisplayRunner implements Runnable {

    private HashMap<DataView, Integer> display_memory;
    private HashMap<DataView, Character> display_ports;
    private Memory memory;
    private Activity activity;
    private Ports ports;
    private boolean running;

    public DisplayRunner(Activity activity, Memory memory, Ports ports) {
        display_memory = new HashMap<DataView, Integer>();
        display_ports = new HashMap<DataView, Character>();
        this.memory = memory;
        this.activity = activity;
        this.ports = ports;
    }

    public void run() {
        running = true;
        while(running) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (DataView dv : display_memory.keySet()) {
                        dv.setData(memory.read(display_memory.get(dv)));
                    }
                    for (DataView dv : display_ports.keySet()) {
                        dv.setData(ports.getPort(display_ports.get(dv)));
                    }
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Bind data display view to some memory address
     * @param dataView
     * @param bindAddress
     */
    public void bindMemoryCell(DataView dataView, int bindAddress) {
        display_memory.put(dataView, bindAddress);
    }

    /**
     * Bind data display view to some port
     * @param dataView
     * @param portName
     */
    public void bindPort(DataView dataView, char portName) {
        display_ports.put(dataView, portName);
    }

    public void stop() {
        running = false;
    }
}
