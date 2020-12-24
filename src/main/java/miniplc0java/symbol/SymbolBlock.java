package miniplc0java.symbol;

import java.util.LinkedHashMap;
import java.util.Map;


public class SymbolBlock {
    private LinkedHashMap<String, Symbol> symMap = new LinkedHashMap<>();
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
    
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder("");
        s.append("father: " + uplevel + "\n");
        if(symMap.isEmpty()){
            s.append("the table is empty\n");
        }
        else
            for (Map.Entry<String, Symbol> entry: symMap.entrySet())
                s.append("name: " + entry.getKey() + ", " + entry.getValue().toString());
        
                return s.toString() + "\n";
    }
}
