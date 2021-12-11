package org.hiranoaiku.mikrolab.model;

/**
 * Created by Hikaru on 28.02.2016.
 */
public class Registers {
    public int IP, SP, WZ, BC, DE, HL;
    public char Acc;
    public char flags;

    public static final char    flagZ = 6,
                                flagS = 7,
                                flagCY = 0,
                                flagP = 2,
                                flagAC = 4;

    public Registers() {
        reset();
    }

    public void reset() {
        IP = 0; SP = 0; WZ = 0;
        BC = 0; DE = 0; HL = 0;
        Acc = 0;
        flags = 0;
    }

    public char getB() { return (char)((BC & 0xff00)>>8); }
    public char getH() { return (char)((HL & 0xff00)>>8); }
    public char getD() { return (char)((DE & 0xff00)>>8); }
    public char getC() { return (char)(BC & 0xff); }
    public char getE() { return (char)(DE & 0xff); }
    public char getL() { return (char)(HL & 0xff); }

    public void setB(char val) { BC = ((BC&0x00ff)|(val<<8)); }
    public void setC(char val) { BC = ((BC&0xff00)|val); }
    public void setD(char val) {
        DE = ((DE&0x00ff)|(val<<8));
    }
    public void setE(char val) {
        DE = ((DE&0xff00)|val);
    }
    public void setH(char val) {
        HL = ((HL&0x00ff)|(val<<8));
    }
    public void setL(char val) { HL = ((HL&0xff00)|val); }

    public void flagSet(char flag, int val) {
        flags &= ~(1<<flag);
        flags |= (val!=0?1:0)<<flag;
    }

    public int flagGet(char flag) {
        return (flags&(1<<flag))>>flag;
    }

    public char getById(int id) {
        char val = 0;
        switch(id) {
            case 0b000: val = getB(); break;
            case 0b001: val = getC(); break;
            case 0b010: val = getD(); break;
            case 0b011: val = getE(); break;
            case 0b100: val = getH(); break;
            case 0b101: val = getL(); break;
            case 0b111: val = Acc; break;
        }
        return val;
    }

    public void setById(int id, char val) {
        switch(id) {
            case 0b000: setB(val); break;
            case 0b001: setC(val); break;
            case 0b010: setD(val); break;
            case 0b011: setE(val); break;
            case 0b100: setH(val); break;
            case 0b101: setL(val); break;
            case 0b111: Acc = val; break;
        }
    }

    public String dump() {
        String s = "";
        s = s.concat(" F="+String.format("%02x", (int)flags));
        s = s.concat(" A="+String.format("%02x", (int)Acc));
        s = s.concat(" BC="+String.format("%04x", (int)BC));
        s = s.concat(" DE="+String.format("%04x", (int)DE));
        s = s.concat(" HL="+String.format("%04x", (int)HL));
        s = s.concat(" SP="+String.format("%04x", (int)SP));
        s = s.concat(" IP="+String.format("%04x", (int)IP));
        return s;
    }

}
