package lightllr;

import java.util.ArrayList;

import static lightllr.Instruction.OpID.br;
import static lightllr.Instruction.OpID.ret;

public class BasicBlock extends Value {
    private ArrayList<BasicBlock> prebbs;
    private ArrayList<BasicBlock> succbbs;
    private ArrayList<Instruction> instrList;
    private Function parent;
    private boolean hasBr;

    public BasicBlock(Module m, String name, Function parent) {
        super(IrType.getLabelType(m), name);
        this.parent = parent;
        parent.addBasicBlock(this);
        prebbs = new ArrayList<>();
        succbbs = new ArrayList<>();
        instrList  = new ArrayList<>();
        hasBr = false;
    }

    public BasicBlock(Module m, Function parent) {
        super(IrType.getLabelType(m));
        this.parent = parent;
        parent.addBasicBlock(this);
        prebbs = new ArrayList<>();
        succbbs = new ArrayList<>();
        instrList  = new ArrayList<>();
        hasBr = false;
    }

    public void setHasBr(boolean hasBr) { this.hasBr = hasBr; }

    public boolean getHasBr() { return hasBr; }

    public static BasicBlock create(Module m, String name ,
                              Function parent) {
        String prefix = name.length() == 0 ? "" : "label_";
        return new BasicBlock(m, prefix + name, parent);
    }

    public void addInstruction(Instruction instr) {
        instrList.add(instr);
    }

    public void addInstrBegin(Instruction instr) {
        instrList.add(0, instr);
    }

    public void deleteInstr(Instruction instr) {
        instrList.remove(instr);
        instr.remove_use_of_ops();
    }

    public ArrayList<Instruction> getInstrList() { return instrList; }

    public boolean isInstrListEmpty() { return instrList.isEmpty(); }

    public int getNumOfInstr() { return instrList.size(); }

    public Module getModule() {
        return getParent().getParent();
    }

    public Function getParent() {
        return parent;
    }

    public ArrayList<BasicBlock> getPrebbs() { return prebbs; }

    public ArrayList<BasicBlock> getSuccbbs() { return succbbs; }

    void addPreBasicBlock(BasicBlock bb) { prebbs.add(bb); }
    void addSuccBasicBlock(BasicBlock bb) { succbbs.add(bb); }

    void removePreBasicBlock(BasicBlock bb) { prebbs.remove(bb); }
    void removeSuccBasicBlock(BasicBlock bb) { succbbs.remove(bb); }

    public Instruction get_terminator() {
        if (instrList.isEmpty()){
            return null;
        }
        if (instrList.get(instrList.size()-1).getInstrType() == ret
            ||instrList.get(instrList.size()-1).getInstrType() == br) {
            return instrList.get(instrList.size()-1);
        }
        return null;
    }

    public void eraseFromParent() {
        this.getParent().remove(this);
    }

    public String print() {
        String bb_ir = "";
        bb_ir += this.getName();
        bb_ir += ":";
        // print prebb
        if(!this.prebbs.isEmpty())
        {
            bb_ir += "                                                ; preds = ";
        }
        for (BasicBlock bb : this.prebbs) {
            if( bb != this.prebbs.get(0)) bb_ir += ", ";
            bb_ir += Irprinter.printAsOp(bb, false);
        }
        if (this.getParent() == null) {
            bb_ir += "\n";
            bb_ir += "; Error: Block without parent!";
        }
        bb_ir += "\n";
        for (Instruction instr : this.instrList) {
            bb_ir += "  ";
            bb_ir += instr.print();
            bb_ir += "\n";
        }
        return bb_ir;
    }
}
