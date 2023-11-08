package symbol;

import paser.nodes.InitValNode;

import java.util.ArrayList;

public class VarSymbol {      //常量，变量，形参
    private boolean isGlobal;
    private boolean isConst;
    private ArrayList<Integer> dim; //dim.size()为维度，保存数组长度
    private ArrayList<Integer> initials; //记录初值

    public VarSymbol(boolean isGlobal,boolean isConst,ArrayList<Integer> dim, ArrayList<Integer> initials) {
        this.isConst = isConst;
        this.isGlobal = isGlobal;
        this.dim = dim;
        this.initials = initials;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getArrayDim() {
        return dim.size();
    }

    public boolean isArray() {
        return dim.size() > 0;
    }

    public int getVVV() { return initials.get(0); }

    public int getVVV(ArrayList<Integer> offset) {
        if (offset.size() != dim.size()) System.out.println("dim not match!");
        int size = offset.size();
        if (size == 1) {
            return initials.get(offset.get(0));
        } else {
            return initials.get(offset.get(0) * dim.get(0) + offset.get(1));
        }
    }

}
