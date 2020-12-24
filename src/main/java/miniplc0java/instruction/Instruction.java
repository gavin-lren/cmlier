package miniplc0java.instruction;

public class Instruction {
    private Operation opt;
    private int num_32;
    private long num_64;
    private double num_d;

    private boolean is_n;
    private boolean is_u;
    private boolean is_d;
    private boolean isreloc;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.is_n = false;
        this.isreloc = false;
    }

    public Instruction(Operation opt, boolean is_32, long num_64) {
        this.opt = opt;
        this.is_n = true;
        //this.is_u = false;
        this.is_d = false;
        if (is_32) {
            this.num_32 = (int) num_64;
        } else {
            this.num_64 = num_64;
        }
    }

    public Instruction(Operation opt, double num_d) {
        this.opt = opt;
        this.is_n = true;
        this.is_u = false;
        this.is_d = true;
        this.num_d = num_d;
    }
    public Operation getOpt() {
        return opt;
    }

    public boolean isis_n() {
        return is_n;
    }

    public boolean isIs_u() {
        return is_u;
    }

    public boolean isIs_d() {
        return is_d;
    }
    
    public int getNum_32() {
        return num_32;
    }

    public void setNum_32(int num_32) {
        this.num_32 = num_32;
    }
    
    public long getNum_64() {
        return num_64;
    }

    public void setNum_64(long num_64) {
        this.num_64 = num_64;
    }
    
    public double getNum_d() {
        return num_d;
    }
    
    public void setNum_d(double num_d) {
        this.num_d = num_d;
    }

    public boolean isIsreloc() {
        return isreloc;
    }

    public void setIsreloc(boolean isreloc) {
        this.isreloc = isreloc;
    }

    @Override
    public String toString() {
        switch (this.opt) {
            case NOP:
                return "nop";
            case PUSH: {
                if (is_n) {
                    if (is_d) {
                        return "push " + num_d;
                    } else if (is_u) {
                        return "push " + num_32;
                    } else {
                        return "push " + num_64;
                    }
                }
                return "push";   
            }
            case POP:
                return "pop";
            case POPN:
                if (is_n)
                    return "popN " + num_32;
                return "popn";
            case LOCA:
                if (is_n)
                    return "locA " + num_32;
                return "loca";
            case ARGA:
                if (is_n)
                    return "argA " + num_32;
                return "arga";
            case GLOBA:
                if (is_n)
                    return "globA " + num_32;
                return "globa";
            /*
            case LOAD_8:
                return "load8";
            case LOAD_16:
                return "load16";
            case LOAD_32:
                return "load32";
                */    
            case LOAD_64:
                return "load64";
            /*
            case STORE_8:
                return "store.8";
            case STORE_16:
                return "srore.16";
            case STORE_32:
                return "store.32";
                */
            case STORE_64:
                return "store64";
            /*
            case ALLOC:
                return "alloc";
            case FREE:
                return "free";
                */
            case STACKALLOC:
                if (is_n)
                    return "stackAlloc " + num_32;
                return "stackalloc";
            case ADD_I:
                return "addI";
            case SUB_I:
                return "subI";
            case MUL_I:
                return "mulI";
            case DIV_I:
                return "divI";
            case ADD_F:
                return "addF";
            case SUB_F:
                return "subF";
            case MUL_F:
                return "mulF";
            case DIV_F:
                return "diF";
            case DIV_U:
                return "divU";
            case AND:
                return "and";
            case SHL:
                return "shl";
            case SHR:
                return "shr";
            case OR:
                return "or";
            case XOR:
                return "xor";
            case NOT:
                return "not";
            case CMP_I:
                return "cmpI";
            case CMP_U:
                return "cmpU";
            case CMP_F:
                return "cmpF";
            case NEG_I:
                return "negI";
            case NEG_F:
                return "negF";
            case ITOF:
                return "itof";
            case FTOI:
                return "ftoi";
            case SHRL:
                return "shrl";
            case SET_LT:
                return "setLt";
            case SET_GT:
                return "setGt";
            case BR:
                if (is_n)
                    return "br " + num_32;
                return "br";
            /*case BR_FALSE:
                return "br.false";
                */
            case BR_TRUE:
                if (is_n)
                    return "brTrue " + num_32;
                return "brTrue";
            case CALL:
                if (is_n)
                    return "call " + num_32;
                return "call";
            case RET:
                return "ret";
            case CALLNAME:
                if (is_n)
                    return "callName " + num_32;
                return "callname";
            /*
            case SCAN_I:
                return "scan.i";
            case SCAN_C:
                return "scan.c";
            case SCAN_F:
                return "scan.f";
            case PRINT_I:
                return "print.i";
            case PRINT_C:
                return "print.c";
            case PRINT_F:
                return "print.f";
            case PRINT_S:
                return "print.s";
            case PRINTLN:
                return "println";
                */
            default:
                return "panic";
        }
    }
}
