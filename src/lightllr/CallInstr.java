package lightllr;

import java.util.ArrayList;

public class CallInstr extends Instruction {
    public CallInstr(Function func, ArrayList<Value> args, BasicBlock bb) {
        super(func.getReturnType(), OpID.call, args.size() + 1, bb);
        int numOps = args.size() + 1;
        setOperand(0, func);
        for (int i = 1; i< numOps; i++) {
            setOperand(i, args.get(i-1));
        }
    }
    public static CallInstr create(Function func, ArrayList<Value> args, BasicBlock bb) {
        return new CallInstr(func, args, bb);
    }

    public FunctionType getFunctionType() {
        return ((FunctionType) this.getOperand(0).getIrType());
    }

    public String print() {
        String instr_ir = "";
        if(!this.isVoid()) {
            instr_ir += "%";
            instr_ir += this.getName();
            instr_ir += " = ";
        }
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        instr_ir += this.getFunctionType().getReturnType().print();

        instr_ir += " ";
        //assert(dynamic_cast<Function *>(this->get_operand(0)) && "Wrong call operand function");
        instr_ir += Irprinter.printAsOp(this.getOperand(0), false);
        instr_ir += "(";
        for (int i = 1; i < this.getNumOps(); i++)
        {
            if(i > 1)
                instr_ir += ", ";
            instr_ir += this.getOperand(i).getIrType().print();
            instr_ir += " ";
            instr_ir += Irprinter.printAsOp(this.getOperand(i), false);
        }
        instr_ir += ")";
        return instr_ir;
    }
}
