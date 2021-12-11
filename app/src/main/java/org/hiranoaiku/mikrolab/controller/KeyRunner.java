package org.hiranoaiku.mikrolab.controller;

import org.hiranoaiku.mikrolab.model.Ports;

/**
 * Created by Hikaru on 04.03.2016.
 */
public class KeyRunner {

    public static final int NO_KEY = 24;

    public int keyPressed;
    public int keyCodesInv[] = new int[] {
            0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80,
            0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80,
            0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80,
            0x00
    };

    public KeyRunner() {
        keyPressed = KeyRunner.NO_KEY;
    }

    /**
     * Метод для триггера нажатой клавиши. Вызывается из Activity по событию onTouch / DOWN
     * @param index
     */
    public void setPressedKeyIndex(int index) {
        keyPressed = index;
    }

    /**
     * Сбрасывает нажатую клавишу
     */
    public void resetPressedKey() {
        keyPressed = KeyRunner.NO_KEY;
    }

    /**
     * Имитирует нажатия клавиш на реальной клавишной матрице.
     * Ссоединяет по заданной матрице сканирующий порт со считывающим
     * @param ports
     * @param portInName
     * @param portOutName
     */
    public void invokePress(Ports ports, char portInName, char portOutName) {
        int stat = ports.getPort(portInName)&0x70;
        char data = 0xFF;

        if(keyPressed == KeyRunner.NO_KEY) ports.setPort(portOutName, (char) 0xFF);

        if((stat&0x10) == 0x10 && keyPressed < 8) {
            data = (char) ~keyCodesInv[keyPressed];
        }
        else if((stat&0x20) == 0x20 && keyPressed >= 8 && keyPressed < 16) {
            data = (char) ~keyCodesInv[keyPressed];
        }
        else if((stat&0x40) == 0x40 && keyPressed >= 16 && keyPressed < 24) {
            data = (char) ~keyCodesInv[keyPressed];
        }
        ports.setPort(portOutName, data);
    }
}
