package lightllr;

public class Argument extends Value {
    private Function parent;
    private int argNo;

    public Argument(IrType ty, String name, Function f, int argNo) {
        super(ty, name);
        parent = f;
        this.argNo = argNo;
    }

    public Argument(IrType ty, String name, Function f) {
        super(ty, name);
        parent = f;
        this.argNo = 0;
    }

    public Argument(IrType ty, Function f, int argNo) {
        super(ty);
        parent = f;
        this.argNo = argNo;
    }

    public Argument(IrType ty, String name, int argNo) {
        super(ty, name);
        parent = null;
        this.argNo = argNo;
    }

    public Argument(IrType ty, int argNo) {
        super(ty);
        parent = null;
        this.argNo = argNo;
    }

    public Argument(IrType ty, String name) {
        super(ty, name);
        parent = null;
        this.argNo = 0;
    }

    public Argument(IrType ty, Function f) {
        super(ty);
        parent = f;
        this.argNo = 0;
    }

    public Argument(IrType ty) {
        super(ty);
        parent = null;
        this.argNo = 0;
    }

    public int getArgNo() { return argNo; }
    public Function getParent() { return parent; }

    public String print() {
        String arg_ir = "";
        arg_ir += this.getIrType().print();
        arg_ir += " %";
        arg_ir += this.getName();
        return arg_ir;
    }

}
