package mips;

import lightllr.*;
import lightllr.Module;
import lightllr.optimization.Pass;

import java.util.*;

public class RegAllocator extends Pass {
    private HashMap<Register, Value> reg2var;
    private HashMap<Value, Register> var2reg;
    private ArrayList<Register> regSet;

    public RegAllocator(Module module) {
        super(module);
        this.regSet = new ArrayList<>();
        for (Register value : Register.values()) {
            if (value.ordinal() >= Register.T0.ordinal() && value.ordinal() <= Register.T9.ordinal()) {
                regSet.add(value);
            }
        }
    }

    @Override
    public void run() {
        for (Function function : module.getFunctions()) {
            if (function.getBasicBlocks().size() > 0) {
                BasicBlock entry = function.getEntryBB();
                init();
                allocaForBB(entry);
                function.setVar2reg(var2reg);
                // 打印调试
//                System.out.println("\n" + function.getName());
//                ArrayList<Value> varList = new ArrayList<>(var2reg.keySet());
//                Collections.sort(varList, Comparator.comparing(Value::getName));
//                for (Value var : varList) {
//                    Register reg = var2reg.get(var);
//                    System.out.println(var.getName() + " ==> " + reg);
//                }
            }
        }
    }

    private void init() {
        reg2var = new HashMap<>();
        var2reg = new HashMap<>();
    }

    private void allocaForBB(BasicBlock entry) {
        ArrayList<Instruction> instrList = entry.getInstrList();
        HashSet<Value> defined = new HashSet<>();
        HashSet<Value> neverUsedAfter = new HashSet<>();
        HashMap<Value, Value> lastUse = new HashMap<>();

        for (Instruction instr : instrList) {
            ArrayList<Value> operands = instr.getOperands();
            if (instr instanceof BinaryInstr || instr instanceof CmpInstr
                    || instr instanceof LoadInstr || instr instanceof StoreInstr
                    || instr instanceof GetElementPtrInstr || instr instanceof ZextInstr) {
                for (Value operand : operands) {
                    if (!(operand instanceof ConstantInt))
                        lastUse.put(operand, instr);
                }
            } else if (instr instanceof MoveInstr) {
                if (!(instr.getOperand(0) instanceof ConstantInt)) {
                    lastUse.put(instr.getOperand(0), instr);
                }
            } else if (instr instanceof PhiInstr) {
                for (int i = 0; i < instr.getNumOps() / 2; i++) {
                    if (!(instr.getOperand(2*i) instanceof ConstantInt)) {
                        lastUse.put(instr.getOperand(2*i), instr);
                    }
                }
            }
            else if (instr instanceof CallInstr) {
                for (int i = 1; i < instr.getNumOps(); i++) {
                    if (!(instr.getOperand(i) instanceof ConstantInt)) {
                        lastUse.put(instr.getOperand(i), instr);
                    }
//                    if (instr.getOperand(i) instanceof GlobalVariable && !var2reg.containsKey(instr.getOperand(i))) {
//                        Register reg = allocRegFor();
//                        if (reg != null) {
//                            if (reg2var.containsKey(reg)) {
//                                var2reg.remove(reg2var.get(reg));
//                            }
//                            reg2var.put(reg, instr.getOperand(i));
//                            var2reg.put(instr.getOperand(i), reg);
//                        }
//                    }
                }
            } else if (instr instanceof BranchInstr && ((BranchInstr) instr).is_cond_br()) {
                if (!(instr.getOperand(0) instanceof ConstantInt)) {
                    lastUse.put(instr.getOperand(0), instr);
                }
            } else if (instr instanceof ReturnInstr && instr.getNumOps() > 0) {
                if (!(instr.getOperand(0) instanceof ConstantInt)) {
                    lastUse.put(instr.getOperand(0), instr);
                }
//                if (instr.getOperand(0) instanceof GlobalVariable && !var2reg.containsKey(instr.getOperand(0))) {
//                    Register reg = allocRegFor();
//                    if (reg != null) {
//                        if (reg2var.containsKey(reg)) {
//                            var2reg.remove(reg2var.get(reg));
//                        }
//                        reg2var.put(reg, instr.getOperand(0));
//                        var2reg.put(instr.getOperand(0), reg);
//                    }
//                }
            }

        }

        for (Instruction instr : instrList) {
            ArrayList<Value> operands = instr.getOperands();
            if (!(instr instanceof MoveInstr)) {
                for (Value operand : operands) {
                    if (lastUse.containsKey(operand) && lastUse.get(operand) == instr && ! entry.getOut().contains(operand) && var2reg.containsKey(operand)) {
                        reg2var.remove(var2reg.get(operand));
                        neverUsedAfter.add(operand);
                    }
                }
            } else {
                Value src = ((MoveInstr)instr).getSrc();
                if (lastUse.containsKey(src) && lastUse.get(src) == instr && !entry.getOut().contains(src) && var2reg.containsKey(src)) {
                    reg2var.remove(var2reg.get(src));
                    neverUsedAfter.add(src);
                }
            }

            if (!instr.isVoid() && !(instr instanceof ZextInstr) && !(instr instanceof MoveInstr)) {
                defined.add(instr);
                Register reg = allocRegFor();
                if (reg != null) {
                    if (reg2var.containsKey(reg)) {
                        var2reg.remove(reg2var.get(reg));
                    }
                    reg2var.put(reg, instr);
                    var2reg.put(instr, reg);
                }
            }
            else if (instr instanceof MoveInstr) {
                defined.add(((MoveInstr)instr).getDst());
                Register reg = allocRegFor();
                if (reg != null) {
                    if (reg2var.containsKey(reg)) {
                        var2reg.remove(reg2var.get(reg));
                    }
                    reg2var.put(reg, ((MoveInstr)instr).getDst());
                    var2reg.put(((MoveInstr)instr).getDst(), reg);
                }
            }
        }

        for (BasicBlock child : entry.getDomlist()) {
            HashMap<Register, Value> buffer = new HashMap<>();

            for (Register reg : reg2var.keySet()) {
                Value var = reg2var.get(reg);
                if (!child.getIn().contains(var)) {
                    buffer.put(reg, var);
                }
            }
            for (Register reg : buffer.keySet()) {
                reg2var.remove(reg);
            }
            allocaForBB(child);
            for (Register reg : buffer.keySet()) {
                reg2var.put(reg, buffer.get(reg));
            }
        }

        for (Value value : defined) {
            if (!(value instanceof PhiInstr)) {
                if (var2reg.containsKey(value)) {
                    reg2var.remove(var2reg.get(value));
                }
            }
        }

        for (Value value : neverUsedAfter) {
            if (var2reg.containsKey(value) && !defined.contains(value)) {
                reg2var.put(var2reg.get(value), value);
            }
        }

    }

    public Register allocRegFor() {
        Set<Register> allocated = reg2var.keySet();
        for (Register reg : regSet) {
            if (!allocated.contains(reg)) {
                return reg;
            }
        }
        return regSet.iterator().next();
    }

}
