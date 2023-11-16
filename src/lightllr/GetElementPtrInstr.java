package lightllr;

import java.util.ArrayList;

public class GetElementPtrInstr extends Instruction {
    private IrType elementTy;
    private GetElementPtrInstr(Value ptr, ArrayList<Value> idxs, BasicBlock bb) {
        super(PointerType.get(get_element_type(ptr, idxs)), OpID.getelementptr, 1 + idxs.size(), bb);
        setOperand(0, ptr);
        for (int i = 0; i < idxs.size(); i++) {
            setOperand(i + 1, idxs.get(i));
        }
        elementTy = get_element_type(ptr, idxs);
    }

    public GetElementPtrInstr(BasicBlock bb, Value ptr, ArrayList<Value> idxs) {
        super(PointerType.get(get_element_type(ptr, idxs)), OpID.getelementptr, 1 + idxs.size());
        setOperand(0, ptr);
        for (int i = 0; i < idxs.size(); i++) {
            setOperand(i + 1, idxs.get(i));
        }
        elementTy = get_element_type(ptr, idxs);
        this.set_parent(bb);
    }



    public static IrType get_element_type(Value ptr, ArrayList<Value> idxs)
    {

        IrType ty = ptr.getIrType().getPointerElementType();
        //assert("GetElementPtrInst ptr is wrong type" && (ty.isArrayType() ||ty.isIntegerType()));
        if (ty.isArrayType())
        {
            ArrayType arr_ty = ((ArrayType)ty);
            for (int i = 1; i < idxs.size(); i++) {
                ty = arr_ty.getElementType();
                if (i < idxs.size() - 1) {
                    assert(ty.isArrayType());
                }
                if (ty.isArrayType()) {
                    arr_ty = ((ArrayType)ty);
                }
            }
        }
        return ty;
    }

    IrType get_element_type() {
        return elementTy;
    }

    public static GetElementPtrInstr createGep(Value ptr, ArrayList<Value> idxs, BasicBlock bb) {
        return new GetElementPtrInstr(ptr, idxs, bb);
    }

    public String print() {
        String instr_ir = "";
        instr_ir += "%";
        instr_ir += this.getName();
        instr_ir += " = ";
        instr_ir += this.getInstrOpName();
        instr_ir += " ";
        //assert(this->get_operand(0)->get_type()->is_pointer_type());
        instr_ir += this.getOperand(0).getIrType().getPointerElementType().print();
        instr_ir += ", ";
        for (int i = 0; i < this.getNumOps(); i++) {
            if( i > 0 ) instr_ir += ", ";
            instr_ir += this.getOperand(i).getIrType().print();
            instr_ir += " ";
            instr_ir += Irprinter.printAsOp(this.getOperand(i), false);
        }
        return instr_ir;
    }
}
