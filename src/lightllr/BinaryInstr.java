package lightllr;

public class BinaryInstr extends Instruction {
    public BinaryInstr(IrType ty, OpID id, Value v1, Value v2,
                       BasicBlock bb) {
        super(ty, id, 2, bb);
        setOperand(0, v1);
        setOperand(1, v2);
    }

    public static BinaryInstr createAdd(Value v1, Value v2, BasicBlock bb, Module m) {
        return new BinaryInstr(IrType.getInt32Type(m), OpID.add, v1, v2, bb);
    }

    public static BinaryInstr createSub(Value v1, Value v2, BasicBlock bb, Module m) {
        return new BinaryInstr(IrType.getInt32Type(m), OpID.sub, v1, v2, bb);
    }

    public static BinaryInstr createMul(Value v1, Value v2, BasicBlock bb, Module m) {
        return new BinaryInstr(IrType.getInt32Type(m), OpID.mul, v1, v2, bb);
    }

    public static BinaryInstr createSdiv(Value v1, Value v2, BasicBlock bb, Module m) {
        return new BinaryInstr(IrType.getInt32Type(m), OpID.sdiv, v1, v2, bb);
    }

    public static BinaryInstr createAnd(Value v1, Value v2, BasicBlock bb, Module m) {
        return new BinaryInstr(IrType.getInt1Type(m), OpID.and, v1, v2, bb);
    }

    public static BinaryInstr createOr(Value v1, Value v2, BasicBlock bb, Module m) {
        return new BinaryInstr(IrType.getInt1Type(m), OpID.or, v1, v2, bb);
    }

    public static BinaryInstr createXor(Value v1, Value v2, BasicBlock bb, Module m) {
        return new BinaryInstr(IrType.getInt1Type(m), OpID.xor, v1, v2, bb);
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
        instr_ir += ", ";
        if (IrType.isEqType(this.getOperand(0).getIrType(), this.getOperand(1).getIrType())) {
            instr_ir += Irprinter.printAsOp(this.getOperand(1), false);
        } else{
            instr_ir += Irprinter.printAsOp(this.getOperand(1), true);
        }
        return instr_ir;
    }


}
