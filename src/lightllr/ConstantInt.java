package lightllr;

public class ConstantInt extends Constant {
    private int truth; //常数真值
    public ConstantInt(IrType ty, int val) {
        super(ty, "", 0);
        this.truth = val;
    }

    public static int getValue(ConstantInt constVal) { return constVal.getTruth(); }
    public int getTruth() { return truth; }
    public static ConstantInt get(int val, Module m) {
        return new ConstantInt(IrType.getInt32Type(m), val);
    }
    public static ConstantInt get(boolean val, Module m) {
        return new ConstantInt(IrType.getInt1Type(m), val ? 1 : 0);
    }

    @Override
    public String print() {
        String const_ir = "";
        IrType ty = this.getIrType();
        if (ty.isIntegerType() && ((IntegerType)(ty)).getNumBits() == 1) {
            const_ir += (this.getTruth() == 0) ? "false" : "true";
        }
        else {
            const_ir += Integer.toString(this.getTruth());
        }
        return const_ir;
    }
}
