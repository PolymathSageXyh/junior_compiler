package lightllr;

public class Use {
    private Value val;
    int argNums;     // the no. of operand, e.g., func(a, b), a is 0, b is 1
    public Use(Value val, int no) {
        this.val = val;
        this.argNums = no;
    }

    public Value getVal() { return val; }
    public int getArgNums() {return argNums; }

}
