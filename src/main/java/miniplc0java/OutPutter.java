package miniplc0java;

import miniplc0java.analyser.*;
import miniplc0java.instruction.*;
import miniplc0java.symbol.*;



import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

public class OutPutter {
    private List<Byte> outList = new ArrayList<>();
    private Analyser analyser;

    public List<Byte> getBinaryList() {
        /* magic、version */
        outList.addAll(byteTrans(4, 0x72303b3e));
        outList.addAll(byteTrans(4, 1));
        int preGlobalSymbolTableSize = analyser.symbolList.getGloba().getSize();
        int preFunctionSymbolTableSize = analyser.symbolList.getFun().getSize();
        SymbolBlock globalTable = analyser.symbolList.getGloba();
        globalTable.cat(analyser.symbolList.getFun());
        globalTable.cat(analyser.symbolList.getString());

        // count
        outList.addAll(byteTrans(4, globalTable.getSize()));
        int i = 0;
        for (Map.Entry<String, Symbol> entry : globalTable.getSymbolBlock().entrySet()) {
            String name = entry.getKey();
            Symbol sym = entry.getValue();

            // isConst
            if (i >= preGlobalSymbolTableSize || sym.isConstant())
                outList.addAll(byteTrans(1, 1));
            else
                outList.addAll(byteTrans(1, 0));

            if (i >= preGlobalSymbolTableSize) {
                outList.addAll(byteTrans(4, strLength(name)));
                outList.addAll(byteTrans(name));
            } else {
                outList.addAll(byteTrans(4, 8));
                outList.addAll(byteTrans(8, 0));
            }
            i++;
        }

        /** function */

        SymbolBlock functionTable = analyser.symbolList.getFun();
        // count
        outList.addAll(byteTrans(4, functionTable.getSize()));
        i = 0;
        for (Map.Entry<String, Symbol> entry : functionTable.getSymbolBlock().entrySet()) {

            Symbol fn = entry.getValue();

            outList.addAll(byteTrans(4, fn.getGlobal()));
            outList.addAll(byteTrans(4, fn.getSlotret()));
            outList.addAll(byteTrans(4, fn.getSlotparam()));
            outList.addAll(byteTrans(4, fn.getSlotloc()));
            outList.addAll(byteTrans(4, fn.getInList().size()));

            for (Instruction ins : fn.getInList().getInstructionList()) {
                // instruction
                outList.addAll(byteInstruction(ins.getOpt()));
                if (ins.isIsreloc()) {
                    if (ins.getOpt() == Operation.CALLNAME) {
                        outList.addAll(byteTrans(4, ins.getNum_32() + preGlobalSymbolTableSize));
                    } else if (ins.getOpt() == Operation.PUSH) {
                        outList.addAll(
                                byteTrans(8, ins.getNum_32() + preGlobalSymbolTableSize + preFunctionSymbolTableSize));
                    }
                }

                else if (ins.isis_n()) {
                    if (ins.getOpt() == Operation.PUSH && ins.isIs_d()==false) {
                        outList.addAll(byteTrans(8, ins.getNum_64()));
                    } else if (ins.isIs_d())
                        outList.addAll(byteTrans(8, ins.getNum_d()));
                    else
                        outList.addAll(byteTrans(4, ins.getNum_32()));
                }

            }
        }
        return outList;
    }

    public OutPutter(Analyser analyser) {
        this.analyser = analyser;
    }

    public List<Byte> getByteList() {
        return outList;
    }

    public void print(PrintStream output) throws IOException {
        int a[] = new int[8];
        byte[] bytes = new byte[outList.size()];
        for (int k = 0; k < outList.size(); k++) {
            for (int i = 7; i >= 0; i--) {
                a[i] = (outList.get(k) >> i) & 0x0001;
            }
            bytes[k] = outList.get(k);
            //System.out.print("" + a[7] + a[6] + " " + a[5] + a[4] + " " + a[3] + a[2] + " " + a[1] + a[0] + "(" + byteList.get(k) + ")" + "\n");
        }
        output.write(bytes);
    }


    public List<Byte> byteTrans(int length, long num){
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8*(length-1);
        for(int i = 0; i < length; i++){
            bytes.add((byte)((num >> (start - i*8)) & 0xFF));
        }

        return bytes;
    }

    public List<Byte> byteTrans(int length, double num){
        long val = Double.doubleToRawLongBits(num);
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8*(length-1);
        for(int i = 0; i < length; i++){
            bytes.add((byte)((val >> (start - i*8)) & 0xFF));
        }

        return bytes;
    }

    public List<Byte> byteTrans(int length, int num){
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8*(length-1);
        for(int i = 0; i < length; i++){
            bytes.add((byte)((num >> (start - i*8)) & 0xFF));
        }

        return bytes;
    }

    public int strLength(String str) {
        int res = 0;

        for (int i = 0; i < str.length(); i ++) {
            char ch = str.charAt(i);

            if (ch == '\\' && i != str.length() - 1) {
                if (str.charAt(i + 1) == 'n' || str.charAt(i + 1) == 'r'
                        || str.charAt(i + 1) == 't' || str.charAt(i + 1) == '\''
                        || str.charAt(i + 1) == '\"' || str.charAt(i + 1) == '\\') {
                    i++;
                }
            }

            res ++;
        }

        return res;
    }

    public List<Byte> byteTrans(String str) {
        List<Byte> str_array = new ArrayList<>();

        for (int i = 0; i < str.length(); i ++){
            char ch = str.charAt(i);

            if (ch == '\\'){
                if (i != str.length() - 1){
                    if (str.charAt(i + 1) == 'n'){
                        str_array.add((byte) (0x0a));
                    }
                    else if (str.charAt(i + 1) == 'r') {
                        str_array.add((byte) (0x0d));
                    }
                    else if (str.charAt(i + 1) == 't') {
                        str_array.add((byte) (0x09));
                    }
                    else if (str.charAt(i + 1) == '\'') {
                        str_array.add((byte) (0x27));
                    }
                    else if (str.charAt(i + 1) == '\"') {
                        str_array.add((byte) (0x22));
                    }
                    else if (str.charAt(i + 1) == '\\') {
                        str_array.add((byte) (0x5c));
                    }

                    i ++;
                }
                else str_array.add((byte)(ch & 0xFF));
            }
            else str_array.add((byte)(ch & 0xFF));
        }

        return str_array;
    }

    public List<Byte> byteInstruction(Operation opt) {
        if(opt == Operation.NOP)
            return byteTrans(1, 0x00);
        else if(opt == Operation.PUSH)
            return byteTrans(1,0x01);
        else if(opt == Operation.POP)
            return byteTrans(1, 0x02);
        else if(opt == Operation.POPN)
            return byteTrans(1, 0x03);
        else if(opt == Operation.DUP)
            return byteTrans(1, 0x04);
        else if(opt == Operation.LOCA)
            return byteTrans(1, 0x0a);
        else if(opt == Operation.ARGA)
            return byteTrans(1, 0x0b);
        else if(opt == Operation.GLOBA)
            return byteTrans(1, 0x0c);
        else if(opt == Operation.LOAD_8)
            return byteTrans(1, 0x10);
        else if(opt == Operation.LOAD_16)
            return byteTrans(1, 0x11);
        else if(opt == Operation.LOAD_32)
            return byteTrans(1, 0x12);
        else if(opt == Operation.LOAD_64)
            return byteTrans(1, 0x13);
        else if(opt == Operation.STORE_8)
            return byteTrans(1, 0x14);
        else if(opt == Operation.STORE_16)
            return byteTrans(1, 0x15);
        else if(opt == Operation.STORE_32)
            return byteTrans(1, 0x16);
        else if(opt == Operation.STORE_64)
            return byteTrans(1, 0x17);
        else if(opt == Operation.ALLOC)
            return byteTrans(1, 0x18);
        else if(opt == Operation.FREE)
            return byteTrans(1, 0x19);
        else if(opt == Operation.STACKALLOC)
            return byteTrans(1, 0x1a);
        else if(opt == Operation.ADD_I)
            return byteTrans(1, 0x20);
        else if(opt == Operation.SUB_I)
            return byteTrans(1, 0x21);
        else if(opt == Operation.MUL_I)
            return byteTrans(1, 0x22);
        else if(opt == Operation.DIV_I)
            return byteTrans(1, 0x23);
        else if(opt == Operation.ADD_F)
            return byteTrans(1, 0x24);
        else if(opt == Operation.SUB_F)
            return byteTrans(1, 0x25);
        else if(opt == Operation.MUL_F)
            return byteTrans(1, 0x26);
        else if(opt == Operation.DIV_F)
            return byteTrans(1, 0x27);
        else if(opt == Operation.DIV_U)
            return byteTrans(1, 0x28);
        else if(opt == Operation.SHL)
            return byteTrans(1, 0x29);
        else if(opt == Operation.SHR)
            return byteTrans(1, 0x2a);
        else if(opt == Operation.AND)
            return byteTrans(1, 0x2b);
        else if(opt == Operation.OR)
            return byteTrans(1, 0x2c);
        else if(opt == Operation.XOR)
            return byteTrans(1, 0x2d);
        else if(opt == Operation.NOT)
            return byteTrans(1, 0x2e);
        else if(opt == Operation.CMP_I)
            return byteTrans(1, 0x30);
        else if(opt == Operation.CMP_U)
            return byteTrans(1, 0x31);
        else if(opt == Operation.CMP_F)
            return byteTrans(1, 0x32);
        else if(opt == Operation.NEG_I)
            return byteTrans(1, 0x34);
        else if(opt == Operation.NEG_F)
            return byteTrans(1, 0x35);
        else if(opt == Operation.ITOF)
            return byteTrans(1, 0x36);
        else if(opt == Operation.FTOI)
            return byteTrans(1, 0x37);
        else if(opt == Operation.SHRL)
            return byteTrans(1, 0x38);
        else if(opt == Operation.SET_LT)
            return byteTrans(1, 0x39);
        else if(opt == Operation.SET_GT)
            return byteTrans(1, 0x3a);
        else if(opt == Operation.BR)
            return byteTrans(1, 0x41);
        else if(opt == Operation.BR_FALSE)
            return byteTrans(1, 0x42);
        else if(opt == Operation.BR_TRUE)
            return byteTrans(1, 0x43);
        else if(opt == Operation.CALL)
            return byteTrans(1, 0x48);
        else if(opt == Operation.RET)
            return byteTrans(1, 0x49);
        else if(opt == Operation.CALLNAME)
            return byteTrans(1, 0x4a);
        else if(opt == Operation.SCAN_I)
            return byteTrans(1, 0x50);
        else if(opt == Operation.SCAN_C)
            return byteTrans(1, 0x51);
        else if(opt == Operation.SCAN_F)
            return byteTrans(1, 0x52);
        else if(opt == Operation.PRINT_I)
            return byteTrans(1, 0x54);
        else if(opt == Operation.PRINT_C)
            return byteTrans(1, 0x55);
        else if(opt == Operation.PRINT_F)
            return byteTrans(1, 0x56);
        else if(opt == Operation.PRINT_S)
            return byteTrans(1, 0x57);
        else if(opt == Operation.PRINTLN)
            return byteTrans(1, 0x58);
        else if(opt == Operation.PANIC)
            return byteTrans(1, 0xfe);
        return null;
    }
}
