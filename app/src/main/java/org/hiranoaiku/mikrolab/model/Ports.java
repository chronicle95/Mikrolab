package org.hiranoaiku.mikrolab.model;

import org.hiranoaiku.mikrolab.controller.AudioSpeaker;
import org.hiranoaiku.mikrolab.controller.KeyRunner;

/**
 * Created by Hikaru on 01.03.2016.
 * Модель КР580ИК55. Программируемый паралельный интерфейс (ППИ).
 */
public class Ports {

    private KeyRunner keyRunner;
    private AudioSpeaker audioSpeaker;
    private char[] ports;
    private char[] ddr; ///< битовая таблица масок: 1 - вывод, 0 - ввод

    /* Стандартные порты + слово конфигурации */
    public static final char  portA = (char)0xF8,
            portB = (char)0xF9,
            portC = (char)0xFA,
            configWord = (char)0xFB;

    public Ports(KeyRunner keyRunner, AudioSpeaker audioSpeaker) {
        ports = new char[256];
        ddr = new char[256];
        ddr[Ports.portA] = 0x00;
        ddr[Ports.portB] = 0xFF;
        ddr[Ports.portC] = 0xF0;

        this.keyRunner = keyRunner;
        this.audioSpeaker = audioSpeaker;
    }

    /**
     * Выводит в порт игнорируя маску ввода-вывода
     * @param portName
     * @param value
     */
    public void setPort(char portName, char value) {
        ports[portName] = value;
    }

    /**
     * Считывает с порта игнорируя маску ввода-вывода
     * @param portName
     * @return
     */
    public char getPort(char portName) {
        return ports[portName];
    }

    /**
     * Выводит в порт по маске
     * @param portName
     * @param value
     */
    public void outPort(char portName, char value) {
        ports[portName] = (char) (ports[portName] & ~ddr[portName]);
        ports[portName] = (char) (ports[portName] | (value & ddr[portName]));

        if(portName == Ports.portC)
            keyRunner.invokePress(this, Ports.portC, Ports.portA);
        if(portName == Ports.portB)
            audioSpeaker.sampleBit(ports[Ports.portB] & 0x1);
        if(portName == Ports.configWord)
            checkConfig();
    }

    /**
     * Проверяет состояние конфигурационного слова.
     * Перенастраивает маски ввода-вывода на портах
     */
    private void checkConfig() {
        /* 1 - вывод, 0 - ввод */
        switch(ports[Ports.configWord]) {
            case 0x80:
                ddr[Ports.portA] = 0xFF;
                ddr[Ports.portC] = 0xFF;
                ddr[Ports.portB] = 0xFF;
                break;
            case 0x81:
                ddr[Ports.portA] = 0xFF;
                ddr[Ports.portC] = 0xF0;
                ddr[Ports.portB] = 0xFF;
                break;
            case 0x92:
                ddr[Ports.portA] = 0x00;
                ddr[Ports.portC] = 0xFF;
                ddr[Ports.portB] = 0x00;
                break;
        }
    }

    /**
     * Считывает байт из порта по маске ввода-вывода
     * @param portName
     * @return
     */
    public char inPort(char portName) {
        return (char) (ports[portName] & (~ddr[portName]));
    }

    /**
     * Сброс состояния портов
     */
    public void reset() {
        for(int i=0; i < ports.length; i++)
            ports[i] = 0;
    }

}
