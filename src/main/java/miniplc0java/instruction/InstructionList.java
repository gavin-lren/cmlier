package miniplc0java.instruction;

import java.util.ArrayList;

public class InstructionList {
    private ArrayList<Instruction> instructionList = new ArrayList<>();
    /**
     * 获取指令集
     * @return
     */
    public ArrayList<Instruction> getInstructionList() {
        return instructionList;
    }
    /**
     * 获取指令集大小
     * @return
     */
    public int size() {
        return instructionList.size();
    }
    /**
     * 添加新的指令
     * @param instruction
     */
    public void add(Instruction instruction) {
        instructionList.add(instruction);
    }
    /**
     * 添加新的指令
     * @param level
     * @param instruction
     */
    public void add(int level, Instruction instruction) {
        instructionList.add(level, instruction);
    }

    /**
     * 弹出指令
     */
    public void pop() {
        if (this.instructionList.isEmpty())
            return;
        this.instructionList.remove(this.instructionList.size() - 1);
    }
    /**
     * 获取第index个指令
     * @param index
     * @return
     */
    public Instruction get(int index) {
        return this.instructionList.get(index);
    }
}
