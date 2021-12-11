package org.hiranoaiku.mikrolab.model;

/**
 * Created by Hikaru on 28.02.2016.
 * Модель процессора КР580ИК80.
 */
public class Processor {

    private Memory memory;
    private Registers regs;
    private Ports ports;
    public boolean isRunning;
    private int ticks;
    private boolean halted;
    private boolean haltPending;
    private boolean interruptsEnabled;
    private boolean interruptRequest;
    private int interruptCommand;

    public Processor(Memory memory, Ports ports) {
        this.memory = memory;
        this.regs = new Registers();
        this.ports = ports;
        halted = false;
        haltPending = false;
        interruptsEnabled = false;
        interruptRequest = false;
        reset();
    }

    /**
     * Внешнее прерывание
     */
    public void interrupt(int icmd)
    {
        if(interruptsEnabled)
        {
            interruptRequest = true;
            interruptCommand = icmd;
        }
    }

    /**
     * Выполняет одну команду (и отсчитывает необходимое количество тактов)
     */
    public void step() {
        int t, cmd;
        if(ticks == 0 && !halted) {
            /* фетчинг инструкции */
            if(interruptRequest)
            {
                cmd = interruptCommand;
                interruptRequest = false;
            }
            else
            {
                cmd = memory.read(regs.IP++);
            }
            /* выполнение инструкции */
            switch(cmd) {

                case 0x00:          // NOP
                case 0x7F:          // MOV A, A
                case 0x40:          // MOV B, B
                case 0x49:          // MOV C, C
                case 0x52:          // MOV D, D
                case 0x5B:          // MOV E, E
                case 0x64:          // MOV H, H
                case 0x6D:          // MOV L, L
                    ticks = 4;
                    break;

                case 0x76:          // HLT
                    haltPending = true;
                    ticks = 5;
                    break;

                case 0x36:          // MVI M, [1 байт]
                    memory.write(regs.HL, memory.read(regs.IP));
                    regs.IP++; ticks = 10;
                    break;

                case 0x01:          // LXI BC, [2 байта]
                    regs.BC = memory.readWord(regs.IP);
                    regs.IP += 2; ticks = 10;
                    break;

                case 0x11:          // LXI DE, [2 байта]
                    regs.DE = memory.readWord(regs.IP);
                    regs.IP += 2; ticks = 10;
                    break;

                case 0x21:          // LXI HL, [2 байта]
                    regs.HL = memory.readWord(regs.IP);
                    regs.IP += 2; ticks = 10;
                    break;

                case 0x31:          // LXI SP, [2 байта]
                    regs.SP = memory.readWord(regs.IP);
                    regs.IP += 2; ticks = 10;
                    break;

                case 0x3E:          // MVI A, [1 байт]
                    regs.Acc = memory.read(regs.IP);
                    regs.IP++; ticks = 7;
                    break;

                case 0x06:          // MVI B, [1 байт]
                    regs.setB(memory.read(regs.IP));
                    regs.IP++; ticks = 7;
                    break;

                case 0x0E:          // MVI C, [1 байт]
                    regs.setC(memory.read(regs.IP));
                    regs.IP++; ticks = 7;
                    break;

                case 0x16:          // MVI D, [1 байт]
                    regs.setD(memory.read(regs.IP));
                    regs.IP++; ticks = 7;
                    break;

                case 0x1E:          // MVI E, [1 байт]
                    regs.setE(memory.read(regs.IP));
                    regs.IP++; ticks = 7;
                    break;

                case 0x26:          // MVI H, [1 байт]
                    regs.setH(memory.read(regs.IP));
                    regs.IP++; ticks = 7;
                    break;

                case 0x2E:          // MVI L, [1 байт]
                    regs.setL(memory.read(regs.IP));
                    regs.IP++; ticks = 7;
                    break;

                case 0x3A:          // LDA [2 байта]
                    regs.Acc = memory.read(memory.readWord(regs.IP));
                    regs.IP += 2; ticks = 13;
                    break;

                case 0x32:          // STA [2 байта]
                    memory.write(memory.readWord(regs.IP), regs.Acc);
                    regs.IP += 2; ticks = 13;
                    break;

                case 0x2A:          // LHLD [2 байта]
                    regs.HL = memory.readWord(memory.readWord(regs.IP));
                    regs.IP += 2; ticks = 16;
                    break;

                case 0x22:          // SHLD [2 байта]
                    memory.writeWord(memory.readWord(regs.IP), regs.HL);
                    regs.IP += 2; ticks = 16;
                    break;

                case 0x0A:          // LDAX BC
                    regs.Acc = memory.read(regs.BC);
                    ticks = 7;
                    break;

                case 0x1A:          // LDAX DE
                    regs.Acc = memory.read(regs.DE);
                    ticks = 7;
                    break;

                case 0x02:          // STAX BC
                    memory.write(regs.BC, regs.Acc);
                    ticks = 7;
                    break;

                case 0x12:          // STAX DE
                    memory.write(regs.DE, regs.Acc);
                    ticks = 7;
                    break;

                case 0xEB:          // XCHG
                    t = regs.HL;
                    regs.HL = regs.DE;
                    regs.DE = t;
                    ticks = 4;
                    break;

                case 0xE6:          // ANI [1 байт]
                    regs.Acc = (char) (regs.Acc & memory.read(regs.IP));
                    updateFlags(regs.Acc);
                    carryFlagsReset();
                    regs.IP++;
                    ticks = 7;
                    break;

                case 0xEE:          // XRI [1 байт]
                    regs.Acc = (char) ((regs.Acc ^ memory.read(regs.IP))&0xff);
                    updateFlags(regs.Acc);
                    carryFlagsReset();
                    regs.IP++;
                    ticks = 7;
                    break;

                case 0xC9:          // RET
                    regs.IP = memory.readWord(regs.SP);
                    regs.SP += 2;
                    ticks = 10;
                    break;

                case 0xC3:          // JMP [2 байта]
                    regs.IP = memory.readWord(regs.IP);
                    ticks = 10;
                    break;

                case 0xCD:          // CALL [2 байта]
                    regs.SP -= 2;
                    memory.writeWord(regs.SP, regs.IP+2);
                    regs.IP = memory.readWord(regs.IP);
                    ticks = 18;
                    break;

                case 0xD3:          // OUT [1 байт]
                    /* Предположим, что контроллер портов ввода-вывода настроен правильно */
                    ports.outPort(memory.read(regs.IP), regs.Acc);
                    regs.IP++;
                    ticks = 10;
                    break;

                case 0xDB:          // IN [1 байт]
                    /* Предположим, что контроллер портов ввода-вывода настроен правильно */
                    regs.Acc = ports.inPort(memory.read(regs.IP));
                    regs.IP++;
                    ticks = 10;
                    break;

                case 0xC5:          // PUSH BC
                    memory.writeWord(regs.SP-2, regs.BC);
                    regs.SP -= 2;
                    ticks = 12;
                    break;

                case 0xD5:          // PUSH DE
                    memory.writeWord(regs.SP-2, regs.DE);
                    regs.SP -= 2;
                    ticks = 12;
                    break;

                case 0xE5:          // PUSH HL
                    memory.writeWord(regs.SP-2, regs.HL);
                    regs.SP -= 2;
                    ticks = 12;
                    break;

                case 0xF5:          // PUSH PSW
                    memory.write(regs.SP - 1, regs.Acc);
                    memory.write(regs.SP - 2, regs.flags);
                    regs.SP -= 2;
                    ticks = 12;
                    break;

                case 0xC1:          // POP BC
                    regs.BC = memory.readWord(regs.SP);
                    regs.SP += 2;
                    ticks = 10;
                    break;

                case 0xD1:          // POP DE
                    regs.DE = memory.readWord(regs.SP);
                    regs.SP += 2;
                    ticks = 10;
                    break;

                case 0xE1:          // POP HL
                    regs.HL = memory.readWord(regs.SP);
                    regs.SP += 2;
                    ticks = 10;
                    break;

                case 0xF1:          // POP PSW
                    regs.flags = memory.read(regs.SP);
                    regs.Acc = memory.read(regs.SP + 1);
                    regs.SP += 2;
                    ticks = 10;
                    break;

                case 0x17:          // RAL
                    t = regs.flagGet(Registers.flagCY);
                    regs.flagSet(Registers.flagCY, regs.Acc & 0x80);
                    regs.Acc <<= 1;
                    regs.Acc |= t;
                    ticks = 4;
                    break;

                case 0x1F:          // RAR
                    t = regs.flagGet(Registers.flagCY);
                    regs.flagSet(Registers.flagCY, regs.Acc & 0x01);
                    regs.Acc >>= 1;
                    regs.Acc |= t<<7;
                    ticks = 4;
                    break;

                case 0x0F:          // RRC
                    regs.flagSet(Registers.flagCY, regs.Acc&0x01);
                    regs.Acc >>= 1;
                    ticks = 4;
                    break;

                case 0x3D:          // DCR r
                case 0x05:case 0x0D:
                case 0x15:case 0x1D:
                case 0x25:case 0x2D:
                    t = (cmd&0x38)>>3;
                    regs.setById(t, (char) ((regs.getById(t) - 1)&0xff));
                    if(regs.getById(t) == 0)
                        regs.flagSet(Registers.flagAC, 1);
                    updateFlags(regs.getById(t));
                    ticks = 4;
                    break;

                case 0x35:          // DCR M
                    memory.write(regs.HL, (char) (memory.read(regs.HL) - 1));
                    if(memory.read(regs.HL) == 0)
                        regs.flagSet(Registers.flagAC, 1);
                    updateFlags(memory.read(regs.HL));
                    ticks = 10;
                    break;


                case 0x3C:          // INR r
                case 0x04:case 0x0C:
                case 0x14:case 0x1C:
                case 0x24:case 0x2C:
                    t = regs.Acc;
                    regs.Acc = (char) ((regs.getById((cmd&0x38)>>3) + 1)&0xff);
                    updateFlags(regs.Acc);
                    regs.flagSet(Registers.flagAC, regs.Acc==0 ? 1 : 0);
                    regs.setById((cmd&0x38)>>3, regs.Acc);
                    regs.Acc = (char) (t&0xff);
                    ticks = 4;
                    break;

                case 0x34:          // INR M
                    memory.write(regs.HL, (char) (memory.read(regs.HL) - 1));
                    if(memory.read(regs.HL) == 0)
                        regs.flagSet(Registers.flagAC, 1);
                    updateFlags(regs.Acc);
                    ticks = 10;
                    break;

                case 0x03:          // INX BC
                    regs.BC++;
                    ticks = 6;
                    break;

                case 0x13:          // INX DE
                    regs.DE++;
                    ticks = 6;
                    break;

                case 0x23:          // INX HL
                    regs.HL++;
                    ticks = 6;
                    break;

                case 0x33:          // INX SP
                    regs.SP++;
                    ticks = 6;
                    break;

                case 0x0B:          // DCX BC
                    regs.BC--;
                    ticks = 6;
                    break;

                case 0x1B:          // DCX DE
                    regs.DE--;
                    ticks = 6;
                    break;

                case 0x2B:          // DCX HL
                    regs.HL--;
                    ticks = 6;
                    break;

                case 0x3B:          // DCX SP
                    regs.SP--;
                    ticks = 6;
                    break;

                case 0x87:          // ADD r
                case 0x80:case 0x81:
                case 0x82:case 0x83:
                case 0x84:case 0x85:
                    regs.Acc += regs.getById(cmd&0x7);
                    updateFlags(regs.Acc);
                    t = (regs.Acc+regs.getById(cmd&0x7)) > 255 ? 1 : 0;
                    regs.flagSet(Registers.flagCY, t);
                    regs.flagSet(Registers.flagAC, t);
                    ticks = 4;
                    break;

                case 0x86:          // ADD M
                    regs.Acc += memory.read(regs.HL);
                    updateFlags(regs.Acc);
                    t = regs.Acc+memory.read(regs.HL) > 255 ? 1 : 0;
                    regs.flagSet(Registers.flagCY, t);
                    regs.flagSet(Registers.flagAC, t);
                    ticks = 4;
                    break;

                case 0xC2:          // JNZ [2 байта]
                    if(regs.flagGet(Registers.flagZ) == 0) {
                        regs.IP = memory.readWord(regs.IP);
                    }
                    else regs.IP += 2;
                    ticks = 10;
                    break;

                case 0xCA:          // JZ [2 байта]
                    if(regs.flagGet(Registers.flagZ) != 0) {
                        regs.IP = memory.readWord(regs.IP);
                    }
                    else regs.IP += 2;
                    ticks = 10;
                    break;

                case 0xD2:          // JNC [2 байта]
                    if(regs.flagGet(Registers.flagCY) == 0) {
                        regs.IP = memory.readWord(regs.IP);
                    }
                    else regs.IP += 2;
                    ticks = 10;
                    break;

                case 0xDA:          // JC [2 байта]
                    if(regs.flagGet(Registers.flagCY) != 0) {
                        regs.IP = memory.readWord(regs.IP);
                    }
                    else regs.IP += 2;
                    ticks = 10;
                    break;

                case 0xBF:          // CMP r
                case 0xB8:case 0xB9:case 0xBA:
                case 0xBB:case 0xBC:case 0xBD:
                    t = regs.Acc;
                    regs.Acc -= regs.getById(cmd&0x7);
                    updateFlags(regs.Acc);
                    regs.flagSet(Registers.flagCY, regs.Acc < regs.getById(cmd & 0x7) ? 1 : 0);
                    regs.Acc = (char) (t&0xff);
                    ticks = 4;
                    break;

                case 0xBE:          // CMP M
                    t = regs.Acc;
                    regs.Acc -= memory.read(regs.HL);
                    updateFlags(regs.Acc);
                    regs.flagSet(Registers.flagCY, regs.Acc < memory.read(regs.HL) ? 1 : 0);
                    regs.Acc = (char) (t&0xff);
                    ticks = 7;
                    break;

                case 0x41:case 0x7d:          // MOV r1, r2
                case 0x42:case 0x43:case 0x44:case 0x45:case 0x47:case 0x48:case 0x4a:case 0x4b:
                case 0x4c:case 0x4d:case 0x4f:case 0x50:case 0x51:case 0x53:case 0x54:case 0x55:
                case 0x57:case 0x58:case 0x59:case 0x5a:case 0x5c:case 0x5d:case 0x5f:case 0x60:
                case 0x61:case 0x62:case 0x63:case 0x65:case 0x67:case 0x68:case 0x69:case 0x6a:
                case 0x6b:case 0x6c:case 0x6f:case 0x78:case 0x79:case 0x7a:case 0x7b:case 0x7c:
                    regs.setById((cmd&0x38)>>3, regs.getById(cmd&0x7));
                    ticks = 4;
                    break;

                case 0x7E:case 0x46:        // MOV r, M
                case 0x4E:case 0x56:case 0x5E:case 0x66:case 0x6E:
                    regs.setById((cmd&0x38)>>3, memory.read(regs.HL));
                    ticks = 7;
                    break;

                case 0x77:case 0x70:        // MOV M, r
                case 0x71:case 0x72:case 0x73:case 0x74:case 0x75:
                    memory.write(regs.HL, regs.getById(cmd&0x7));
                    ticks = 7;
                    break;

                case 0xA7:case 0xA0:        // ANA r
                case 0xA1:case 0xA2:case 0xA3:case 0xA4:case 0xA5:
                    regs.Acc &= regs.getById(cmd&0x7);
                    updateFlags(regs.Acc);
                    regs.flagSet(Registers.flagCY, 0);
                    regs.flagSet(Registers.flagAC, 1);
                    ticks = 4;
                    break;

                case 0xA6:                  // ANA M
                    regs.Acc &= memory.read(regs.HL);
                    updateFlags(regs.Acc);
                    carryFlagsReset();
                    ticks = 7;
                    break;

                case 0xAF:case 0xA8:        // XRA r
                case 0xA9:case 0xAA:case 0xAB:case 0xAC:case 0xAD:
                    regs.Acc ^= regs.getById(cmd&0x7);
                    updateFlags(regs.Acc);
                    carryFlagsReset();
                    ticks = 4;
                    break;

                case 0xAE:                  // XRA M
                    regs.Acc ^= memory.read(regs.HL);
                    updateFlags(regs.Acc);
                    carryFlagsReset();
                    ticks = 7;
                    break;

                case 0xB7:case 0xB0:        // ORA r
                case 0xB1:case 0xB2:case 0xB3:case 0xB4:case 0xB5:
                    regs.Acc |= regs.getById(cmd&0x7);
                    updateFlags(regs.Acc);
                    regs.flagSet(Registers.flagCY, 0);
                    regs.flagSet(Registers.flagAC, 1);
                    ticks = 4;
                    break;

                case 0xB6:                  // ORA M
                    regs.Acc |= memory.read(regs.HL);
                    updateFlags(regs.Acc);
                    carryFlagsReset();
                    ticks = 7;
                    break;

                case 0x2F:                  // CMA
                    regs.Acc = (char)~regs.Acc;
                    ticks = 4;
                    break;

                case 0xE9:                  // PCHL
                    regs.IP = regs.HL;
                    ticks = 6;
                    break;

                case 0x09:                  // DAD BC
                    regs.flagSet(Registers.flagCY, regs.HL+regs.BC > 0xFFFF ? 1 : 0);
                    regs.HL += regs.BC;
                    ticks = 10;
                    break;

                case 0x19:                  // DAD DE
                    regs.flagSet(Registers.flagCY, regs.HL+regs.DE > 0xFFFF ? 1 : 0);
                    regs.HL += regs.DE;
                    ticks = 10;
                    break;

                case 0x29:                  // DAD HL
                    regs.flagSet(Registers.flagCY, regs.HL+regs.HL > 0xFFFF ? 1 : 0);
                    regs.HL += regs.HL;
                    ticks = 10;
                    break;

                case 0x39:                  // DAD SP
                    regs.flagSet(Registers.flagCY, regs.HL+regs.SP > 0xFFFF ? 1 : 0);
                    regs.HL += regs.SP;
                    ticks = 10;
                    break;

                case 0xFB:                  // EI
                    interruptsEnabled = true;
                    ticks = 4;
                    break;

                case 0xF3:                  // DI
                    interruptsEnabled = false;
                    ticks = 4;
                    break;

                case 0xE3:                  // XTHL
                    t = regs.HL;
                    regs.HL = regs.SP;
                    regs.SP = t;
                    ticks = 16;
                    break;

                case 0xFE:                  // CPI [1 байт]
                    t = regs.Acc;
                    regs.Acc -= memory.read(regs.IP);
                    updateFlags(regs.Acc);
                    regs.flagSet(Registers.flagCY, regs.Acc < memory.read(regs.IP) ? 1 : 0);
                    regs.Acc = (char)(t&0xff);
                    regs.IP++;
                    ticks = 7;
                    break;

                case 0xC7:case 0xCF:        // RST n
                case 0xD7:case 0xDF:
                case 0xE7:case 0xEF:
                case 0xF7:case 0xFF:
                    regs.SP -= 2;
                    memory.writeWord(regs.SP, regs.IP+2);
                    regs.IP = cmd & 0x38; // определение вектора прерывания по его номеру
                    ticks = 12;
                    break;
            }

            //System.out.println(regs.dump());

        } else {
            /* счет отдельного такта, для более реалистичной модели (на самом деле, это фейл) */
            ticks--;

            if(ticks == 0 && haltPending) {
                haltPending = false;
                halted = true;
            }
        }
    }

    private void updateFlags(char val) {
        regs.flags = (char) (((val==0 ? 1 : 0) << Registers.flagZ)
                | ((~val & 0x01) << Registers.flagP)
                | (val&0x80));
    }

    private void carryFlagsReset() {
        regs.flagSet(Registers.flagCY, 0);
        regs.flagSet(Registers.flagAC, 0);
    }

    /**
     * Асинхронный сброс машины
     */
    public void reset() {
        ticks = 0;
        haltPending = false;
        interruptsEnabled = false;
        regs.reset();
        ports.reset();
        //memory.clearRAM();
    }

}
