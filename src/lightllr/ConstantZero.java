package lightllr;

public class ConstantZero extends Constant {

    private ConstantZero(IrType ty) {
        super(ty, "", 0);
    }
    public static ConstantZero get(IrType ty, Module m)
    {
        return new ConstantZero(ty);
    }
    @Override
    public String print() {
        return "zeroinitializer";
    }
}
