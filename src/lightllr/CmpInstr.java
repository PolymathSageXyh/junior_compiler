package lightllr;

public class CmpInstr extends Instruction {
    public enum CmpOp
    {
        EQ, // ==
        NE, // !=
        GT, // >
        GE, // >=
        LT, // <
        LE,  // <=
    }
    private CmpOp cmpOp;

    public CmpInstr(IrType ty, CmpOp op, Value lhs, Value rhs,
                    BasicBlock bb) {
        super(ty, OpID.cmp, 2, bb);
        this.cmpOp = op;
        setOperand(0, lhs);
        setOperand(1, rhs);
    }

    public static CmpInstr createCmp(CmpOp op, Value lhs, Value rhs,
                            BasicBlock bb, Module m) {
        return new CmpInstr(IrType.getInt1Type(m), op, lhs, rhs, bb);
    }

    public String print() {
        String instr_ir = "";
        instr_ir += "%";
        instr_ir += this.getName();
        instr_ir += " = ";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        instr_ir += Irprinter.printCmpType(this.cmpOp);
        instr_ir += " ";
        instr_ir += this.getOperand(0).getIrType().print();
        instr_ir += " ";
        instr_ir += Irprinter.printAsOp(this.getOperand(0), false);
        instr_ir += ", ";
        if (IrType.isEqType(this.getOperand(0).getIrType(), this.getOperand(1).getIrType())) {
            instr_ir += Irprinter.printAsOp(this.getOperand(1), false);
        } else {
            instr_ir += Irprinter.printAsOp(this.getOperand(1), true);
        }
        return instr_ir;
    }
}
