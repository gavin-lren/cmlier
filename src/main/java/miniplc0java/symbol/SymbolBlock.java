package miniplc0java.symbol;

import java.util.LinkedHashMap;
import java.util.Map;


public class SymbolBlock {
    /** 使用LinkedHashMap进行排序 */
    private LinkedHashMap<String, Symbol> symMap = new LinkedHashMap<>();
    /** 符号表block的层级和上一级 */
    private int level;
    private int uplevel;

    public SymbolBlock(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setUplevel(int uplevel) {
        this.uplevel = uplevel;
    }

    public int getUplevel() {
        return uplevel;
    }

    public int getSize() {
        return symMap.size();
    }

    public void clear() {
        this.symMap.clear();
    }

    public Symbol getValue(String key) {
        return symMap.get(key);
    }

    public void putValue(String key, Symbol symbol) {
        symMap.put(key, symbol);
    }

    public LinkedHashMap<String, Symbol> getSymbolBlock() {
        return this.symMap;
    }

    public void cat(SymbolBlock cat) {
        for (Map.Entry<String, Symbol> entry : cat.getSymbolBlock().entrySet())
            this.symMap.put(entry.getKey(), entry.getValue());
    }
}
