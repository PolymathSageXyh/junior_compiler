package lightllr;

public class MoveInstr extends Instruction {
    private Value src;
    private Value dst;

    private MoveInstr(IrType ty, OpID id, Value src, Value dst, BasicBlock bb) {
        super(ty, id, 2);
        this.setOperand(0, src);
        this.setOperand(1, dst);
        this.src = src;
        this.dst = dst;
        this.set_parent(bb);
    }

    public static MoveInstr createMove(Value src, Value dst, BasicBlock bb, Module module) {
        return new MoveInstr(IrType.getVoidType(module), OpID.move, src, dst, bb);
    }

    public Value getSrc() {
        return src;
    }
    public Value getDst() {
        return dst;
    }

    public void setSrc(Value src) {
        this.src = src;
    }

    public String print() {
        String instr_ir = "";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        instr_ir += this.getOperand(1).getIrType().print();
        instr_ir += " ";
        instr_ir += Irprinter.printAsOp(this.getOperand(1), false);;
        instr_ir += " ";
        instr_ir += this.getOperand(0).getIrType().print();
        instr_ir += " ";
        instr_ir += Irprinter.printAsOp(this.getOperand(0), false);
        return instr_ir;
    }

//    public String print() {
//        String instr_ir = "";
//        instr_ir += "%";
//        instr_ir += this.getOperand(1).getName();
//        instr_ir += " = ";
//        instr_ir += "add";
//        instr_ir += " ";
//        instr_ir += this.getOperand(0).getIrType().print();
//        instr_ir += " ";
//        instr_ir += Irprinter.printAsOp(this.getOperand(0), false);
//        instr_ir += ", ";
//        instr_ir += Irprinter.printAsOp(ConstantInt.get(0,getModule()), false);
//        return instr_ir;
//    }

}
