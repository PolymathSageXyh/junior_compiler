package lightllr.optimization;

import lightllr.*;
import lightllr.Module;
import lightllr.IrBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RemovePhi extends Pass {

    public RemovePhi(Module module) {
        super(module);
    }

    private void removePhiForEachBlock(BasicBlock basicBlock) {
        if (!(basicBlock.getInstrList().get(0) instanceof PhiInstr)) {
            return;
        }
        HashMap<BasicBlock, Pcopy> PcopyMap = new HashMap<>();
        for (BasicBlock bb : basicBlock.getPrebbs()) {
            PcopyMap.put(bb, new Pcopy());
        }
        ArrayList<Instruction> tmp = basicBlock.getInstrList();
        for (int k = 0; k < tmp.size(); k++) {
            if (!(tmp.get(k) instanceof PhiInstr)) {
                break;
            }
            PhiInstr phiInstr = ((PhiInstr) tmp.get(k));
            for (int i = 0; i < phiInstr.getNumOps() / 2; i++) {
                PcopyMap.get(((BasicBlock) phiInstr.getOperand(2*i+1))).addPair(phiInstr, phiInstr.getOperand(2*i));
            }
            tmp.remove(k);
            k--;
        }
        for (int i = 0; i < basicBlock.getPrebbs().size(); i++) {
            BasicBlock bb = basicBlock.getPrebbs().get(i);
            processPcopy(bb, PcopyMap.get(bb), basicBlock, i);
        }
    }

    private void processPcopy(BasicBlock bb, Pcopy pcopy, BasicBlock succ, int idx) {
        HashMap<Value, Value> map = pcopy.map; //<dst,src>
        ArrayList<MoveInstr> moves = new ArrayList<>();
        while (!map.isEmpty()) {
            HashSet<Value> rmKeys = new HashSet();
            for (Value v : map.keySet()) {
                if (!map.values().contains(v)) {
                    rmKeys.add(v);
                } //被赋值以后不会用于赋值
            }
            if (rmKeys.isEmpty()) {
                //存在环！那就直接把环断开就好了
                Value cirPint = detectCircle(map);
                Value oriSrc = map.get(cirPint);
                if (oriSrc.equals(cirPint)) {
                    map.remove(cirPint);
                } else {
                    VirValue tmpReg = new VirValue(cirPint.getIrType());
                    MoveInstr mm = MoveInstr.createMove(oriSrc, tmpReg, bb, module);
                    System.out.println(mm.print());
                    moves.add(mm);
                    map.put(cirPint, tmpReg);
                }
            } else {
                for (Value v : rmKeys) {
                    Value gg = map.get(v);
                    MoveInstr mm = MoveInstr.createMove(map.get(v), v, bb, module);
                    System.out.println(mm.print());
                    moves.add(mm);
                    map.remove(v);
                }
            }
        }
        if (!moves.isEmpty()) {
            if (bb.getSuccbbs().size() == 1) {
                for (MoveInstr moveInstr : moves) {
                    int index = bb.getInstrList().size()-1;
                    bb.getInstrList().add(index, moveInstr);
                }
            } else {
                BasicBlock mid = new BasicBlock(module, "", bb.getParent());
                for (MoveInstr moveInstr : moves) {
                    mid.getInstrList().add(moveInstr);
                }
                BranchInstr.create_br(succ, mid);
                assert bb.getInstrList().get(bb.getInstrList().size()-1) instanceof BranchInstr;

                BranchInstr branchInstr = (BranchInstr) bb.getInstrList().get(bb.getInstrList().size()-1);
                if (branchInstr.getOperand(1).equals(succ)) {
                    branchInstr.setOperand(1, mid);
                } else {
                    branchInstr.setOperand(2, mid);
                }
                succ.getPrebbs().set(idx, mid);
                succ.getPrebbs().remove(succ.getPrebbs().size() - 1);
                bb.getSuccbbs().remove(succ);
                bb.getSuccbbs().add(mid);
                mid.getPrebbs().add(bb);
            }
        }

    }

    public Value detectCircle(HashMap<Value, Value> G) {
        HashSet<Value> visited = new HashSet<>();
        Value ori;
        ori = null;
        for (Value v : G.keySet()) {
            ori = v;
            break;
        }
        if (ori == null) {
            throw new RuntimeException("ori is null");
        }
        while (!visited.contains(ori)) {
            visited.add(ori);
            ori = G.get(ori);
        }
        return ori;
    }

    @Override
    public void run() {
        for (Function func : module.getFunctions()) {
            for (int i = 0; i < func.getBasicBlocks().size(); i++) {
                removePhiForEachBlock(func.getBasicBlocks().get(i));
            }
        }
    }

    public class Pcopy {
        HashMap<Value, Value> map = new HashMap<>();

        public void addPair(Value lhs, Value rhs) {
            map.put(lhs, rhs);
        }
    }
}

