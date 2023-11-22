package lightllr;

public class IntegerType extends IrType {
    private int numBits;
    public IntegerType(int numBits ,Module module) {
        super(TypeID.IntegerTyID, module);
        this.numBits = numBits;
    }

    public IntegerType get(int numBits, Module module) {
        return new IntegerType(numBits, module);
    }

    public int getNumBits() { return numBits; }

    public boolean isI1() {
        return numBits == 1;
    }

    public boolean isI8() {
        return numBits == 8;
    }


}
