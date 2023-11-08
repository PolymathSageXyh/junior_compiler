package lightllr;

import java.util.ArrayList;
import java.util.HashMap;

public class Function extends Value {
    private ArrayList<BasicBlock> basicBlocks = new ArrayList<>();    // basic blocks
    private ArrayList<Argument> arguments = new ArrayList<>();         // arguments
    private Module parent;
    private int seqCnt;

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
        buildArgs();
    }

    public static Function create(FunctionType ty, String name, Module parent) {
        return new Function(ty, name, parent);
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
                if (bb.setName("label"+seq_num )) {
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
