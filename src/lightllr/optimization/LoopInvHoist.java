package lightllr.optimization;

import lightllr.*;
import lightllr.Module;
import paser.Mypair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LoopInvHoist extends Pass {
    public LoopInvHoist(Module module) {
        super(module);
    }

    @Override
    public void run() {
        LoopSearch loop_searcher = new LoopSearch(module);
        loop_searcher.run();
        ArrayList<Function> func_list = module.getFunctions();
        for (Function func : func_list) {
            if (func.getBasicBlocks().size() > 0) {
                HashSet<HashSet<BasicBlock>> loops_in_func = loop_searcher.get_loops_in_func(func);
                HashMap<BasicBlock, Boolean> loop2child = new HashMap<>();
                for (HashSet<BasicBlock> cloop : loops_in_func) {
                    BasicBlock base = loop_searcher.get_loop_base(cloop);
                    loop2child.put(base, true);
                }
                for (HashSet<BasicBlock> cloop : loops_in_func) {
                    HashSet<BasicBlock> ploop = loop_searcher.get_parent_loop(cloop);
                    if (ploop != null) {
                        BasicBlock base = loop_searcher.get_loop_base(ploop);
                        loop2child.put(base, false);
                    } // ploop:外一层循环
                }
                while (loops_in_func.size() > 0) {
                    // 函数中存在未分析的循环
                    HashSet<BasicBlock> inner_loop = null;
                    for (HashSet<BasicBlock> cloop : loops_in_func) {
                        BasicBlock base = loop_searcher.get_loop_base(cloop);
                        if (loop2child.get(base)) {
                            inner_loop = cloop;
                            break;
                        }
                    }
                    // 此时 inner_loop 是当前未分析的最小循环
                    while (inner_loop != null) {
                        HashSet<Value> define_set = new HashSet<>();
                        ArrayList<Instruction> wait_move = new ArrayList<>();
                        HashMap<Instruction, BasicBlock> l_val2bb_set = new HashMap<>();
                        for (BasicBlock bb : inner_loop) {
                            // 遍历 inner 循环中的每一条语句
                            for (Instruction instr : bb.getInstrList()) {
                                define_set.add(instr);
                                l_val2bb_set.put(instr, bb); // 指令--指令所在的 basicblock
                            }
                        }
                        // 分析指令参数是否与循环无关, 找出循环不变式
                        boolean changed;
                        do {
                            changed = false;
                            for (BasicBlock bb : inner_loop) {
                                for (Instruction instr : bb.getInstrList()) {
                                    if (!define_set.contains(instr)) {
                                        continue;
                                    }
                                    boolean move = true;
                                    if (instr.isLoad() || instr.isStore() || instr.isBr() || instr.isPhi() || instr.isCall() || instr.isRet()) {
                                        continue;
                                    } else {
                                        for (int i = 0; i < instr.getNumOps(); i++) {
                                            Value arg = instr.getOperand(i);
                                            if (define_set.contains(arg) || arg instanceof GetElementPtrInstr
                                                    || arg instanceof GlobalVariable) {
                                                // arg 在循环中有定值
                                                move = false;
                                                break;
                                            }
                                        }
                                    }
                                    if (move) {
                                        changed = true;
                                        wait_move.add(instr);
                                        define_set.remove(instr);
                                    }
                                }
                            }
                        } while (changed);


                        BasicBlock base = loop_searcher.get_loop_base(inner_loop);
                        for (BasicBlock prev : base.getPrebbs()) {
                            if (!inner_loop.contains(prev)) {
                                Instruction br = prev.get_terminator();
                                prev.getInstrList().remove(br);
                                while (wait_move.size() > 0) {
                                    Instruction move_ins = wait_move.get(0);
                                    prev.addInstruction(move_ins);
                                    l_val2bb_set.get(move_ins).getInstrList().remove(move_ins);
                                    wait_move.remove(0);
                                }
                                prev.addInstruction(br);
                                break;
                            }
                        }
                        HashSet<BasicBlock> parent_loop = loop_searcher.get_parent_loop(inner_loop);
                        loops_in_func.remove(inner_loop);
                        if (parent_loop != null) {
                            BasicBlock bb = loop_searcher.get_loop_base(parent_loop);
                            loop2child.put(bb, true);
                        }
                        inner_loop = parent_loop;
                    }
                }
            }
        }
    }

}
