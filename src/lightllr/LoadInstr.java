package lightllr;

public class LoadInstr extends Instruction {
    public LoadInstr(IrType ty, Value ptr, BasicBlock bb) {
        super(ty, OpID.load, 1, bb);
        setOperand(0, ptr);
    }

    public IrType getLoadType() {
        return ((PointerType)(getOperand(0).getIrType())).getElementType();
    }

    public static LoadInstr createLoad(IrType ty, Value ptr, BasicBlock bb) {
        return new LoadInstr(ty, ptr, bb);
    }

    public Value getLval() { return this.getOperand(0); }

    public String print() {
        String instr_ir = "";
        instr_ir += "%";
        instr_ir += this.getName();
        instr_ir += " = ";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        assert(this.getOperand(0).getIrType().isPointerType());
        instr_ir += this.getOperand(0).getIrType().getPointerElementType().print();
        instr_ir += ",";
        instr_ir += " ";
        instr_ir += Irprinter.printAsOp(this.getOperand(0), true);
        return instr_ir;
    }

}
