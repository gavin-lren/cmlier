package miniplc0java.symbol;

import java.util.HashMap;
import java.util.Map;


public class SymbolBlock {
    private HashMap<String, Symbol> symMap = new HashMap<>();
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

    public HashMap<String, Symbol> getSymbolBlock() {
        return this.symMap;
    }

    public void cat(SymbolBlock cat) {
        for (Map.Entry<String, Symbol> entry : cat.getSymbolBlock().entrySet())
            this.symMap.put(entry.getKey(), entry.getValue());
    }    
}
