package lightllr;

public class Irprinter {
    public static String printAsOp(Value v, boolean printTy)
    {
        String op_ir = "";
        if(printTy) {
            op_ir += v.getIrType().print();
            op_ir += " ";
        }
        if (v instanceof GlobalVariable) {
            op_ir += "@"+ v.getName();
        } else if (v instanceof Function) {
            op_ir += "@"+ v.getName();
        } else if (v instanceof Constant) {
            op_ir += v.print();
        } else {
            op_ir += "%" + v.getName();
        }
        return op_ir;
    }

    public static String printCmpType(CmpInstr.CmpOp op)
    {
        switch (op) {
            case GE -> {
                return "sge";
            }
            case GT -> {
                return "sgt";
            }
            case LE -> {
                return "sle";
            }
            case LT -> {
                return "slt";
            }
            case EQ -> {
                return "eq";
            }
            case NE -> {
                return "ne";
            }
            default -> {
            }
        }
        return "wrong cmpop";
    }
}
