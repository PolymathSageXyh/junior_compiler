package lightllr;

import java.util.ArrayList;

public class User extends Value {
    private ArrayList<Value> operands;   // operands of this value
    private int numOps;
    public User(IrType ty, String name , int num_ops) {
        super(ty, name);
        this.numOps = num_ops;
        operands = new ArrayList<>();
        for (int i = 0; i < num_ops; i++) {
            operands.add(null);
        }
    }

    public ArrayList<Value> getOperands() { return operands; }

    public Value getOperand(int i) { return operands.get(i); }

    public void setOperand(int i , Value val) {
        operands.set(i, val);
        val.addUse(this, i);
    }

    public void addOperand(Value val) {
        operands.add(val);
        val.addUse(this, numOps);
        numOps++;
    }

    public int getNumOps() {
        return numOps;
    }

    public void remove_use_of_ops()
    {
        for (Value op : operands) {
            op.remove_use(this);
        }
    }

    public void remove_operands(int index1,int index2){
        for(int i=index1;i<=index2;i++){
            operands.get(i).remove_use(this);
        }
        ArrayList<Value> tmp = new ArrayList<>();
        for(int i = 0; i < operands.size(); i++){
            if (i < index1 || i > index2) {
                tmp.add(operands.get(i));
            }
        }
        operands.clear();
        operands.addAll(tmp);
        // std::cout<<operands_.size()<<std::endl;
        numOps =operands.size();
    }

}
