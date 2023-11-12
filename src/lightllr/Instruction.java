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
        if (!this.parent.getHasBr()) {
            this.parent.addInstruction(this);
            if (id == OpID.br) {
                this.parent.setHasBr(true);
            }
        }
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

    public boolean isVoid() { return ((opid == OpID.ret) || (opid == OpID.br) || (opid == OpID.store) || (opid == OpID.call && this.getIrType().isVoidType())); }

    public boolean isPhi() { return opid == OpID.phi; }
    public boolean isStore() { return opid == OpID.store; }
    public boolean isAlloca() { return opid == OpID.alloca; }
    public boolean isRet() { return opid == OpID.ret; }
    public boolean isLoad() { return opid == OpID.load; }
    public boolean isBr() { return opid == OpID.br; }

    public boolean isAdd() { return opid == OpID.add; }
    public boolean isSub() { return opid == OpID.sub; }
    public boolean isMul() { return opid == OpID.mul; }
    public boolean isDiv() { return opid == OpID.sdiv; }

    public boolean isCmp() { return opid == OpID.cmp; }

    public boolean isCall() { return opid == OpID.call; }
    public boolean isGep() { return opid == OpID.getelementptr; }
    public boolean isZext() { return opid == OpID.zext; }


    public boolean isBinary()
    {
        return (isAdd() || isSub() || isMul() || isDiv()) &&
                (getNumOps() == 2);
    }

    public boolean isTerminator() { return isBr() || isRet(); }

    public BasicBlock getParent() { return parent; }
    public void set_parent(BasicBlock parent) { this.parent = parent; }
    // Return the function this instruction belongs to.
    public Function getFunction() {
        return parent.getParent();
    }
    public Module getModule() {
        return parent.getModule();
    }

}
