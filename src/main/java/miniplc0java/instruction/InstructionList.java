package miniplc0java.instruction;

import java.util.ArrayList;



public class InstructionList {
    private ArrayList<Instruction> instructionList = new ArrayList<>();
    
    public int size() {
        return instructionList.size();
    }

    public ArrayList<Instruction> getInstructionList() {
        return instructionList;
    }
    public void add(Instruction instruction) {
        instructionList.add(instruction);
    }

    public void add(int level, Instruction instruction) {
        instructionList.add(level, instruction);
    }

    public void pop() {
        if (this.instructionList.isEmpty())
            return;
        this.instructionList.remove(this.instructionList.size() - 1);
    }

    public Instruction get(int index) {
        return this.instructionList.get(index);
    }
    
    public void print() {
        for (Instruction now : this.instructionList) {
            System.out.println(now.toString());
        }
        System.out.println("");
    }
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("");
        for(int i = 0; i<instructionList.size(); i++){
            s.append(instructionList.get(i) + "\n");
        }
        s.append("\n");
        return s.toString();
    }
}
