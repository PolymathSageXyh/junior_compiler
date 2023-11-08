package lightllr;

public class IrType {
    enum TypeID {
        VoidTyID,         // Void
        LabelTyID,        // Labels, e.g., BasicBlock
        IntegerTyID,      // Integers, include 32 bits and 1 bit
        FunctionTyID,     // Functions
        ArrayTyID,        // Arrays
        PointerTyID,      // Pointer
    }
    private TypeID tid;
    private Module module;

    public IrType(TypeID tid, Module module) {
        this.tid = tid;
        this.module = module;
    }

    public TypeID getTypeId() { return tid; }

    public Module getModule() { return module; }

    public boolean isVoidType() { return getTypeId() == TypeID.VoidTyID; }

    public boolean isLabelType()  { return getTypeId() == TypeID.LabelTyID; }

    public boolean isIntegerType() { return getTypeId() == TypeID.IntegerTyID; }

    public boolean isFunctionType()  { return getTypeId() == TypeID.FunctionTyID; }

    public boolean isArrayType()  { return getTypeId() == TypeID.ArrayTyID; }

    public boolean isPointerType()  { return getTypeId() == TypeID.PointerTyID; }

    public static boolean isEqType(IrType ty1, IrType ty2) {
        return ty1 == ty2;
    }

    public static IrType getVoidType(Module m) {
        return m.getVoidType();
    }

    public static IrType getLabelType(Module m) {
        return m.getLabelType();
    }

    public static IntegerType getInt1Type(Module m) {
        return m.getInt1Type();
    }

    public static IntegerType getInt32Type(Module m) {
        return m.getInt32Type();
    }

    public static PointerType getInt32PtrType(Module m) {
        return m.getInt32PtrType();
    }

    public static PointerType getPointerType(IrType contains) {
        return PointerType.get(contains);
    }

    public static ArrayType getArrayType(IrType contains, int numOfElements) {
        return ArrayType.get(contains, numOfElements);
    }

    public IrType getPointerElementType() {
        if(this.isPointerType()) {
            return ((PointerType)this).getElementType();
        } else {
            return null;
        }
    }

    public IrType getArrayElementType() {
        if(this.isArrayType()) {
            return ((ArrayType)this).getElementType();
        } else {
            return null;
        }
    }

    public int getSize() {
        if (this.isIntegerType()) {
            int bits = ((IntegerType)this).getNumBits() / 8;
            return bits > 0 ? bits : 1;
        }
        if (this.isArrayType()) {
            int elementSize = ((ArrayType)this).getElementType().getSize();
            int numOfElements = ((ArrayType)this).getNumOfElements();
            return elementSize * numOfElements;
        }
        if (this.isPointerType()) {
            if (this.getPointerElementType().isArrayType()) {
                return this.getPointerElementType().getSize();
            } else { return 4; }
        }
        return 0;
    }

    public String print(){
        String type_ir = "";
        switch (this.getTypeId()) {
            case VoidTyID:
                type_ir += "void";
                break;
            case LabelTyID:
                type_ir += "label";
                break;
            case IntegerTyID:
                type_ir += "i";
                type_ir += Integer.toString(((IntegerType)this).getNumBits());
                break;
            case FunctionTyID:
                type_ir += ((FunctionType)this).getReturnType().print();
                type_ir += " (";
                for(int i = 0; i < ((FunctionType)this).getNumOfArgs(); i++) {
                    if(i > 0) type_ir += ", ";
                type_ir += ((FunctionType)this).getParamType(i).print();
            }
            type_ir += ")";
            break;
            case PointerTyID:
                type_ir += this.getPointerElementType().print();
                type_ir += "*";
                break;
            case ArrayTyID:
                type_ir += "[";
                type_ir += Integer.toString(((ArrayType)this).getNumOfElements());
                type_ir += " x ";
                type_ir += ((ArrayType)this).getElementType().print();
                type_ir += "]";
                break;
            default:
                break;
        }
        return type_ir;
    }


}
