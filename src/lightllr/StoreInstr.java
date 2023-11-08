package lightllr;

public class StoreInstr extends Instruction {
    public StoreInstr(Value val, Value ptr, BasicBlock bb) {
        super(IrType.getVoidType(bb.getModule()), OpID.store, 2, bb);
        setOperand(0, val);
        setOperand(1, ptr);
    }

    public static StoreInstr createStore(Value val, Value ptr, BasicBlock bb) {
        return new StoreInstr(val, ptr, bb);
    }

    public Value getRval() { return this.getOperand(0); }
    public Value getLval() { return this.getOperand(1); }

    public String print() {
        String instr_ir = "";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        instr_ir += this.getOperand(0).getIrType().print();
        instr_ir += " ";
        instr_ir += Irprinter.printAsOp(this.getOperand(0), false);
        instr_ir += ", ";
        instr_ir += Irprinter.printAsOp(this.getOperand(1), true);
        return instr_ir;
    }
}
