package mips;

import lightllr.*;
import lightllr.Module;

import java.util.*;

public class RegAllocator {
    private Module module;
    private HashMap<Register, Value> reg2var;
    private HashMap<Value, Register> var2reg;
    private ArrayList<Register> regSet;

    public RegAllocator(Module module) {
        this.module = module;
        this.regSet = new ArrayList<>();
        for (Register value : Register.values()) {
            if (value.ordinal() >= Register.T0.ordinal() && value.ordinal() <= Register.T9.ordinal()) {
                regSet.add(value);
            }
        }
    }

    public void run() {
        for (Function function : module.getFunctions()) {
            if (function.getBasicBlocks().size() > 0) {
                BasicBlock entry = function.getEntryBB();
                init();
                allocaForBB(entry);
                function.setVar2reg(var2reg);
                // 打印调试
                System.out.println("\n" + function.getName());
                ArrayList<Value> varList = new ArrayList<>(var2reg.keySet());
                Collections.sort(varList, Comparator.comparing(Value::getName));
                for (Value var : varList) {
                    Register reg = var2reg.get(var);
                    System.out.println(var.getName() + " ==> " + reg);
                }
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
        HashMap<Value, Value> lastUse = new HashMap<>(); // key的最后一次使用是在value中

        // 1.遍历一边所有指令，记录每个变量在该基本块里最后一次被使用的位置
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
            } else if (instr instanceof CallInstr) {
                for (int i = 1; i < instr.getNumOps(); i++) {
                    if (!(instr.getOperand(i) instanceof ConstantInt)) {
                        lastUse.put(instr.getOperand(i), instr);
                    }
                }
            } else if (instr instanceof BranchInstr && ((BranchInstr) instr).is_cond_br()) {
                if (!(instr.getOperand(0) instanceof ConstantInt)) {
                    lastUse.put(instr.getOperand(0), instr);
                }
            } else if (instr instanceof ReturnInstr && instr.getNumOps() > 0) {
                if (!(instr.getOperand(0) instanceof ConstantInt)) {
                    lastUse.put(instr.getOperand(0), instr);
                }
            }

        }

        // 2.再遍历一遍所有指令
        for (Instruction instr : instrList) {
            // 如果该指令的某个operand是该基本块内的最后一次使用，
            // 并且该基本块的out中没有这个operand
            // 那么我们可以暂时释放这个变量所占用的寄存器（释放reg2var，但不改变var2reg）
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

            // 如果该指令属于定义语句，并且不是创建数组的alloc指令
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
            } else if (instr instanceof MoveInstr) {
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

        // 3.遍历其直接支配的节点
        for (BasicBlock child : entry.getDomlist()) {
            // 如果当前个寄存器所对应的变量在其child中不会被使用到(即in中不包含该变量)
            // 可以记录这个映射关系到buffer中，并释放该寄存器
            // 当为child（包括child的child）分配完寄存器后，再将buffer中的映射关系恢复
            // 以免影响其兄弟节点的寄存器分配
            HashMap<Register, Value> buffer = new HashMap<>();

            for (Register reg : reg2var.keySet()) {
                Value var = reg2var.get(reg);
                if (!child.getIn().contains(var)) {
                    buffer.put(reg, var);
                }
            }
            // 将buffer中记录的分配删除
            for (Register reg : buffer.keySet()) {
                reg2var.remove(reg);
            }
            // 为child调用该函数
            allocaForBB(child);
            // 将buffer中存储的被删除的映射关系恢复
            for (Register reg : buffer.keySet()) {
                reg2var.put(reg, buffer.get(reg));
            }
        }

        for (Value value : defined) {
            if (var2reg.containsKey(value)) {
                reg2var.remove(var2reg.get(value));
            }
        }

        // 5.将“后继不再使用但是是从前驱BB传过来”的变量对应的寄存器映射恢复回来
        // 也就是在neverUsedAfter中，但是不是在当前基本块定义的变量
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
