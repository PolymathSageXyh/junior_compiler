package lightllr.optimization;

import lightllr.*;
import lightllr.Module;
import paser.Mypair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Mem2Reg extends Pass {
    private Function func;
    private Dominators dominators;
    private HashMap<Value, ArrayList<Value>> var_val_stack = new HashMap<>();

    public Mem2Reg(Module module) {
        super(module);
    }

    @Override
    public void run() {
        // get info from Dominators
        dominators = new Dominators(module);
        dominators.run();
        int i = 0;
        for (Function f : module.getFunctions()) {
            func = f;
            if (func.getBasicBlocks().size() >= 1) {
                generate_phi();


                //module.print();
                re_name(f.getEntryBB());
                //System.out.println(i);
                i++;

            }
            remove_alloca();
            //System.out.println(func.print());
        }
    }

    public void remove_alloca() {
        for (BasicBlock bb : func.getBasicBlocks()) {
            ArrayList<Instruction> wait_delete = new ArrayList<>();
            for (Instruction instr : bb.getInstrList()) {
                boolean is_alloca = instr instanceof AllocaInstr;
                if (is_alloca) {
                    AllocaInstr al = (AllocaInstr)instr;
                    boolean is_int = al.getIrType().getPointerElementType().isIntegerType();
                    if (is_int) {
                        wait_delete.add(instr);
                    }
                }
            }
            for (Instruction instr : wait_delete) {
                bb.deleteInstr(instr);
            }
        }
    }

    public boolean IS_GLOBAL_VARIABLE(Value l_val) {
        return l_val instanceof GlobalVariable;
    }

    public boolean IS_GEP_INSTR(Value l_val) {
        return l_val instanceof GetElementPtrInstr;
    }

    public void generate_phi() {
        // step 1: find all global_live_var_name x and get their blocks
        LinkedHashSet<Value> global_live_var_name = new LinkedHashSet<>();
        HashMap<Value, LinkedHashSet<BasicBlock>> live_var_2blocks = new HashMap<>();
        for (BasicBlock bb : func.getBasicBlocks()) {
            for (Instruction instr : bb.getInstrList()) {
                if (instr.isStore()) {
                    // store i32 a, i32 *b
                    // a is r_val, b is l_val
                    Value r_val = ((StoreInstr)instr).getRval();
                    Value l_val = ((StoreInstr)instr).getLval();
                    if (!IS_GLOBAL_VARIABLE(l_val) && !IS_GEP_INSTR(l_val))
                    {
                        global_live_var_name.add(l_val);
                        if (!live_var_2blocks.containsKey(l_val)) {
                            LinkedHashSet<BasicBlock> tmp = new LinkedHashSet<>();
                            tmp.add(bb);
                            live_var_2blocks.put(l_val, tmp);
                        } else {
                            live_var_2blocks.get(l_val).add(bb);
                        }

                    }
                }
            }
        }

        // step 2: insert phi instr
        HashMap<Mypair<BasicBlock, Value>, Boolean> bb_has_var_phi = new HashMap<>(); // bb has phi for var
        for (Value var : global_live_var_name) {
            ArrayList<BasicBlock> work_list = new ArrayList<>(live_var_2blocks.get(var));
            for (int i =0; i < work_list.size(); i++) {
                BasicBlock bb = work_list.get(i);
                //System.out.println(bb.getName());
                for (BasicBlock bb_dominance_frontier_bb : dominators.getDominanceFrontier(bb)) {
                    //System.out.println("bb_dominace  " + bb_dominance_frontier_bb.getName());
                    if (!bb_has_var_phi.containsKey(Mypair.of(bb_dominance_frontier_bb, var))) {
                        // generate phi for bb_dominance_frontier_bb & add bb_dominance_frontier_bb to work list
                        PhiInstr phi = PhiInstr.createPhi(var.getIrType().getPointerElementType(), bb_dominance_frontier_bb);
                        phi.setLval(var);
                        phi.setName();
                        bb_dominance_frontier_bb.addInstrBegin(phi);
                        work_list.add(bb_dominance_frontier_bb);
                        bb_has_var_phi.put(Mypair.of(bb_dominance_frontier_bb, var), true);
                    }
                }
            }
        }
    }

    public void re_name(BasicBlock bb) {
        ArrayList<Instruction> wait_delete = new ArrayList<>();
        for (Instruction instr : bb.getInstrList()) {
            if (instr.isPhi()) {
                // step 3: push phi instr as lval's lastest value define
                Value l_val = ((PhiInstr)instr).getLval();
                if (!var_val_stack.containsKey(l_val)) {
                    ArrayList<Value> tmp = new ArrayList<>();
                    tmp.add(instr);
                    var_val_stack.put(l_val, tmp);
                } else {
                    var_val_stack.get(l_val).add(instr);
                }
                //System.out.println("当前bb " + bb.getName() +"左值  " + l_val.getName() + "新定义 phi  " + "当前大小 " + var_val_stack.get(l_val).size());
            }
        }

        for (Instruction instr : bb.getInstrList()) {
            if (instr.isLoad()) {
                // step 4: replace load with the top of stack[l_val]
                Value l_val = ((LoadInstr)instr).getLval();
                if (!IS_GLOBAL_VARIABLE(l_val) && !IS_GEP_INSTR(l_val))
                {
                    if (var_val_stack.containsKey(l_val) && var_val_stack.get(l_val).size() > 0)
                    {
                        //System.out.println("Load 左值  " + l_val.getName());
                        instr.replace_all_use_with(var_val_stack.get(l_val).get(var_val_stack.get(l_val).size()-1));
                        wait_delete.add(instr);
                    }
                }
            }
            if (instr.isStore()) {
                // step 5: push r_val of store instr as lval's lastest definition
                Value l_val = ((StoreInstr)instr).getLval();
                Value r_val = ((StoreInstr)instr).getRval();
                if (!IS_GLOBAL_VARIABLE(l_val) && !IS_GEP_INSTR(l_val))
                {
                    if (!var_val_stack.containsKey(l_val)) {
                        ArrayList<Value> tmp = new ArrayList<>();
                        tmp.add(r_val);
                        var_val_stack.put(l_val, tmp);
                    } else {
                        var_val_stack.get(l_val).add(r_val);
                    }
                    //System.out.println("当前bb " + bb.getName()+"左值  " + l_val.getName() + "更新为store右值  " + r_val.getName() + "当前大小 " + var_val_stack.get(l_val).size());
                    wait_delete.add(instr);
                }
            }
        }

        for (BasicBlock succ_bb : bb.getSuccbbs()) {
            //System.out.println("进入当前bb " + bb.getName() + "后继节点 " + succ_bb.getName());
            for (Instruction instr : succ_bb.getInstrList()) {
                if (instr.isPhi()) {
                    Value l_val = ((PhiInstr)instr).getLval();
                    //System.out.println("hhhhhhhhh" + l_val.getName());
                    if (var_val_stack.containsKey(l_val) && var_val_stack.get(l_val).size() > 0) {
                        assert var_val_stack.get(l_val).size()!=0 : "yaoqukong";
                        // step 6: fill phi pair parameters
                        //System.out.println("lsjkdgflsjglsjgl   "+var_val_stack.get(l_val).size() + succ_bb.getName());
                        //System.out.println("当前bb " + bb.getName() + "后继节点 " + succ_bb.getName()+ "  添加操作数 l_val"+l_val.getName()+"最新值" + var_val_stack.get(l_val).get(var_val_stack.get(l_val).size()-1).getName());
                        ((PhiInstr)instr).addPhiPairOperand(var_val_stack.get(l_val).get(var_val_stack.get(l_val).size()-1), bb);
                    }
                    // else phi parameter is [ undef, bb ]
                }
            }
        }

        for (BasicBlock dom_succ_bb : dominators.getDomTreeSuccBlocks(bb)) {
            re_name(dom_succ_bb);
        }

        for (Instruction instr : bb.getInstrList()) {
            // step 7: pop lval's lastest definition
            if(instr.isStore()) {
                Value l_val = ((StoreInstr)instr).getLval();
                if (!IS_GLOBAL_VARIABLE(l_val) && !IS_GEP_INSTR(l_val) && var_val_stack.containsKey(l_val) && var_val_stack.get(l_val).size() > 0) {
                    //System.out.println("当前bb " + bb.getName()+"弹出 " + l_val.getName() + "顶层值 " + var_val_stack.get(l_val).get(var_val_stack.get(l_val).size()-1).getName());
                    var_val_stack.get(l_val).remove(var_val_stack.get(l_val).size()-1);
                    //System.out.println("当前bb " + bb.getName()+"弹出后大小" +  var_val_stack.get(l_val).size());
                }
            }
            else if (instr.isPhi()) {
                Value l_val = ((PhiInstr)instr).getLval();
                if (var_val_stack.containsKey(l_val) && var_val_stack.get(l_val).size() > 0) {
                    //System.out.println("当前bb " + bb.getName()+"弹出 " + l_val.getName() + "顶层值 " + var_val_stack.get(l_val).get(var_val_stack.get(l_val).size()-1).getName());
                    var_val_stack.get(l_val).remove(var_val_stack.get(l_val).size()-1);
                    //System.out.println("当前bb " + bb.getName()+"弹出后大小" +  var_val_stack.get(l_val).size());
                }
            }
        }
        for (Instruction instr : wait_delete) {
            bb.deleteInstr(instr);
        }
    }

}
