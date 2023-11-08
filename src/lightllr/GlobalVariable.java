package lightllr;

public class GlobalVariable extends User{
    private boolean isConst;
    private Constant initVal;

    public GlobalVariable(String name, Module m, IrType ty, boolean isConst, Constant initVal) {
        super(ty, name, initVal != null ? 1 : 0);
        m.addGlobalVariable(this);
        if (initVal != null) {
            this.setOperand(0, initVal);
        }
        this.isConst = isConst;
        this.initVal = initVal;
    }

    public boolean isConst() { return isConst; }

    public Constant getInitVal() { return initVal; }

    public static GlobalVariable create(String name, Module m, IrType ty, boolean isConst,
                                  Constant initVal) {
        return new GlobalVariable(name, m, PointerType.get(ty), isConst, initVal);
    }

    public String print() {
        String global_val_ir = "";
        global_val_ir += Irprinter.printAsOp(this, false);
        global_val_ir += " = ";
        global_val_ir += (this.isConst() ? "constant " : "global ");
        global_val_ir += this.getIrType().getPointerElementType().print();
        global_val_ir += " ";
        global_val_ir += this.getInitVal().print();
        return global_val_ir;
    }

}
