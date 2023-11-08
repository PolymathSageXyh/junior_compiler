package lightllr;

public class ZextInstr extends Instruction {

    private IrType destTy;
    private ZextInstr (OpID op, Value val, IrType ty, BasicBlock bb) {
        super(ty, op, 1, bb);
        this.destTy = ty;
        setOperand(0, val);
    }

    public static ZextInstr createZext(Value val, IrType ty, BasicBlock bb) {
        return new ZextInstr(OpID.zext, val, ty, bb);
    }

    public IrType getDestType() {
        return this.destTy;
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
        instr_ir += Irprinter.printAsOp(this.getOperand(0), false);
        instr_ir += " to ";
        instr_ir += this.getDestType().print();
        return instr_ir;
    }


}
