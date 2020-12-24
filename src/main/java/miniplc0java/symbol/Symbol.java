package miniplc0java.symbol;

import miniplc0java.instruction.InstructionList;

public class Symbol {
    /**
     * true is fun/false is var
     */
    private boolean isFunOrVar;
    private String type;
    private int global;
    private int local;
    private int param;

    public boolean isFunOrVar() {
        return isFunOrVar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getGlobal() {
        return global;
    }

    public void setGlobal(int global) {
        this.global = global;
    }

    public int getLocal() {
        return local;
    }

    public void setLocal(int local) {
        this.local = local;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    //--Fun--
    private int slotparam;
    private int slotloc;
    private int slotret;
    public InstructionList inList;

    public int getSlotparam() {
        return slotparam;
    }

    public void setSlotparam(int slotparam) {
        this.slotparam = slotparam;
    }

    public int getSlotloc() {
        return slotloc;
    }

    public void setSlotloc(int slotloc) {
        this.slotloc = slotloc;
    }

    public int getSlotret() {
        return slotret;
    }

    public void setSlotret(int slotret) {
        this.slotret = slotret;
    }

    public InstructionList getInList() {
        return inList;
    }

    public void setInList(InstructionList inList) {
        this.inList = inList;
    }
    

    //--Var--
    private boolean isConstant;
    private boolean isGlobal;
    private boolean isInitialized;
    private boolean isParam;

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public boolean isParam() {
        return isParam;
    }
    
    public void setParam(boolean isParam) {
        this.isParam = isParam;
    }
    

    //
    public Symbol(boolean isFunOrVar, int global, int local, int param) {
        this.isFunOrVar = isFunOrVar;
        this.global = global;
        this.local = local;
        this.param = param;
        if (isFunOrVar)
            this.inList = new InstructionList();
        else
            this.isConstant = this.isGlobal = this.isInitialized = this.isParam = false;
    }
    
    @Override
    public String toString() {
        if (this.isFunOrVar == true) {
            return "index_global: " + getLocal() + " index_local: " + getGlobal() + " index_prarm: " + getParam()
                    + " rettype:" + type + "\n" + inList.toString();
        }
        return "is Constant: " + isConstant + " is Global:" + isGlobal + 
        " is Param:" + isParam +
        " datatype:" + type + "\n";
    }
}

    