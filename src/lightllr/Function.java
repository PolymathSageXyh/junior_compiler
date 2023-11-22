package lightllr;

import mips.Register;

import java.util.ArrayList;
import java.util.HashMap;

public class Function extends Value {
    private ArrayList<BasicBlock> basicBlocks = new ArrayList<>();    // basic blocks
    private ArrayList<Argument> arguments = new ArrayList<>();         // arguments
    private Module parent;
    private int seqCnt;
    private HashMap<Value, Register> var2reg = new HashMap<>();
    private boolean isLibrary;
    private boolean hasSideEffect;
    private ArrayList<Function> calleeList = new ArrayList<>(); //调用了哪些自定义函数
    private ArrayList<Function> callerList = new ArrayList<>(); //被哪些自定义函数调用

    private void buildArgs() {
        FunctionType funcTy = getFunctionType();
        int numArgs = getNumOfArgs();
        for (int i = 0; i < numArgs; i++) {
            arguments.add(new Argument(funcTy.getParamType(i), "", this, i));
        }
    }

    public Function(FunctionType ty, String name, Module parent) {
        super(ty, name);
        this.parent = parent;
        this.seqCnt = 0;
        parent.addFunction(this);
        isLibrary = false;
        hasSideEffect = false;
        buildArgs();
    }

    public Function(FunctionType ty, String name) {
        super(ty, name);
        this.seqCnt = 0;
        buildArgs();
    }

    public ArrayList<Function> getCalleeList() {
        return calleeList;
    }

    public ArrayList<Function> getCallerList() {
        return callerList;
    }

    public void setLibrary(boolean library) {
        isLibrary = library;
    }

    public boolean isLibrary() {
        return isLibrary;
    }

    public void setHasSideEffect(boolean hasSideEffect) {
        this.hasSideEffect = hasSideEffect;
    }

    public boolean isHasSideEffect() {
        return hasSideEffect;
    }

    public static Function create(FunctionType ty, String name, Module parent) {
        return new Function(ty, name, parent);
    }

    public HashMap<Value, Register> getVar2reg() {
        return var2reg;
    }

    public void setVar2reg(HashMap<Value, Register> var2reg) {
        this.var2reg = var2reg;
    }

    public FunctionType getFunctionType() {
        return ((FunctionType) this.getIrType());
    }

    public IrType getReturnType() {
        return getFunctionType().getReturnType();
    }

    public int getNumOfArgs() {
        return getFunctionType().getNumOfArgs();
    }

    public int getNumBasicBlocks() {
        return basicBlocks.size();
    }

    public Module getParent() {
        return parent;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public boolean isDeclaration() { return basicBlocks.isEmpty(); }

    public void addBasicBlock(BasicBlock bb) {
        basicBlocks.add(bb);
    }

    public BasicBlock getEntryBB() { return basicBlocks.get(0); }

    public void remove(BasicBlock bb) {
        basicBlocks.remove(bb);
        for (BasicBlock pre : bb.getPrebbs()) {
            pre.removePreBasicBlock(bb);
        }
        for (BasicBlock succ : bb.getSuccbbs()) {
            succ.removePreBasicBlock(bb);
        }
    }

    public void setInstrName() {
        HashMap<Value, Integer> seq = new HashMap<>();
        for (Argument arg : this.getArguments())
        {
            if (!seq.containsKey(arg))
            {
                int seq_num = seq.size() + seqCnt;
                if (arg.setName("arg" + seq_num)) {
                    seq.put(arg, seq_num);
                }
            }
        }
        for (BasicBlock bb : basicBlocks)
        {
            if (!seq.containsKey(bb)) {
                int seq_num = seq.size() + seqCnt;
                if (bb.setName(name+"_label"+seq_num )) {
                    seq.put(bb, seq_num);
                }
            }
            for (Instruction instr : bb.getInstrList()) {
                if (!instr.isVoid() && !seq.containsKey(instr)) {
                    int seq_num = seq.size() + seqCnt;
                    if (instr.setName("op"+ seq_num)) {
                        seq.put(instr, seq_num);
                    }
                }
            }
        }
        seqCnt += seq.size();
    }

    public String print() {
        setInstrName();
        String func_ir = "";
        if (this.isDeclaration()) {
            func_ir += "declare ";
        } else {
            func_ir += "define ";
        }
        func_ir += this.getReturnType().print();
        func_ir += " ";
        func_ir += Irprinter.printAsOp(this, false);
        func_ir += "(";

        //print arg
        if (this.isDeclaration()) {
            for ( int i = 0 ; i < this.getNumOfArgs(); i++) {
                if(i > 0) func_ir += ", ";
                func_ir += ((FunctionType)this.getIrType()).getParamType(i).print();
            }
        } else {
            for (int i = 0; i < this.getNumOfArgs(); i++)
            {
                if(i > 0) {
                    func_ir += ", ";
                }
                func_ir += (arguments.get(i)).print();
            }
        }
        func_ir += ")";
        if( this.isDeclaration()) { func_ir += "\n"; }
        else {
            func_ir += " {";
            func_ir += "\n";
            for (BasicBlock bb : this.getBasicBlocks())
            {
                func_ir += bb.print();
            }
            func_ir += "}";
        }
        return func_ir;
    }


}
