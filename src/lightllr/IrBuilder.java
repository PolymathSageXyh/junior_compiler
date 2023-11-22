package lightllr;

import java.util.ArrayList;

public class IrBuilder {
    private BasicBlock bb;
    private Module m;

    public IrBuilder(BasicBlock bb, Module m) {
        this.bb = bb;
        this.m = m;
    }

    public Module getModule(){return m;}
    public BasicBlock getInsertBlock() { return this.bb; }
    public void setInsertPoint(BasicBlock bb) { this.bb = bb; } //在某个基本块中插入指令
    public BinaryInstr createIadd(Value lhs, Value rhs){ return BinaryInstr.createAdd(lhs, rhs,bb, m);}
    public BinaryInstr createIsub(Value lhs, Value rhs) { return BinaryInstr.createSub(lhs, rhs, bb, m);}
    public BinaryInstr createImul(Value lhs, Value rhs){ return BinaryInstr.createMul(lhs, rhs, bb, m);}
    public BinaryInstr createIsdiv(Value lhs, Value rhs) { return BinaryInstr.createSdiv(lhs, rhs, bb, m);}

    public BinaryInstr createIsrem(Value lhs, Value rhs) { return BinaryInstr.createSrem(lhs, rhs, bb, m);}

    public BinaryInstr createIand(Value lhs, Value rhs) { return BinaryInstr.createAnd(lhs, rhs, bb, m);}

    public BinaryInstr createIor(Value lhs, Value rhs) { return BinaryInstr.createOr(lhs, rhs, bb, m);}
    public BinaryInstr createIxor(Value lhs, Value rhs) { return BinaryInstr.createXor(lhs, rhs, bb, m); }
    public CmpInstr createIcmpEq(Value lhs, Value rhs) { return CmpInstr.createCmp(CmpInstr.CmpOp.EQ, lhs, rhs, bb, m); }
    public CmpInstr createIcmpNe(Value lhs, Value rhs) { return CmpInstr.createCmp(CmpInstr.CmpOp.NE, lhs, rhs, bb, m); }
    public CmpInstr createIcmpGt(Value lhs, Value rhs){ return CmpInstr.createCmp(CmpInstr.CmpOp.GT, lhs, rhs, bb, m); }
    public CmpInstr createIcmpGe(Value lhs, Value rhs){ return CmpInstr.createCmp(CmpInstr.CmpOp.GE, lhs, rhs, bb, m); }
    public CmpInstr createIcmpLt(Value lhs, Value rhs){ return CmpInstr.createCmp(CmpInstr.CmpOp.LT, lhs, rhs, bb, m); }
    public CmpInstr createIcmpLe(Value lhs, Value rhs){ return CmpInstr.createCmp(CmpInstr.CmpOp.LE, lhs, rhs, bb, m); }

    public CallInstr createCall(Value func, ArrayList<Value> args) {
        return CallInstr.create(((Function)func) ,args, bb);
    }
    public ReturnInstr createRet(Value val) { return ReturnInstr.createRet(val,bb); }
    public ReturnInstr createVoidRet() { return ReturnInstr.createVoidRet(bb); }

    public StoreInstr createStore(Value val, Value ptr) { return StoreInstr.createStore(val, ptr, bb); }
    public LoadInstr createLoad(IrType ty, Value ptr) { return LoadInstr.createLoad(ty, ptr, bb); }
    public LoadInstr createLoad(Value ptr) {
        return LoadInstr.createLoad(ptr.getIrType().getPointerElementType(), ptr, bb);
    }
    public AllocaInstr createAlloca(IrType ty) { return AllocaInstr.createAlloca(ty, bb); }

    public BranchInstr createBr(BasicBlock if_true){ return BranchInstr.create_br(if_true, bb); }
    public BranchInstr createCondBr(Value cond, BasicBlock if_true, BasicBlock if_false){ return BranchInstr.create_cond_br(cond, if_true, if_false,bb); }

    public GetElementPtrInstr createGep(Value ptr, ArrayList<Value> idxs) { return GetElementPtrInstr.createGep(ptr, idxs, bb); }

    public ZextInstr createZext(Value val, IrType ty) { return ZextInstr.createZext(val, ty, bb); }
}
