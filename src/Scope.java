import lightllr.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Scope {
    private ArrayList<HashMap<String, Value>> inner;

    public Scope() {
        inner = new ArrayList<>();
    }

    public void enter() {
        inner.add(new HashMap<>());
    }

    public void exit() {
        inner.remove(inner.size() - 1);
    }

    public boolean inGlobal() {
        return inner.size() == 1;
    }

    public boolean push(String name, Value val) {
        if (inner.get(inner.size() - 1).containsKey(name)) return false;
        inner.get(inner.size() - 1).put(name, val);
        return true;
    }

    public Value find(String name) {
        int len = inner.size();
        for (int i = len - 1; i >= 0; i--) {
            if (inner.get(i).containsKey(name))
                return inner.get(i).get(name);
        }
        return null;
    }

}
