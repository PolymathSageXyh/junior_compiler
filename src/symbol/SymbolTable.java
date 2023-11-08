package symbol;
import paser.Mypair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class SymbolTable { //单例模式
    public Stack<HashSet<String>> symbolStack; //栈
    public HashMap<String, VarSymbol> globalVal; //全局变量
    public HashSet<Mypair<String, VarSymbol>> constVal; //常量
    public HashMap<String, String> constStr; //常串
    public HashMap<String, FuncSymbol> funcs;  //函数
    public HashMap<String, Stack<VarSymbol>> vars; //变量

    private final static SymbolTable instance = new SymbolTable();
    private SymbolTable() {
        funcs = new HashMap<>();
        vars = new HashMap<>();
        symbolStack = new Stack<>();
        globalVal = new HashMap<>();
        constVal = new HashSet<>();
        constStr = new HashMap<>();
    }

    public static SymbolTable getInstance(){
        return instance;
    }

    public void startScope() {
        symbolStack.push(new HashSet<>());
    }

    public void endScope() {
        HashSet<String> cur = symbolStack.peek();
        for(String str:cur) {
            vars.get(str).pop();
            if(vars.get(str).isEmpty()){
                vars.remove(str);
            }
        }
        symbolStack.pop();
    }

    public boolean tryAddFunc(ArrayList<Integer> params, String name, boolean isVoid) {
        if (funcs.containsKey(name)) {
            return false;
        }
        if (globalVal.containsKey(name)) {
            return false;
        }
        funcs.put(name, new FuncSymbol(isVoid, params, name));
        return true;
    }

    public FuncSymbol getFunc(String name) {
        return funcs.getOrDefault(name, null);
    }

    public VarSymbol getVar(String name) {
        if (vars.containsKey(name)) {
            return vars.get(name).peek();
        }
        return null;
    }

    public boolean addVar(boolean isConst, ArrayList<Integer> dim, String name) {
        HashSet<String> temp = symbolStack.peek();
        if (temp.contains(name)) return false;
        ArrayList<Integer> initials = new ArrayList<>();
        boolean isGlobal = symbolStack.size() == 1;
        if(dim.size() == 0) {
            initials.add(0);
        } else if (dim.size() == 1) {
            for (int i = 0; i < dim.get(0); i++)
                initials.add(0);
        } else {
            int len = dim.get(0) * dim.get(1);
            for (int i = 0; i < len; i++)
                initials.add(0);
        }
        VarSymbol varSymbol = new VarSymbol(isGlobal, isConst, dim, initials);
        temp.add(name);
        if (isGlobal) {
            globalVal.put(name,varSymbol);
        }
        if (!vars.containsKey(name)) {
            vars.put(name, new Stack<>());
        }
        vars.get(name).push(varSymbol);
        return true;
    }

    public boolean addVar(boolean isConst, ArrayList<Integer> dim, String name, ArrayList<Integer> inits) {
        HashSet<String> temp = symbolStack.peek();
        if (temp.contains(name)) return false;
        boolean isGlobal = symbolStack.size() == 1;
        VarSymbol varSymbol = new VarSymbol(isGlobal, isConst, dim, inits);
        temp.add(name);
        if (isGlobal) {
            globalVal.put(name,varSymbol);
        }
        if (!vars.containsKey(name)) {
            vars.put(name, new Stack<>());
        }
        vars.get(name).push(varSymbol);
        return true;
    }

    public boolean isGlobal() {
        return symbolStack.size() == 1;
    }

}
