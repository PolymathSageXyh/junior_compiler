package lightllr.optimization;

import lightllr.*;
import lightllr.Module;

import java.util.ArrayList;

public class SideEffect extends Pass {

    public SideEffect (Module module) {
        super(module);
    }

    @Override
    public void run() {
        getCallRel();
        for (Function func : module.getFunctions()) {
            for (BasicBlock bb : func.getBasicBlocks()) {
                for (Instruction instr : bb.getInstrList()) {
                    if (instr instanceof StoreInstr) {
                        func.setHasSideEffect(true);
                    }
                }
            }
        }
        for (Function f : module.getFunctions()) {
            if (f.isHasSideEffect()) {
                passSideEffect(f);
            }
        }
    }

    public void getCallRel() {
        for (Function func : module.getFunctions()) {
            func.getCalleeList().clear();
            func.getCallerList().clear();
        }
        for (Function func : module.getFunctions()) {
            for (BasicBlock bb : func.getBasicBlocks()) {
                for (Instruction instr : bb.getInstrList()) {
                    if (instr instanceof CallInstr) {
                        Function calleeF = ((Function) instr.getOperand(0));
                        //System.out.println("now callinstr is " + calleeF.getName());
                        if (!calleeF.getCallerList().contains(func)) {
                            calleeF.getCallerList().add(func);
                            //System.out.println(calleeF.getName() + " is used by " + func.getName());
                        }
                        if (!calleeF.isLibrary() && !func.getCalleeList().contains(calleeF)) {
                            func.getCalleeList().add(calleeF);
                        }
                    }
                }
            }
        }
        ArrayList<Function> uselessFuncs = new ArrayList<>();
        for (Function func : module.getFunctions()) {
            if (func.getCallerList().isEmpty() && !func.getName().equals("main") && !func.isLibrary()) {
                uselessFuncs.add(func);
                for (Function f : func.getCalleeList()) {
                    f.getCallerList().remove(func);
                }
            }
        }
        module.getFunctions().removeAll(uselessFuncs);
    }

    public void passSideEffect(Function func) {
        func.setHasSideEffect(true);
        for (Function f : func.getCallerList()) {
            if (!f.isHasSideEffect()) {
                passSideEffect(f);
            }
        }
    }


}
