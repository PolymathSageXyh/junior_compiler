package lightllr;

import java.util.ArrayList;
import java.util.Objects;

public class Value {
    IrType irType;
    ArrayList<Use> useList;   // who use this value
    String name;

    public Value(IrType irType) {
        this.irType = irType;
        useList = new ArrayList<>();
        name = "";
    }

    public Value(IrType irType, String name) {
        this.irType = irType;
        useList = new ArrayList<>();
        this.name = name;
    }

    public void addUse(Value val, int argNo) {
        useList.add(new Use(val, argNo));
    }

    public boolean setName(String name) {
        if (Objects.equals(this.name, ""))
        {
            this.name=name;
            return true;
        }
        return false;
    }

    public String getName() { return name; }

    public IrType getIrType() {
        return irType;
    }

    public String print() { return ""; }

    public void replace_all_use_with(Value new_val)
    {
        for (Use use : useList) {
            User val = ((User)use.getVal());
            //assert(val && "new_val is not a user");
            val.setOperand(use.getArgNums(), new_val);
        }
    }

    public void remove_use(Value val) {
        for (int i = 0; i < useList.size(); i++) {
            if ((useList.get(i).getVal()) == val) {
                useList.remove(i);
                i--;
            }
        }
    }



}
