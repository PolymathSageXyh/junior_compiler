package lightllr;

public class ArrayType extends IrType {
    private IrType contains;
    private int numOfElements;
    private int totalInt;
    private int dim;

    public ArrayType(IrType contains, int numOfElements) {
        super(TypeID.ArrayTyID, contains.getModule());
        this.contains = contains;
        this.numOfElements = numOfElements;
        if (this.contains instanceof IntegerType) {
            this.totalInt = numOfElements;
            this.dim = 1;
        } else {
            // 否则说明是多维数组，递归下降计算int值数量
            this.totalInt = ((ArrayType)contains).getTotalInt() * this.numOfElements;
            this.dim = ((ArrayType)contains).getDim() + 1;
        }
    }

    public IrType getElementType() { return contains; }

    public int getNumOfElements() { return numOfElements; }

    public int getTotalInt() {
        return totalInt;
    }

    public int getDim() { return dim; }

    public static ArrayType get(IrType contains, int numOfElements) {
        return contains.getModule().getArrayType(contains, numOfElements);
    }


}
