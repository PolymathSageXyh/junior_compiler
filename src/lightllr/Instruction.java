package lightllr;

import paser.Mypair;

public class Instruction extends User{
    public enum OpID
    {
        // Terminator Instructions
        ret,
        br,
        // Standard binary operators
        add,
        sub,
        mul,
        sdiv,
        // Memory operators
        alloca,
        load,
        store,
        // Other operators
        cmp,
        phi,
        call,
        getelementptr,
        zext,
        and,
        or,
        xor,
    }

    private BasicBlock parent;
    private OpID opid;
    private int numOps;

    public Instruction(IrType ty, OpID id, int numOps,
                BasicBlock parent) {
        super(ty, "", numOps);
        this.parent = parent;
        this.opid = id;
        this.numOps = numOps;
        this.parent.addInstruction(this);
    }
    public Instruction(IrType ty, OpID id, int numOps) {
        super(ty, "", numOps);
        this.parent = null;
        this.opid = id;
        this.numOps = numOps;
    }

    public OpID getInstrType() { return opid; }

    public String getInstrOpName() {
        return switch (opid) {
            case ret -> "ret";
            case br -> "br";
            case add -> "add";
            case sub -> "sub";
            case mul -> "mul";
            case sdiv -> "sdiv";
            case alloca -> "alloca";
            case load -> "load";
            case store -> "store";
            case cmp -> "icmp";
            case phi -> "phi";
            case call -> "call";
            case getelementptr -> "getelementptr";
            case zext -> "zext";
            case and -> "and";
            case or -> "or";
            case xor -> "xor";
            default -> "";
        };
    }

    boolean isVoid() { return ((opid == OpID.ret) || (opid == OpID.br) || (opid == OpID.store) || (opid == OpID.call && this.getIrType().isVoidType())); }

    boolean isPhi() { return opid == OpID.phi; }
    boolean isStore() { return opid == OpID.store; }
    boolean isAlloca() { return opid == OpID.alloca; }
    boolean isRet() { return opid == OpID.ret; }
    boolean isLoad() { return opid == OpID.load; }
    boolean isBr() { return opid == OpID.br; }

    boolean isAdd() { return opid == OpID.add; }
    boolean isSub() { return opid == OpID.sub; }
    boolean isMul() { return opid == OpID.mul; }
    boolean isDiv() { return opid == OpID.sdiv; }

    boolean isCmp() { return opid == OpID.cmp; }

    boolean isCall() { return opid == OpID.call; }
    boolean isGep() { return opid == OpID.getelementptr; }
    boolean isZext() { return opid == OpID.zext; }


    boolean isBinary()
    {
        return (isAdd() || isSub() || isMul() || isDiv()) &&
                (getNumOps() == 2);
    }

    boolean isTerminator() { return isBr() || isRet(); }

    public BasicBlock getParent() { return parent; }
    void set_parent(BasicBlock parent) { this.parent = parent; }
    // Return the function this instruction belongs to.
    public Function getFunction() {
        return parent.getParent();
    }
    public Module getModule() {
        return parent.getModule();
    }

}
