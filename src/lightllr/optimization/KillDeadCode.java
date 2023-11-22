package lightllr.optimization;

import lightllr.*;
import lightllr.Module;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

public class KillDeadCode extends Pass {
    private HashSet<Instruction> visited = new HashSet<>();
    private HashMap<Instruction, Boolean> isUseful = new HashMap<>();

    public KillDeadCode(Module module) {
        super(module);
    }

    @Override
    public void run() {
        for (Function func : module.getFunctions()) {
            if (!func.isLibrary()) {
                isUseful.clear();
                visited.clear();
                for (BasicBlock bb : func.getBasicBlocks()) {
                    for (Instruction instr : bb.getInstrList()) {
                        isUseful.put(instr, false);
                    }
                }
                deleteUselessInstrs(func);
            }
        }
    }

    public boolean isMatter(Instruction instr) {
        return instr instanceof StoreInstr ||
                instr instanceof BranchInstr ||
                instr instanceof ReturnInstr ||
                (instr instanceof CallInstr && ((Function)instr.getOperand(0)).isHasSideEffect());
    }

    public void BFS(Instruction instr) {
        ArrayDeque<Instruction> queue = new ArrayDeque<>();
        queue.add(instr);
        visited.add(instr);
        while (!queue.isEmpty()) {
            Instruction item = queue.pollFirst();
            isUseful.put(item, true);
            for (Value v : item.getOperands()) {
                if (v instanceof Instruction && !visited.contains((Instruction) v) && !isMatter((Instruction) v)) {
                    visited.add((Instruction) v);
                    queue.add((Instruction) v);
                }
            }
        }
    }

    public void deleteUselessInstrs(Function func) {
        for (BasicBlock bb : func.getBasicBlocks()) {
            for (Instruction instr : bb.getInstrList()) {
                if (isMatter(instr)) {
                    BFS(instr);
                }
            }
        }
        for (BasicBlock bb : func.getBasicBlocks()) {
            for (int i = 0; i < bb.getInstrList().size(); i++) {
                if (!isUseful.get(bb.getInstrList().get(i))) {
                    bb.deleteInstr(bb.getInstrList().get(i));
                    i--;
                }
            }
        }
    }

}
