package lightllr;

public class Constant extends User {
    public Constant(IrType ty, String name, int numOps) {
        super(ty, name, numOps);
    }

    public Constant(IrType ty, int numOps) {
        super(ty, "", numOps);
    }


}