package lightllr;

import java.util.ArrayList;

public class FunctionType extends IrType {
    private IrType result;
    ArrayList<IrType> args;

    public FunctionType(IrType result, ArrayList<IrType> params) {
        super(TypeID.FunctionTyID, null);
        this.result = result;
        this.args = params;
    }

    public static boolean isValidReturnType(IrType ty) {
        return ty.isIntegerType() || ty.isVoidType();
    }
    public static boolean isValidArgumentType(IrType ty) {
        return ty.isIntegerType() || ty.isPointerType();
    }

    public static FunctionType get(IrType result, ArrayList<IrType> params) {
        return new FunctionType(result, params);
    }

    public int getNumOfArgs() { return args.size(); }

    public IrType getParamType(int i) {
        return args.get(i);
    }

    public IrType getReturnType() {
        return result;
    }

}
