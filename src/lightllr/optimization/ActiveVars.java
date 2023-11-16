package lightllr.optimization;

import lightllr.*;
import lightllr.Module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class ActiveVars extends Pass {
    private Function func;
    HashMap<BasicBlock, HashSet<Value>> live_in, live_out;
    HashMap<BasicBlock, HashSet<Value>> map_use, map_def;
    //HashMap<BasicBlock, HashMap<Value, BasicBlock>> big_phi;

    public ActiveVars(Module module) {
        super(module);
        map_def = new HashMap<>();
        map_use = new HashMap<>();
        //big_phi = new HashMap<>();
    }

    public boolean judge_set(HashSet<Value> set1, HashSet<Value> set2) {
        return set1.equals(set2);
    }

    @Override
    public void run() {
        for (Function function : this.module.getFunctions()) {
            if (function.getBasicBlocks().isEmpty()) {
                continue;
            } else {
                func = function;
                live_in = new HashMap<>();
                live_out = new HashMap<>();
                for (BasicBlock bb : func.getBasicBlocks()) {
                    HashSet<Value> use = new HashSet<>();
                    HashSet<Value> def = new HashSet<>();
                    for (Instruction instr: bb.getInstrList()) {
                        if(instr.isBinary() || instr.isCmp() ) {
                            if (!((instr.getOperand(0)) instanceof ConstantInt) && !def.contains(instr.getOperand(0))) {
                                use.add(instr.getOperand(0));
                            }
                            if (!((instr.getOperand(1)) instanceof ConstantInt) && !def.contains(instr.getOperand(1))) {
                                use.add(instr.getOperand(1));
                            }
                            if (!use.contains(instr)) {
                                def.add(instr);
                            }
                        }
                        else if (instr.isGep()) {
                            for (int i = 0; i < instr.getNumOps(); i++) {
                                if (!((instr.getOperand(i)) instanceof ConstantInt) && !def.contains(instr.getOperand(i))) {
                                    use.add(instr.getOperand(i));
                                }
                                if (!use.contains(instr)) {
                                    def.add(instr);
                                }
                            }
                        }
                        else if (instr.isAlloca()) {
                            if (!use.contains(instr)) {
                                def.add(instr);
                            }
                        }
                        else if (instr.isLoad()) {
                            Value l_val = instr.getOperand(0);
                            if (!def.contains(l_val)) {
                                use.add(l_val);
                            }
                            if (!use.contains(instr)) {
                                def.add(instr);
                            }
                        }
                        else if(instr.isStore()) {
                        //r_val:use
                        //l_val:def
                            Value l_val = ((StoreInstr)instr).getLval();
                            Value r_val = ((StoreInstr)instr).getRval();
                            if ((!def.contains(r_val)) && !(r_val instanceof ConstantInt)) {
                                use.add(r_val);
                            }
                            if ((!def.contains(l_val)) && !(l_val instanceof ConstantInt)) {
                                use.add(l_val);
                            }
                        }
                    //other operations
                        else if (instr.isCall()) {
                            for (int i = 1; i < instr.getNumOps(); i++) {
                                if ((!def.contains(instr.getOperand(i))) && (!(instr.getOperand(i) instanceof ConstantInt))) {
                                    use.add(instr.getOperand(i));
                                }
                            }
                            if ((!use.contains(instr)) && !instr.isVoid()) {
                                def.add(instr);
                            }
                        }
                        else if (instr.isZext()) {
                            if (!def.contains(instr.getOperand(0))) {
                                use.add(instr.getOperand(0));
                            }
                            if (!use.contains(instr)) {
                                def.add(instr);
                            }
                        }
                        else if (instr.isRet()) {
                            if ((!((ReturnInstr) instr).isVoidRet()) && (!def.contains(instr.getOperand(0))) && (!(instr.getOperand(0) instanceof ConstantInt))) {
                                use.add(instr.getOperand(0));
                            }
                        }
//                        else if (instr.isPhi()) {
//                            for (int i = 0; i < instr.getNumOps() / 2; i++) {
//                                if(!def.contains(instr.getOperand(2*i)) && !(instr.getOperand(2*i) instanceof ConstantInt)) {
//                                    use.add(instr.getOperand(2 * i));
//                                    if (!big_phi.containsKey(bb)) {
//                                        HashMap<Value, BasicBlock> hh = new HashMap<>();
//                                        hh.put(instr.getOperand(2*i), ((BasicBlock) instr.getOperand(2*i+1)));
//                                        big_phi.put(bb, hh);
//                                    } else {
//                                        big_phi.get(bb).put(instr.getOperand(2*i), ((BasicBlock) instr.getOperand(2*i+1)));
//                                    }
//                                }
//                            }
//                            if (!use.contains(instr)) {
//                                def.add(instr);
//                            }
//                        }
                        else if (instr.isMov()) {
                            Value src = ((MoveInstr)instr).getSrc();
                            Value dst = ((MoveInstr)instr).getDst();
                            if (!(src instanceof ConstantInt)) {
                                use.add(src);
                            }
                            if (!use.contains(dst)) {
                                def.add(dst);
                            }
                        }
                        else if (instr.isBr()) {
                            if (!(instr.getOperand(0) instanceof ConstantInt) && !def.contains(instr.getOperand(0))) {
                                if (((BranchInstr)instr).is_cond_br()) {
                                    use.add(instr.getOperand(0));
                                }
                            }
                        }
                    }
                    bb.setDef(def);
                    bb.setUse(use);
                    map_def.put(bb, def);
                    map_use.put(bb, use);
                }

                boolean is_change = true;
                HashSet<Value> union_ins = new HashSet<>();
                for(BasicBlock bb : func.getBasicBlocks()) {
                    HashSet<Value> initial_set = new HashSet<>();
                    live_in.put(bb, initial_set);
                }
                while (is_change) {
                    is_change = false;
                    for (BasicBlock bb : func.getBasicBlocks()) {
                        for (BasicBlock succ_bb : bb.getSuccbbs()) {
                            HashSet<Value> INS = live_in.get(succ_bb);
                            union_ins.addAll(INS);
                        }
                        live_out.put(bb, union_ins);
                        HashSet<Value> newIn = new HashSet<>(union_ins);
                        newIn.removeAll(map_def.get(bb));
                        newIn.addAll(map_use.get(bb));
                        HashSet<Value> before = live_in.get(bb);
                        live_in.put(bb, newIn);
                        if (!judge_set(before, newIn)) {
                            is_change = true;
                        }
                    }
                }
                for (BasicBlock bb : func.getBasicBlocks()) {
                    bb.setIn(live_in.get(bb));
                    bb.setOut(live_out.get(bb));
                }
                System.out.println(print());
            }
            map_def.clear();
            map_use.clear();
            //big_phi.clear();
            for (BasicBlock bb : function.getBasicBlocks()) {
                System.out.println("\n" + bb.getName() + ": ");
                System.out.print("use: ");
                for (Value value : bb.getUse()) {
                    System.out.print(value.getName() + " ");
                }
                System.out.print("\ndef:");
                for (Value value : bb.getDef()) {
                    System.out.print(value.getName() + " ");
                }
                System.out.print("\nin:");
                for (Value value : bb.getIn()) {
                    System.out.print(value.getName() + " ");
                }
                System.out.print("\nout:");
                for (Value value : bb.getOut()) {
                    System.out.print(value.getName() + " ");
                }
                System.out.println("");
            }
        }
    }

    public String print() {
        String active_vars = "";
        active_vars +=  "{\n";
        active_vars +=  "\"function\": \"";
        active_vars +=  func.getName();
        active_vars +=  "\",\n";

        active_vars +=  "\"live_in\": {\n";
        for(HashMap.Entry<BasicBlock, HashSet<Value>> entry : live_in.entrySet()) {
            if (entry.getValue().size() != 0) {
                active_vars +=  "  \"";
                active_vars +=  entry.getKey().getName();
                active_vars +=  "\": [" ;
                for (Value v : entry.getValue()) {
                    active_vars +=  "\"%";
                    active_vars +=  v.getName();
                    active_vars +=  "\",";
                }
                active_vars += "]";
                active_vars += ",\n";
            }
        }
        active_vars += "\n";
        active_vars +=  "    },\n";

        active_vars +=  "\"live_out\": {\n";
        for(HashMap.Entry<BasicBlock, HashSet<Value>> entry : live_out.entrySet()) {
            if (entry.getValue().size() != 0) {
                active_vars +=  "  \"";
                active_vars +=  entry.getKey().getName();
                active_vars +=  "\": [" ;
                for (Value v : entry.getValue()) {
                    active_vars +=  "\"%";
                    active_vars +=  v.getName();
                    active_vars +=  "\",";
                }
                active_vars += "]";
                active_vars += ",\n";
            }
        }
        active_vars += "\n";
        active_vars +=  "    },\n";

        active_vars += "}\n";
        active_vars += "\n";
        return active_vars;
    }

}
