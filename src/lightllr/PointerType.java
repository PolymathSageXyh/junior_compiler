package lightllr;

public class PointerType extends IrType{
    private IrType contains;
    public PointerType(IrType contains) {
        super(TypeID.PointerTyID, contains.getModule());
        this.contains = contains;
    }

    public IrType getElementType() {return contains; }

    public static PointerType get(IrType contains) {
        return contains.getModule().getPointerType(contains);
    }
}
