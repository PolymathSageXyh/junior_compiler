package mips;
import lightllr.Argument;
import lightllr.Function;
import lightllr.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MipsBuilder {
    private Function curFunction;
    private int curStackOffset;
    private HashMap<Value, Integer> stackOffsetMap;
    private HashMap<Value, Register> var2reg;
    private AssemblyTable assemblyTable;

    private MipsBuilder() {
        this.curFunction = null;
        this.assemblyTable = new AssemblyTable();
    }


    public void enterFunc(Function newFunction) {
        this.curFunction = newFunction;
        this.curStackOffset = 0;
        this.stackOffsetMap = new HashMap<>();
        this.var2reg = newFunction.getVar2reg();
    }

    public Register getRegOf(Value value) {
        if (var2reg == null) return null;
        return var2reg.get(value);
    }

    public void allocRegForParam(Argument param, Register reg) {
        if (var2reg == null) return;
        var2reg.put(param, reg);
    }

    public boolean useReg() {
        return var2reg != null;
    }

    public ArrayList<Register> getAllocatedRegs() {
        if (var2reg == null) return new ArrayList<>();
        return new ArrayList<>(new HashSet<>(var2reg.values()));
    }

    public void addValueOffsetMap(Value value, int offset) {
        stackOffsetMap.put(value, offset);
    }

    public Integer getOffsetOf(Value value) {
        return stackOffsetMap.get(value);
    }

    public int getCurOffset() {
        return curStackOffset;
    }

    public void subCurOffset(int offset) {
        curStackOffset -= offset;
        assert curStackOffset >= 0;
    }

}
