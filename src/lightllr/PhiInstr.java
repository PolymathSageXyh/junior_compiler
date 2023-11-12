package lightllr;

import java.util.ArrayList;

public class PhiInstr extends Instruction {
    private Value l_val;
    private PhiInstr(OpID op, ArrayList<Value> vals, ArrayList<BasicBlock> valBBs, IrType ty, BasicBlock bb) {
        super(ty, op, 2* vals.size());
        for ( int i = 0; i < vals.size(); i++) {
            this.setOperand(2*i, vals.get(i));
            this.setOperand(2*i+1, valBBs.get(i));
        }
        this.set_parent(bb);

    }
    private PhiInstr (IrType ty, OpID op, int numOps, BasicBlock bb) {
        super(ty, op, numOps, bb);

    }
    
    public static PhiInstr createPhi(IrType ty, BasicBlock bb) {
        ArrayList<Value> vals = new ArrayList<>();
        ArrayList<BasicBlock> valBBs = new ArrayList<>();
        return new PhiInstr (OpID.phi, vals, valBBs, ty, bb);
    }
    public Value getLval() { return l_val; }
    public void setLval(Value l_val) { this.l_val = l_val; }
    
    public void addPhiPairOperand(Value val, Value pre_bb) {
        this.addOperand(val);
        this.addOperand(pre_bb);
    }

    public String print() {
        String instr_ir = "";
        instr_ir += "%";
        instr_ir += this.getName();
        instr_ir += " = ";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        instr_ir += this.getOperand(0).getIrType().print();
        instr_ir += " ";
        for (int i = 0; i < this.getNumOps()/2; i++)
        {
            if( i > 0 )
                instr_ir += ", ";
            instr_ir += "[ ";
            instr_ir += Irprinter.printAsOp(this.getOperand(2*i), false);
            instr_ir += ", ";
            instr_ir += Irprinter.printAsOp(this.getOperand(2*i+1), false);
            instr_ir += " ]";
        }
        if ( this.getNumOps()/2 < this.getParent().getPrebbs().size()) {
            for (BasicBlock pre_bb : this.getParent().getPrebbs()) {
                if (!this.getOperands().contains(pre_bb)) {
                    instr_ir += ", [ undef, " +Irprinter.printAsOp(pre_bb, false)+" ]";
                }
            }
        }
        return instr_ir;
    }
    
    
}
