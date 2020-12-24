package miniplc0java.symbol;

import java.util.ArrayList;

public class SymbolList {
    private ArrayList<SymbolBlock> symbolBlockList = new ArrayList<>();
    /**
     * 获取当前符号表的block层数
     * @return
     */
    public int getSize() {
        return symbolBlockList.size();
    }
    
    /**
     * 全局变量的block为0，上层为-1
     */
    public SymbolList() {
        SymbolBlock funSymbolBlock = new SymbolBlock(-1);
        funSymbolBlock.setUplevel(-1);

        SymbolBlock stringSymbolBlock = new SymbolBlock(-1);
        stringSymbolBlock.setUplevel(-1);

        SymbolBlock globalSymbolBlock = new SymbolBlock(0);
        globalSymbolBlock.setUplevel(1);
        
        

        Symbol start = new Symbol(true, 0, -1, -1);
        start.setType("void");
        start.setSlotloc(0);
        start.setSlotparam(0);
        start.setSlotret(0);
        funSymbolBlock.putValue("_start", start);
        symbolBlockList.add(funSymbolBlock);
        symbolBlockList.add(stringSymbolBlock);
        symbolBlockList.add(globalSymbolBlock);
        
    }
    /**
     * 获取函数block
     * @return
     */
    public SymbolBlock getFun() {
        return symbolBlockList.get(0);
    }
    /**
     * 获取字符串block
     * @return
     */
    public SymbolBlock getString() {
        return symbolBlockList.get(1);
    }
    /**
     * 获取全局变量block
     * @return
     */
    public SymbolBlock getGloba() {
        return symbolBlockList.get(2);
    }
    /**
     * 获取当前局部变量block
     * @return
     */
    public SymbolBlock getNow() {
        return symbolBlockList.get(this.getSize() - 1);
    }

    /**
     * 获取函数局部变量block
     * @param fun
     * @return
     */
    public SymbolBlock getFunLoc(Symbol fun) {
        if (fun == null)
            return null;
        return symbolBlockList.get(fun.getLocal());
    }

    /**
     * 新增局部变量block
     * @param level
     */
    public void addSymbolBlock(int level) {
        SymbolBlock now = new SymbolBlock(level);
        SymbolBlock up = symbolBlockList.get(symbolBlockList.size() - 1);
        
        //设置上一层block的值
        if (now.getLevel() == up.getLevel()) {
            now.setUplevel(up.getUplevel());
        } else {
            now.setUplevel(symbolBlockList.size() - 1);
        }
        symbolBlockList.add(now);
    }

    /**
     * 删除局部变量block
     */
    public void removeSymbolBlock() {
        symbolBlockList.remove(symbolBlockList.size() - 1);
    }

    /**
     * 在upblock中查找符号
     * @param name
     * @return
     */
    public Symbol find(String name) {
        Symbol out = null;
        SymbolBlock loc = getNow();
        do{
            out = loc.getValue(name);
            if (loc.getUplevel() == -1 || out == null) {
                break;
            }
            loc = symbolBlockList.get(loc.getUplevel());
        } while (out == null);
        return out;
    }
    /**
     * 在当前局部变量block里查找符号
     * @param key
     * @return
     */
    public Symbol findDupSymbol(String key) {
        SymbolBlock now = getNow();
        return now.getValue(key);
    }
    /**
     * 查找当前局部变量block里的函数
     * @param key
     * @return
     */
    public Symbol findFun(String key) {
        SymbolBlock now = getFun();
        Symbol fun = now.getValue(key);
        if (fun == null)
            return null;
        else
            return fun;
    }
    
    public void print() {
        for (int i = 0; i < symbolBlockList.size(); i++) {
            String id;
            if (i == 0) {
                id = "Function Block";
            } else if (i == 1) {
                id = "String Block";
            } else if (i == 2) {
                id = "Glova Block";
            } else {
                id = String.valueOf(i);
            }
            System.out.println(id+"->"+symbolBlockList.get(i));
        }
    }
}