package lightllr;

public class AllocaInstr extends Instruction {
    private IrType allocaTy;
    public AllocaInstr(IrType ty, BasicBlock bb) {
        super(PointerType.get(ty), OpID.alloca, 0, bb);
        allocaTy = ty;
    }

    public static AllocaInstr createAlloca(IrType ty, BasicBlock bb) {
        return new AllocaInstr(ty, bb);
    }

    public IrType getAllocaTy() { return allocaTy; }

    public String print() {
        String instr_ir = "";
        instr_ir += "%";
        instr_ir += this.getName();
        instr_ir += " = ";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        instr_ir += getAllocaTy().print();
        return instr_ir;
    }
}
