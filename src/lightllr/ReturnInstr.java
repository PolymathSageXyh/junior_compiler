package lightllr;

public class ReturnInstr extends Instruction {

    public ReturnInstr(Value val, BasicBlock bb) {
        super(IrType.getVoidType(bb.getModule()), OpID.ret, 1, bb);
        setOperand(0, val);
    }

    public ReturnInstr(BasicBlock bb) {
        super(IrType.getVoidType(bb.getModule()), OpID.ret, 0, bb);
    }

    public static ReturnInstr createRet(Value val, BasicBlock bb) {
        return new ReturnInstr(val, bb);
    }

    public static ReturnInstr createVoidRet(BasicBlock bb) {
        return new ReturnInstr(bb);
    }

    public boolean isVoidRet() {
        return getNumOps() == 0;
    }

    public String print() {
        String instr_ir = "";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        if (!isVoidRet()) {
            instr_ir += this.getOperand(0).getIrType().print();
            instr_ir += " ";
            instr_ir += Irprinter.printAsOp(this.getOperand(0), false);
        } else {
            instr_ir += "void";
        }
        return instr_ir;
    }

}
