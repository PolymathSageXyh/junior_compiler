package lightllr.optimization;

import lightllr.*;
import lightllr.Module;
import paser.Mypair;

import java.util.*;

public class ConstPropagation extends Pass {

    public ConstPropagation(Module module) {
        super(module);
    }

    @Override
    public void run() {
        ConstFolder CF = new ConstFolder(module);
        for (Function func : module.getFunctions()) {
            if (func.getNumBasicBlocks() == 0) continue;
            HashSet<BasicBlock> visitedBB = new HashSet<>();
            ArrayDeque<BasicBlock> bbQueue = new ArrayDeque<>();
            HashSet<BasicBlock> undirectReach = new HashSet<>();
            bbQueue.push(func.getEntryBB());
            visitedBB.add(func.getEntryBB());
            while (!bbQueue.isEmpty()) {
                BasicBlock bb = bbQueue.pollFirst();
                if (bb.isInstrListEmpty()) continue;
                HashSet<Instruction> instrToBeDelete = new HashSet<>();
                HashMap<Value, Stack<Value>> pointerConst = new HashMap<>();
                for (Instruction instr: bb.getInstrList()) {
                    if (instr.isBinary()) {
                        Instruction.OpID op = instr.getInstrType();
                        Value lhs = instr.getOperand(0);
                        Value rhs = instr.getOperand(1);
                        if (lhs instanceof ConstantInt && rhs instanceof ConstantInt) {
                            instr.replace_all_use_with(CF.compute(op, (ConstantInt) lhs, (ConstantInt) rhs));
                            instrToBeDelete.add(instr);
                        }
                    } else if (instr.isZext()) {
                        if ((instr.getOperand(0)) instanceof ConstantInt) {
                            instr.replace_all_use_with(ConstantInt.get
                                    (((ConstantInt)instr.getOperand(0)).getTruth(), module));
                            instrToBeDelete.add(instr);
                        }
                    } else if (instr.isCmp()) {
                        Value lhs = instr.getOperand(0);
                        Value rhs = instr.getOperand(1);
                        if (lhs instanceof ConstantInt && rhs instanceof ConstantInt) {
                            instr.replace_all_use_with(CF.compute_comp(((CmpInstr)instr).getCmpOp(), (ConstantInt) lhs, (ConstantInt) rhs));
                            instrToBeDelete.add(instr);
                        }
                    } else if (instr.isStore()) {
                        Value tmp = instr.getOperand(1); //ptr
                        if (!pointerConst.containsKey(tmp) && !(tmp instanceof GlobalVariable)) {
                            Stack<Value> hh = new Stack<>();
                            pointerConst.put(tmp, hh);
                        }
                        if (!(tmp instanceof GlobalVariable))
                            pointerConst.get(tmp).push(instr.getOperand(0)); //val
                    } else if (instr.isLoad()) {
                        // 若存在，则说明值为刚刚存进去的，直接替换指令
                        if (pointerConst.containsKey(instr.getOperand(0))) {
                            instr.replace_all_use_with(pointerConst.get(instr.getOperand(0)).peek());
                            instrToBeDelete.add(instr);
                        }
                        else {
                            if (!(instr.getOperand(0) instanceof GlobalVariable)) {
                                Stack<Value> hh = new Stack<>();
                                pointerConst.put(instr.getOperand(0), hh);
                                pointerConst.get(instr.getOperand(0)).push(instr);
                            }
                        }
                    } else if (instr.isBr()) {
                        if (((BranchInstr)instr).is_cond_br()) {
                            Value cond = instr.getOperand(0);
                            BasicBlock trueBB =  ((BasicBlock)(instr.getOperand(1)));
                            BasicBlock falseBB = ((BasicBlock)(instr.getOperand(2)));
                            if (cond instanceof ConstantInt) {
                                boolean _tar = ((ConstantInt)cond).getTruth() != 0;
                                if (_tar) {
                                    BranchInstr tmp = BranchInstr.uncond_create_br (trueBB, bb);
                                    instr.replace_all_use_with(tmp);
                                    bb.addInstruction(tmp);
                                    bb.deleteInstr(instr);
                                    if (!visitedBB.contains(trueBB)) {
                                        visitedBB.add(trueBB);
                                        bbQueue.push(trueBB);
                                    }
                                    bb.getSuccbbs().remove(falseBB);
                                    falseBB.getPrebbs().remove(bb);
                                    undirectReach.add(bb);
                                } else {
                                    BranchInstr tmp = BranchInstr.uncond_create_br (falseBB, bb);
                                    instr.replace_all_use_with(tmp);
                                    bb.addInstruction(tmp);
                                    bb.deleteInstr(instr);
                                    if (!visitedBB.contains(falseBB)) {
                                        visitedBB.add(falseBB);
                                        bbQueue.push(falseBB);
                                    }
                                    bb.getSuccbbs().remove(trueBB);
                                    trueBB.getPrebbs().remove(bb);
                                    undirectReach.add(bb);
                                }
                            } else {
                                if (!visitedBB.contains(trueBB)) {
                                    visitedBB.add(trueBB);
                                    bbQueue.push(trueBB);
                                }
                                if (!visitedBB.contains(falseBB)) {
                                    visitedBB.add(falseBB);
                                    bbQueue.push(falseBB);
                                }
                            }
                        } else {
                            BasicBlock trueBB = (BasicBlock)(instr.getOperand(0));
                            if (!visitedBB.contains(trueBB)) {
                                visitedBB.add(trueBB);
                                bbQueue.push(trueBB);
                            }
                        }

                    }
                }
                for (Instruction instr : instrToBeDelete){
                    bb.deleteInstr(instr);
                }

            }
            HashSet<BasicBlock> tbdBB = new HashSet<>();
            for(BasicBlock  bb: func.getBasicBlocks()){
                if(!visitedBB.contains(bb)){
                    tbdBB.add(bb);
                }
            }
            tbdBB.addAll(undirectReach);
            for(BasicBlock bb : tbdBB) {
                ArrayList<Use> l = bb.getUseList();
                HashMap<PhiInstr, Mypair<Integer, Integer>> waitToDel = new HashMap<>();
                for(Use use : l) {
                    if (!visitedBB.contains(bb))
                        use.getVal().remove_use(bb);
                    if(use.getVal() instanceof PhiInstr){
                        PhiInstr pinstr = (PhiInstr)(use.getVal());
                        int indTBR = -1;
                        for(int i = 0; i < pinstr.getNumOps(); ++i){
                            if (!visitedBB.contains(bb)) {
                                if(pinstr.getOperand(i) == bb) {
                                    indTBR = i;
                                    for(int j = i + 2; j < pinstr.getNumOps(); j += 1){
                                        pinstr.getOperand(j).remove_use(pinstr);
                                        pinstr.getOperand(j).addUse(pinstr, j - 2);
                                    }
                                    break;
                                }
                            } else {
                                if (!pinstr.getParent().getPrebbs().contains(bb) && pinstr.getOperand(i) == bb) {
                                    indTBR = i;
                                    for(int j = i + 2; j < pinstr.getNumOps(); j += 1){
                                        pinstr.getOperand(j).remove_use(pinstr);
                                        pinstr.getOperand(j).addUse(pinstr, j - 2);
                                    }
                                    break;
                                }
                            }

                        }
                        if(indTBR > 0) {
                            waitToDel.put(pinstr, Mypair.of(indTBR-1, indTBR));
                            //pinstr.remove_operands(indTBR - 1, indTBR);
                        }
                    }
                }
                for (PhiInstr pp : waitToDel.keySet()) {
                    pp.remove_operands(waitToDel.get(pp).first, waitToDel.get(pp).second);
                }
                for (PhiInstr pp : waitToDel.keySet()) {
                    if (pp.getOperands().size() == 2) {
                        pp.replace_all_use_with(pp.getOperand(0));
                        pp.getParent().deleteInstr(pp);
                    }
                }
                if(!visitedBB.contains(bb)) {
                    func.remove(bb);
                    for (BasicBlock item : bb.getSuccbbs()) {
                        item.getPrebbs().remove(bb);
                    }
                    for (BasicBlock item : bb.getPrebbs()) {
                        item.getSuccbbs().remove(bb);
                    }
                }
            }
        }

    }

    public class ConstFolder {
        private Module module;

        public ConstFolder(Module module) {
            this.module = module;
        }

        public ConstantInt compute(Instruction.OpID op, ConstantInt value1, ConstantInt value2) {
            int c_value1 = value1.getTruth();
            int c_value2 = value2.getTruth();
            return switch (op) {
                case add -> ConstantInt.get(c_value1 + c_value2, module);
                case sub -> ConstantInt.get(c_value1 - c_value2, module);
                case mul -> ConstantInt.get(c_value1 * c_value2, module);
                case sdiv -> ConstantInt.get(c_value1 / c_value2, module);
                case srem -> ConstantInt.get(c_value1 % c_value2, module);
                default -> null;
            };
        }

        public ConstantInt compute_comp(CmpInstr.CmpOp op, ConstantInt value1, ConstantInt value2) {
            int c_value1 = value1.getTruth();
            int c_value2 = value2.getTruth();
            return switch (op) {
                case EQ -> ConstantInt.get(c_value1 == c_value2, module);
                case NE -> ConstantInt.get(c_value1 != c_value2, module);
                case GT -> ConstantInt.get(c_value1 > c_value2, module);
                case GE -> ConstantInt.get(c_value1 >= c_value2, module);
                case LT -> ConstantInt.get(c_value1 < c_value2, module);
                case LE -> ConstantInt.get(c_value1 <= c_value2, module);
            };
        }

    }

}
