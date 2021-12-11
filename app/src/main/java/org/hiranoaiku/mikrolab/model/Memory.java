package org.hiranoaiku.mikrolab.model;

/**
 * Created by Hikaru on 28.02.2016.
 */
public class Memory {

    private char[] data;
    public int size;
    private int romTop, ramBottom;

    public Memory(int size, int romTop, int ramBottom) {
        data = new char[size];
        this.size = size;
        this.romTop = romTop;
        this.ramBottom = ramBottom;
        clear();
    }

    public char read(int address) {
        if(address >= 0 && address < this.size)
            return this.data[address];
        return 0;
    }

    public int readWord(int address) {
        if(address >= 0 && address+1 < this.size) {
            return (this.data[address] | (this.data[address+1]<<8))&0xffff;
        }
        return 0; // в остальных случаях читать как '0' (что близко к правде)
    }

    public void writeWord(int address, int word) {
        if(address >= this.ramBottom && address+1 < this.size) {
            this.data[address] = (char) (word & 0xff);
            this.data[address+1] = (char) ((word&0xff00)>>8);
        }
    }

    public void write(int address, char data) {
        if(address >= this.ramBottom && address < this.size) {
            this.data[address] = data;
        }
    }

    /**
     * Загрузить блок данных в память со смещением
     * @param memoryOffset
     * @param block
     */
    public void load(int memoryOffset, char[] block) {
        if(memoryOffset+block.length < this.size) {
            for (int i = 0; i < block.length; i++) {
                this.data[memoryOffset + i] = block[i];
            }
        }
    }

    public void clear() {
        for(int i = 0; i < this.size; i++)
            this.data[i] = 0;
    }

    public void clearRAM() {
        for(int i = this.ramBottom; i < this.size; i++) {
            this.data[i] = 0;
        }
    }

}
