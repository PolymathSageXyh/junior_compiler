package lightllr;

import java.util.ArrayList;

public class ConstantArray extends Constant{

    private ArrayList<Constant> constArray;

    public ConstantArray(ArrayType ty, ArrayList<Constant> val) {
        super(ty, "", val.size());
        for (int i = 0; i < val.size(); i++) {
            setOperand(i, val.get(i));
        }
        constArray = new ArrayList<>(val);    //深拷贝 浅拷贝问题
    }


    public Constant getElementValue(int index) {
        return constArray.get(index);
    }

    public int getSizeOfArray() { return constArray.size(); }

    public static ConstantArray get(ArrayType ty, ArrayList<Constant> val) {
        return new ConstantArray(ty, val);
    }

    @Override
    public String print() {
        String const_ir = "";
        //const_ir += this.getIrType().print();
        const_ir += " ";
        const_ir += "[";
        for (int i = 0 ; i < this.getSizeOfArray(); i++) {
            const_ir += getElementValue(i).getIrType().print();
            const_ir += " ";
            const_ir += getElementValue(i).print();
            const_ir += i == this.getSizeOfArray() - 1 ? "]" : ", ";
        }
        return const_ir;
    }
}
