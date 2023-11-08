package lightllr;

public class BranchInstr extends Instruction {

        private BranchInstr(Value cond, BasicBlock if_true, BasicBlock if_false, BasicBlock bb) {
            super(IrType.getVoidType(if_true.getModule()), Instruction.OpID.br, 3, bb);
            setOperand(0, cond);
            setOperand(1, if_true);
            setOperand(2, if_false);
        }
        private BranchInstr(BasicBlock if_true, BasicBlock bb) {
            super(IrType.getVoidType(if_true.getModule()), Instruction.OpID.br, 1, bb);
            setOperand(0, if_true);
        }

        public static BranchInstr create_cond_br(Value cond, BasicBlock if_true, BasicBlock if_false,
            BasicBlock bb) {
            if_true.addPreBasicBlock(bb);
            if_false.addPreBasicBlock(bb);
            bb.addSuccBasicBlock(if_false);
            bb.addSuccBasicBlock(if_true);
            return new BranchInstr(cond, if_true, if_false, bb);
        }
        public static BranchInstr create_br(BasicBlock if_true, BasicBlock bb) {
            if_true.addPreBasicBlock(bb);
            bb.addSuccBasicBlock(if_true);

            return new BranchInstr(if_true, bb);
        }

        public boolean is_cond_br() {
            return getNumOps() == 3;
        }

        public String print() {
            String instr_ir = "";
            instr_ir += this.getInstrOpName();
            instr_ir += " ";
            // instr_ir += this->get_operand(0)->get_type()->print();
            instr_ir += Irprinter.printAsOp(this.getOperand(0), true);
            if(is_cond_br()) {
                instr_ir += ", ";
                instr_ir += Irprinter.printAsOp(this.getOperand(1), true);
                instr_ir += ", ";
                instr_ir += Irprinter.printAsOp(this.getOperand(2), true);
            }
            return instr_ir;
        }
}
