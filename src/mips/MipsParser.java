package mips;

import lightllr.*;
import lightllr.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MipsParser {
    private AssemblyTable assemblyTable;
    private Module module;
    private Function curFunction;
    private int curStackOffset;
    private HashMap<Value, Integer> stackOffsetMap;
    private HashMap<Value, Register> var2reg;
    private HashMap<Register, Value> reg2var;

    public MipsParser(Module module) {
        this.module = module;
        this.assemblyTable = new AssemblyTable();
    }

    @Override
    public String toString() {
        return assemblyTable.toString();
    }

    public void enterFunc(Function newFunction) {
        this.curFunction = newFunction;
        this.curStackOffset = 0;
        this.stackOffsetMap = new HashMap<>();
        this.var2reg = newFunction.getVar2reg();
        this.reg2var = new HashMap<>();
    }

    public Register getRegOf(Value value) {
        if (var2reg == null) return null;
        return var2reg.get(value);
    }

    public void allocRegForParam(Argument param, Register reg) {
        if (var2reg == null) return;
        var2reg.put(param, reg);
    }

    public boolean useReg() {
        return var2reg != null;
    }

    public ArrayList<Register> getAllocatedRegs() {
        if (var2reg == null) return new ArrayList<>();
        return new ArrayList<>(new HashSet<>(var2reg.values()));
    }

    public void addValueOffsetMap(Value value, int offset) {
        stackOffsetMap.put(value, offset);
    }

    public Integer getOffsetOf(Value value) {
        return stackOffsetMap.get(value);
    }

    public void subCurOffset(int offset) {
        curStackOffset -= offset;
        assert curStackOffset >= 0;
    }

    public void parseModule() {
        parseGlobalVariables();
        parseFunctions();
    }

    public void parseGlobalVariables() {
        //System.out.println("\\0a".length());
        //System.out.println("\\n\\n".length());
        for (GlobalVariable gg : module.getGlobalVariable()) {
            ArrayList<Integer> elements = new ArrayList<>();
            Constant irInitVal = gg.getInitVal();
            if (irInitVal instanceof ConstantZero) {
                GlobalAsm tmp = new GlobalAsm(assemblyTable, gg.getName(), irInitVal.getIrType().getSize());
                //System.out.println(tmp);
            } else if (irInitVal instanceof ConstantStr) {
                GlobalAsm tmp = new GlobalAsm(assemblyTable, gg.getName(), ((ConstantStr) irInitVal).getContent());
                //System.out.println(tmp);
            } else if (irInitVal instanceof ConstantArray) {
                ArrayList<ConstantInt> dataElements = ((ConstantArray) irInitVal).getDataElements();
                for (ConstantInt dataElement : dataElements) {
                    elements.add(dataElement.getTruth());
                }
                GlobalAsm tmp = new GlobalAsm(assemblyTable, gg.getName(), elements);
                //System.out.println(tmp);
            } else {
                elements.add(((ConstantInt) irInitVal).getTruth());
                GlobalAsm tmp = new GlobalAsm(assemblyTable, gg.getName(), elements);
                //System.out.println(tmp);
            }
        }

    }

    public void parseFunctions() {
        int i = module.getFunctions().size();
        enterFunc(module.getFunctions().get(i-1));
        parseFunction(module.getFunctions().get(i-1));
        for (Function func : module.getFunctions()) {
            if (!func.isLibrary() && !func.getName().equals("main")) {
                enterFunc(func);
                parseFunction(func);
            }
        }
    }

    public void parseFunction(Function func) {
        new LabelAsm(assemblyTable, func.getName());
        for (int i = 0; i < func.getArguments().size(); i++) {
            if (i < 3) {
                allocRegForParam(func.getArguments().get(i),
                        Register.indexToReg(Register.A0.ordinal() + i + 1));
            }
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(func.getArguments().get(i), curOffset);
        }
        for (BasicBlock bb : func.getBasicBlocks()) {
            parseBasicBlock(bb);
        }
    }

    public void parseBasicBlock(BasicBlock bb) {
        new LabelAsm(assemblyTable, bb.getName());
        for (Instruction instr : bb.getInstrList()) {
            parseInstruction(instr);
        }
    }

    public void parseInstruction(Instruction instr) {
        if (instr instanceof BinaryInstr) {
            parseBinaryInstr((BinaryInstr) instr);
        } else if (instr instanceof CmpInstr) {
            parseCmpInstr((CmpInstr) instr);
        } else if (instr instanceof BranchInstr) {
            parseBranchInstr((BranchInstr) instr);
        } else if (instr instanceof CallInstr) {
            parseCallInstr((CallInstr) instr);
        } else if (instr instanceof AllocaInstr) {
            parseAllocaInstr((AllocaInstr) instr);
        } else if (instr instanceof MoveInstr) {
            parseMoveInstr((MoveInstr) instr);
        } else if (instr instanceof ReturnInstr) {
            parseReturnInstr((ReturnInstr) instr);
        } else if (instr instanceof GetElementPtrInstr) {
            parseGetElementPtrInstr((GetElementPtrInstr) instr);
        } else if (instr instanceof LoadInstr) {
            parseLoadInstr((LoadInstr) instr);
        } else if (instr instanceof StoreInstr) {
            parseStoreInstr((StoreInstr) instr);
        } else if (instr instanceof ZextInstr) {
            parseZextInstr((ZextInstr) instr);
        } else {
            System.out.println("undefined instuction");
            assert false : "I haven't implemented the " + instr.getClass().toString();
        }

    }

    public void parseBinaryInstr(BinaryInstr instr) {
        if (instr.isAdd()) {
            parseAddInstr(instr);
        } else if (instr.isSub()) {
            parseSubInstr(instr);
        } else if (instr.isMul() || instr.isDiv() || instr.isSrem()) {
            parseMulDivSremInstr(instr);
        }
    }

    public void parseAddInstr(BinaryInstr instr) {
        boolean isOp1Const = instr.getOperand(0) instanceof ConstantInt;
        boolean isOp2Const = instr.getOperand(1) instanceof ConstantInt;
        Register tarReg = getRegOf(instr);
        if (tarReg == null) {
            tarReg = Register.K0;
        }
        if (isOp1Const && isOp2Const) {
            int op1Imm = ((ConstantInt) instr.getOperand(0)).getTruth();
            int op2Imm = ((ConstantInt) instr.getOperand(1)).getTruth();
            new LiAsm(assemblyTable, tarReg, op1Imm + op2Imm);
        } else if (isOp1Const) {
            Register src2 = getRegOf(instr.getOperand(1));
            if (src2 != null) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, src2, ((ConstantInt) instr.getOperand(0)).getTruth());
            } else {
                src2 = Register.K1;
                Integer offset = getOffsetOf(instr.getOperand(1));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(1), offset);
                }
                new MemAsm(assemblyTable, "lw", src2, Register.SP, offset);
                new BinaryAsm(assemblyTable, "addiu", tarReg, src2, ((ConstantInt) instr.getOperand(0)).getTruth());
            }
        } else if (isOp2Const) {
            Register src1 = getRegOf(instr.getOperand(0));
            if (src1 != null) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, src1, ((ConstantInt) instr.getOperand(1)).getTruth());
            } else {
                src1 = Register.K1;
                Integer offset = getOffsetOf(instr.getOperand(0));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(0), offset);
                }
                new MemAsm(assemblyTable, "lw", src1, Register.SP, offset);
                new BinaryAsm(assemblyTable, "addiu", tarReg, src1, ((ConstantInt) instr.getOperand(1)).getTruth());
            }
        } else {
            Register src1 = getRegOf(instr.getOperand(0));
            Register src2 = getRegOf(instr.getOperand(1));
            if (src1 == null) {
                src1 = Register.K0;
                Integer offset = getOffsetOf(instr.getOperand(0));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(0), offset);
                }
                new MemAsm(assemblyTable, "lw", src1, Register.SP, offset);
            }
            if (src2 == null) {
                src2 = Register.K1;
                Integer offset = getOffsetOf(instr.getOperand(1));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(1), offset);
                }
                new MemAsm(assemblyTable, "lw", src2, Register.SP, offset);
            }
            new BinaryAsm(assemblyTable, "addu", tarReg, src1, src2);
        }
        if (getRegOf(instr) == null) {
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
        }
    }

    public void parseSubInstr(BinaryInstr instr) {
        boolean isOp1Const = instr.getOperand(0) instanceof ConstantInt;
        boolean isOp2Const = instr.getOperand(1) instanceof ConstantInt;
        Register tarReg = getRegOf(instr);
        if (tarReg == null) {
            tarReg = Register.K0;
        }
        if (isOp1Const && isOp2Const) {
            int op1Imm = ((ConstantInt) instr.getOperand(0)).getTruth();
            int op2Imm = ((ConstantInt) instr.getOperand(1)).getTruth();
            new LiAsm(assemblyTable, tarReg, op1Imm - op2Imm);
        } else if (isOp1Const) {
            Register src2 = getRegOf(instr.getOperand(1));
            if (src2 != null) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, src2, -((ConstantInt) instr.getOperand(0)).getTruth());
                new BinaryAsm(assemblyTable, "subu", tarReg, Register.ZERO, tarReg);
            } else {
                src2 = Register.K1;
                Integer offset = getOffsetOf(instr.getOperand(1));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(1), offset);
                }
                new MemAsm(assemblyTable, "lw", src2, Register.SP, offset);
                new BinaryAsm(assemblyTable, "addiu", tarReg, src2, -((ConstantInt) instr.getOperand(0)).getTruth());
                new BinaryAsm(assemblyTable, "subu", tarReg, Register.ZERO, tarReg);
            }
        } else if (isOp2Const) {
            Register src1 = getRegOf(instr.getOperand(0));
            if (src1 != null) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, src1, -((ConstantInt) instr.getOperand(1)).getTruth());
            } else {
                src1 = Register.K1;
                Integer offset = getOffsetOf(instr.getOperand(0));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(0), offset);
                }
                new MemAsm(assemblyTable, "lw", src1, Register.SP, offset);
                new BinaryAsm(assemblyTable, "addiu", tarReg, src1, -((ConstantInt) instr.getOperand(1)).getTruth());
            }
        } else {
            Register src1 = getRegOf(instr.getOperand(0));
            Register src2 = getRegOf(instr.getOperand(1));
            if (src1 == null) {
                src1 = Register.K0;
                Integer offset = getOffsetOf(instr.getOperand(0));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(0), offset);
                }
                new MemAsm(assemblyTable, "lw", src1, Register.SP, offset);
            }
            if (src2 == null) {
                src2 = Register.K1;
                Integer offset = getOffsetOf(instr.getOperand(1));
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(instr.getOperand(1), offset);
                }
                new MemAsm(assemblyTable, "lw", src2, Register.SP, offset);
            }
            new BinaryAsm(assemblyTable, "subu", tarReg, src1, src2);
        }
        if (getRegOf(instr) == null) {
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
        }
    }

    public void parseMulDivSremInstr(BinaryInstr instr) {
        Value operand1 = instr.getOperand(0);
        Value operand2 = instr.getOperand(1);
        Register reg1 = Register.K0;
        Register reg2 = Register.K1;
        Register tarReg = getRegOf(instr);
        if (tarReg == null) {
            tarReg = Register.K0;
        }
        if (operand1 instanceof ConstantInt && operand2 instanceof ConstantInt) {
            int op1 = ((ConstantInt)operand1).getTruth();
            int op2 = ((ConstantInt)operand2).getTruth();
            int res = instr.isMul() ? op1 * op2 :
                      instr.isDiv() ? op1 / op2 : op1 % op2;
            new LiAsm(assemblyTable, tarReg, res);
            if (getRegOf(instr) == null) {
                subCurOffset(4);
                int curOffset = curStackOffset;
                addValueOffsetMap(instr, curOffset);
                new MemAsm(assemblyTable,  "sw", tarReg, Register.SP, curOffset);
            }
            return;
        }
        if (operand1 instanceof ConstantInt) {
            new LiAsm(assemblyTable, reg1, ((ConstantInt)operand1).getTruth());
        }
        else if (getRegOf(operand1) != null) {
            reg1 = getRegOf(operand1);
        }
        else {
            Integer offset = getOffsetOf(operand1);
            if (offset == null) {
                subCurOffset(4);
                offset = curStackOffset;
                addValueOffsetMap(operand1, offset);
            }
            new MemAsm(assemblyTable, "lw", reg1, Register.SP, offset);
        }
        if (operand2 instanceof ConstantInt) {
            new LiAsm(assemblyTable, reg2, ((ConstantInt)operand2).getTruth());
        }
        else if (getRegOf(operand2) != null) {
            reg2 = getRegOf(operand2);
        }
        else {
            Integer offset = getOffsetOf(operand2);
            if (offset == null) {
                subCurOffset(4);
                offset = curStackOffset;
                addValueOffsetMap(operand2, offset);
            }
            new MemAsm(assemblyTable, "lw", reg2, Register.SP, offset);
        }
        if (instr.isMul()) {
            new MdsAsm(assemblyTable,"mult", reg1, reg2);
            new HiLoAsm(assemblyTable,"mflo", tarReg);
        } else if (instr.isDiv()) {
            new MdsAsm(assemblyTable, "div", reg1, reg2);
            new HiLoAsm(assemblyTable,"mflo", tarReg);
        } else {
            new MdsAsm(assemblyTable, "div", reg1, reg2);
            new HiLoAsm(assemblyTable,"mfhi", tarReg);
        }
        if (getRegOf(instr) == null) {
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable,  "sw", tarReg, Register.SP, curOffset);
        }
    }

    public void parseZextInstr(ZextInstr instr) {
        Value oriValue = instr.getOperand(0);
        if (((IntegerType)oriValue.getIrType()).isI1()) {
            if (getRegOf(oriValue) != null && getRegOf(instr) != null) {
                Register reg = getRegOf(oriValue);
                Register target = getRegOf(instr);
                if (reg != target) {
                    new MoveAsm(assemblyTable, target, reg);
                }
            } else if (getRegOf(oriValue) != null) {
                Register reg = getRegOf(oriValue);
                subCurOffset(4);
                int curOffset = curStackOffset;
                addValueOffsetMap(instr, curOffset);
                new MemAsm(assemblyTable, "sw", reg, Register.SP, curOffset);
            } else if (getRegOf(instr) != null) {
                Register target = getRegOf(instr);
                new MemAsm(assemblyTable, "lw", target, Register.SP, getOffsetOf(oriValue));
            } else {
                addValueOffsetMap(instr, getOffsetOf(oriValue));
            }
        }
    }

    public void parseCmpInstr(CmpInstr instr) {
        Value lhs = instr.getOperand(0);
        Value rhs = instr.getOperand(1);
        Register tarReg = getRegOf(instr);
        if (tarReg == null) tarReg = Register.K0;
        if (lhs instanceof ConstantInt && rhs instanceof ConstantInt) {
            int ll = ((ConstantInt)lhs).getTruth();
            int rr = ((ConstantInt)rhs).getTruth();
            int res = instr.compare(ll, rr) ? 1 : 0;
            new LiAsm(assemblyTable, tarReg, res);
            if (getRegOf(instr) == null) {
                subCurOffset(4);
                int curOffset = curStackOffset;
                addValueOffsetMap(instr, curOffset);
                new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
            }
        } else {
            Register reg1 = Register.K0;
            Register reg2 = Register.K1;
            if (lhs instanceof ConstantInt) {
                new LiAsm(assemblyTable, reg1, ((ConstantInt)lhs).getTruth());
            }
            else if (getRegOf(lhs) != null) {
                reg1 = getRegOf(lhs);
            }
            else {
                Integer offset = getOffsetOf(lhs);
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(lhs, offset);
                }
                new MemAsm(assemblyTable, "lw", reg1, Register.SP, offset);
            }
            if (rhs instanceof ConstantInt) {
                new LiAsm(assemblyTable, reg2, ((ConstantInt)rhs).getTruth());
            } else if (getRegOf(rhs) != null) {
                reg2 = getRegOf(rhs);
            } else {
                Integer offset = getOffsetOf(rhs);
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(rhs, offset);
                }
                new MemAsm(assemblyTable, "lw", reg2, Register.SP, offset);
            }
            switch (instr.getCmpOp()) {
                case EQ -> new BinaryAsm(assemblyTable, "seq", tarReg, reg1, reg2);
                case NE -> new BinaryAsm(assemblyTable, "sne", tarReg, reg1, reg2);
                case GT -> new BinaryAsm(assemblyTable, "sgt", tarReg, reg1, reg2);
                case GE -> new BinaryAsm(assemblyTable, "sge", tarReg, reg1, reg2);
                case LT -> new BinaryAsm(assemblyTable, "slt", tarReg, reg1, reg2);
                case LE -> new BinaryAsm(assemblyTable, "sle", tarReg, reg1, reg2);
            }
            if (getRegOf(instr) == null) {
                subCurOffset(4);
                int curOffset = curStackOffset;
                addValueOffsetMap(instr, curOffset);
                new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
            }
        }
    }

    public void parseCallInstr(CallInstr instr) {
        Function func = ((Function) instr.getOperand(0));
        if (func.isLibrary()) {
            parseLibraryCall(instr, func.getName());
        } else {
            parseNoneLibraryCall(instr);
        }
    }

    public void parseNoneLibraryCall(CallInstr instr) {
        Function func = (Function) instr.getOperand(0);
        ArrayList<Value> argList = new ArrayList<>();
        for (int i = 1; i < instr.getNumOps(); i++) {
            argList.add(instr.getOperand(i));
        }
        ArrayList<Register> allocatedRegs = getAllocatedRegs();
        int curOffset = curStackOffset;
        int regNum = 0;
        for (Register reg : allocatedRegs) {
            ++regNum;
            new MemAsm(assemblyTable, "sw", reg, Register.SP, curOffset - regNum * 4);
        }
        new MemAsm(assemblyTable, "sw", Register.SP, Register.SP, curOffset - regNum * 4 - 4);
        new MemAsm(assemblyTable, "sw", Register.RA, Register.SP, curOffset - regNum * 4 - 8);
        int argNum = 0;
        for (Value arg : argList) {
            ++argNum;
            if (argNum <= 3 && useReg()) {
                Register paramReg = Register.indexToReg(Register.A0.ordinal() + argNum);
                if (arg instanceof ConstantInt) {
                    new LiAsm(assemblyTable, paramReg, ((ConstantInt)arg).getTruth());
                } else if (getRegOf(arg) != null) {
                    Register srcReg = getRegOf(arg);
                    if (arg instanceof Argument) {
                        new MemAsm(assemblyTable, "lw", paramReg, Register.SP, curOffset - (allocatedRegs.indexOf(srcReg) + 1) * 4);
                    } else {
                        new MoveAsm(assemblyTable, paramReg, getRegOf(arg));
                    }
                } else {
                    new MemAsm(assemblyTable, "lw", paramReg, Register.SP, getOffsetOf(arg));
                }
            }
            else {
                Register tempReg = Register.K0;
                if (arg instanceof ConstantInt) {
                    new LiAsm(assemblyTable, tempReg, ((ConstantInt)arg).getTruth());
                } else if (getRegOf(arg) != null) {
                    Register srcReg = getRegOf(arg);
                    if (arg instanceof Argument) {
                        new MemAsm(assemblyTable, "lw", tempReg, Register.SP, curOffset - (allocatedRegs.indexOf(srcReg) + 1) * 4);
                    }
                    else tempReg = srcReg;
                } else {
                    new MemAsm(assemblyTable, "lw", tempReg, Register.SP, getOffsetOf(arg));
                }
                new MemAsm(assemblyTable, "sw", tempReg, Register.SP,  curOffset - regNum * 4 - 8 -argNum * 4);
            }

        }
        new BinaryAsm(assemblyTable, "addi", Register.SP, Register.SP, curOffset- regNum * 4 - 8);
        new JumpAsm(assemblyTable,"jal", func.getName());
        new MemAsm(assemblyTable, "lw", Register.RA, Register.SP, 0);
        new MemAsm(assemblyTable,  "lw", Register.SP, Register.SP, 4);
        regNum = 0;
        for (Register reg : allocatedRegs) {
            ++regNum;
            new MemAsm(assemblyTable, "lw", reg, Register.SP, curOffset - regNum * 4);
        }
        if (!instr.getIrType().isVoidType()) {
            if (getRegOf(instr) != null) {
                Register reg = getRegOf(instr);
                new MoveAsm(assemblyTable, reg, Register.V0);
            }
            else {
                subCurOffset(4);
                curOffset = curStackOffset;
                addValueOffsetMap(instr, curOffset);
                new MemAsm(assemblyTable, "sw", Register.V0, Register.SP, curOffset);
            }
        }
    }


    public void parseLibraryCall(CallInstr instr, String name) {
        if(name.equals("getint")) {
            new LiAsm(assemblyTable, Register.V0, 5);
            new SyscallAsm(assemblyTable);
            if (getRegOf(instr) != null) {
                Register reg = getRegOf(instr);
                new MoveAsm(assemblyTable, reg, Register.V0);
            }
            else {
                subCurOffset(4);
                int curOffset = curStackOffset;
                addValueOffsetMap(instr, curOffset);
                new MemAsm(assemblyTable, "sw", Register.V0, Register.SP, curOffset);
            }
        } else if (name.equals("putint")) {
            Value target = instr.getOperand(1);
            if (target instanceof ConstantInt) {
                new LiAsm(assemblyTable, Register.A0, ((ConstantInt)target).getTruth());
            } else if (getRegOf(target) != null) {
                Register reg = getRegOf(target);
                new MoveAsm(assemblyTable, Register.A0, reg);
            } else {
                Integer offset = getOffsetOf(target);
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(target, offset);
                }
                new MemAsm(assemblyTable, "lw", Register.A0, Register.SP, offset);
            }
            new LiAsm(assemblyTable, Register.V0, 1);
            new SyscallAsm(assemblyTable);
        } else if (name.equals("putstr")) {
            Value target = ((GetElementPtrInstr)instr.getOperand(1)).getOperand(0);
            if (target instanceof GlobalVariable) {
                new LaAsm(assemblyTable, Register.A0, target.getName());
                new LiAsm(assemblyTable, Register.V0, 4);
                new SyscallAsm(assemblyTable);
            } else {
                System.out.println("none Global str!!!");
            }
        } else {
            Value target = instr.getOperand(1);
            int val = ((ConstantInt)target).getTruth();
            new LiAsm(assemblyTable, Register.V0, 11);
            new LiAsm(assemblyTable, Register.A0, val);
            new SyscallAsm(assemblyTable);
        }

    }

    public void parseAllocaInstr (AllocaInstr instr) {
        if (instr.getAllocaTy().isArrayType()) {
            subCurOffset((((ArrayType)instr.getAllocaTy()).getTotalInt()) * 4);
        } else {
            subCurOffset(4);
        }
        if (getRegOf(instr) != null) {
            Register pointerReg = getRegOf(instr);
            int curOffset = curStackOffset;
            new BinaryAsm(assemblyTable, "addi", pointerReg, Register.SP, curOffset);
        } else {
            int curOffset = curStackOffset;
            new BinaryAsm(assemblyTable, "addi", Register.K0, Register.SP, curOffset);
            subCurOffset(4);
            curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable, "sw", Register.K0, Register.SP, curOffset);
        }
    }

    public void parseLoadInstr(LoadInstr instr) {
        Value ptr = instr.getLval();
        Register pointerReg = Register.K0;
        Register tarReg = getRegOf(instr);
        if (tarReg == null) tarReg = Register.K0;
        if (ptr instanceof GlobalVariable) {
//            if (getRegOf(ptr) != null) {
//                pointerReg = getRegOf(ptr);
//            }
            new LaAsm(assemblyTable, pointerReg, ptr.getName());
        } else if (getRegOf(ptr) != null) {
            pointerReg = getRegOf(ptr);
        } else {
            Integer offset = getOffsetOf(ptr);
            if (offset == null) {
                subCurOffset(4);
                offset = curStackOffset;
                addValueOffsetMap(ptr, offset);
            }
            new MemAsm(assemblyTable, "lw", pointerReg, Register.SP, offset);
        }
        new MemAsm(assemblyTable, "lw", tarReg, pointerReg, 0);
        if (getRegOf(instr) == null) {
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
        }
    }

    public void parseStoreInstr(StoreInstr instr) {
        Value val = instr.getRval();
        Value ptr = instr.getLval();
        Register fromReg = Register.K0;
        Register toReg = Register.K1;
        if (ptr instanceof GlobalVariable) {
            new LaAsm(assemblyTable, toReg, ptr.getName());
        } else if (getRegOf(ptr) != null) {
            toReg = getRegOf(ptr);
        } else {
            new MemAsm(assemblyTable, "lw", toReg, Register.SP, getOffsetOf(ptr));
        }
        if (val instanceof ConstantInt) {
            new LiAsm(assemblyTable, fromReg, ((ConstantInt)val).getTruth());
        }
        else if (getRegOf(val) != null) {
            fromReg = getRegOf(val);
        }
        else {
            Integer valueOffset = getOffsetOf(val);
            if (valueOffset == null) {
                subCurOffset(4);
                valueOffset = curStackOffset;
                addValueOffsetMap(val, valueOffset);
            }
            new MemAsm(assemblyTable, "lw", fromReg, Register.SP, valueOffset);
        }
        new MemAsm(assemblyTable, "sw", fromReg, toReg, 0);
    }

    public void parseBranchInstr(BranchInstr instr) {
        if (instr.is_cond_br()) {
            Value con = instr.getOperand(0);
            BasicBlock trueBlock = (BasicBlock) instr.getOperand(1);
            BasicBlock falseBlock = (BasicBlock) instr.getOperand(2);

            Register reg = getRegOf(con);
            if (reg == null) {
                reg = Register.K0;
                new MemAsm(assemblyTable, "lw", reg, Register.SP, getOffsetOf(con));
            }
            new BranchAsm(assemblyTable, "bne", reg, Register.ZERO, trueBlock.getName());
            new JumpAsm(assemblyTable, "j", falseBlock.getName());
        } else {
            BasicBlock targetBB = (BasicBlock) instr.getOperand(0);
            new JumpAsm(assemblyTable, "j", targetBB.getName());
        }
    }

    public void parseGetElementPtrInstr(GetElementPtrInstr instr) {
        Value pointer = instr.getOperand(0);
        IrType ty = ((PointerType)pointer.getIrType()).getElementType();
        if (ty.isArrayType() && ((ArrayType)ty).getElementType().isIntegerType() && ((IntegerType)((ArrayType)ty).getElementType()).isI8()) return;
        if (ty.isIntegerType()) {
            parseZeroDimGEP(instr);
        } else if (ty.isArrayType()) {
            ArrayType temp = (ArrayType) ty;
            if (temp.getDim() == 1) {
                parseOneDimGEP(instr, temp);
            } else {
                parseTwoDimGEP(instr, temp);
            }
        } else {
            System.out.println("undefined GEP");
        }
    }

    public void parseZeroDimGEP(GetElementPtrInstr instr) {
        ArrayList<Value> offset = new ArrayList<>();
        for (int i = 1; i < instr.getNumOps(); i++) {
            offset.add(instr.getOperand(i));
        }
        assert offset.size() == 1 : "parseZeroDimGEP error!!!";
        Value pointer = instr.getOperand(0);
        Register pointerReg = Register.K0;
        Register offsetReg = Register.K1;
        Register tarReg = getRegOf(instr);
        if (tarReg == null) {
            tarReg = Register.K0;
        }
        if (pointer instanceof GlobalVariable) {
            new LaAsm(assemblyTable, pointerReg, pointer.getName());
        } else if (getRegOf(pointer) != null) {
            pointerReg = getRegOf(pointer);
        } else {
            new MemAsm(assemblyTable, "lw", pointerReg, Register.SP, getOffsetOf(pointer));
        }
        Value tmp = offset.get(0);
        if (tmp instanceof ConstantInt) {
            new BinaryAsm(assemblyTable, "addiu", tarReg, pointerReg, 4*((ConstantInt) tmp).getTruth());
        } else {
            if (getRegOf(tmp) != null) {
                offsetReg = getRegOf(tmp);
            } else {
                Integer hh = getOffsetOf(tmp);
                if (tmp == null) {
                    subCurOffset(4);
                    hh = curStackOffset;
                    addValueOffsetMap(tmp, hh);
                }
                new MemAsm(assemblyTable, "lw", offsetReg, Register.SP, hh);
            }
            new BinaryAsm(assemblyTable, "sll", Register.K1, offsetReg, 2);
            new BinaryAsm(assemblyTable, "addu", tarReg, Register.K1, pointerReg);
        }
        if (getRegOf(instr) == null) {
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
        }
    }

    public void parseOneDimGEP(GetElementPtrInstr instr, ArrayType ty) {
        ArrayList<Value> offset = new ArrayList<>();
        for (int i = 1; i < instr.getNumOps(); i++) {
            offset.add(instr.getOperand(i));
        }
        Value pointer = instr.getOperand(0);
        Register pointerReg = Register.K0;
        Register offsetReg = Register.K1;
        Register tarReg = getRegOf(instr);
        int length = ty.getNumOfElements();
        if (tarReg == null) {
            tarReg = Register.K0;
        }
        if (pointer instanceof GlobalVariable) {
            new LaAsm(assemblyTable, pointerReg, pointer.getName());
        } else if (getRegOf(pointer) != null) {
            pointerReg = getRegOf(pointer);
        } else {
            new MemAsm(assemblyTable, "lw", pointerReg, Register.SP, getOffsetOf(pointer));
        }
        if (offset.size() == 1) {
            Value tmp = offset.get(0);
            if (tmp instanceof ConstantInt) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, pointerReg, 4 * (((ConstantInt) tmp).getTruth()) * length);
            } else {
                if (getRegOf(tmp) != null) {
                    offsetReg = getRegOf(tmp);
                } else {
                    Integer hh = getOffsetOf(tmp);
                    if (tmp == null) {
                        subCurOffset(4);
                        hh = curStackOffset;
                        addValueOffsetMap(tmp, hh);
                    }
                    new MemAsm(assemblyTable, "lw", offsetReg, Register.SP, hh);
                }
                new LiAsm(assemblyTable, Register.GP, length);
                new MdsAsm(assemblyTable, "mult", offsetReg, Register.GP);
                new HiLoAsm(assemblyTable, "mflo", Register.GP);
                new BinaryAsm(assemblyTable, "sll", Register.K1, Register.GP, 2);
                new BinaryAsm(assemblyTable, "addu", tarReg, Register.K1, pointerReg);
            }
        } else if (offset.size() > 1) {
            Value tmp = offset.get(0);
            if (tmp instanceof ConstantInt) {
                new BinaryAsm(assemblyTable, "addiu", Register.FP, pointerReg, 4 * (((ConstantInt) tmp).getTruth()) * length);
            } else {
                if (getRegOf(tmp) != null) {
                    offsetReg = getRegOf(tmp);
                } else {
                    Integer hh = getOffsetOf(tmp);
                    if (tmp == null) {
                        subCurOffset(4);
                        hh = curStackOffset;
                        addValueOffsetMap(tmp, hh);
                    }
                    new MemAsm(assemblyTable, "lw", offsetReg, Register.SP, hh);
                }
                new LiAsm(assemblyTable, Register.GP, length);
                new MdsAsm(assemblyTable, "mult", offsetReg, Register.GP);
                new HiLoAsm(assemblyTable, "mflo", Register.GP);
                new BinaryAsm(assemblyTable, "sll", Register.K1, Register.GP, 2);
                new BinaryAsm(assemblyTable, "addu", Register.FP, Register.K1, pointerReg);
            }
            Value tt = offset.get(1);
            Register oo = Register.K1;
            if (tt instanceof ConstantInt) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, Register.FP, 4 * (((ConstantInt) tt).getTruth()));
            } else {
                if (getRegOf(tt) != null) {
                    oo = getRegOf(tt);
                } else {
                    Integer hh = getOffsetOf(tt);
                    if (tt == null) {
                        subCurOffset(4);
                        hh = curStackOffset;
                        addValueOffsetMap(tt, hh);
                    }
                    new MemAsm(assemblyTable, "lw", oo, Register.SP, hh);
                }
                new BinaryAsm(assemblyTable, "sll", Register.K1, oo, 2);
                new BinaryAsm(assemblyTable, "addu", tarReg, Register.K1, Register.FP);
            }
        }
        if (getRegOf(instr) == null) {
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
        }
    }

    public void parseTwoDimGEP(GetElementPtrInstr instr, ArrayType ty) {
        ArrayList<Value> offset = new ArrayList<>();
        for (int i = 1; i < instr.getNumOps(); i++) {
            offset.add(instr.getOperand(i));
        }
        Value pointer = instr.getOperand(0);
        Register pointerReg = Register.K0;
        Register offsetReg = Register.K1;
        Register tarReg = getRegOf(instr);
        int length = ty.getTotalInt() / ty.getNumOfElements();
        if (tarReg == null) {
            tarReg = Register.K0;
        }
        if (pointer instanceof GlobalVariable) {
            new LaAsm(assemblyTable, pointerReg, pointer.getName());
        } else if (getRegOf(pointer) != null) {
            pointerReg = getRegOf(pointer);
        } else {
            new MemAsm(assemblyTable, "lw", pointerReg, Register.SP, getOffsetOf(pointer));
        }
        if (offset.size() == 2) {
            Value tmp = offset.get(1);
            if (tmp instanceof ConstantInt) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, pointerReg, 4 * (((ConstantInt) tmp).getTruth()) * length);
            } else {
                if (getRegOf(tmp) != null) {
                    offsetReg = getRegOf(tmp);
                } else {
                    Integer hh = getOffsetOf(tmp);
                    if (tmp == null) {
                        subCurOffset(4);
                        hh = curStackOffset;
                        addValueOffsetMap(tmp, hh);
                    }
                    new MemAsm(assemblyTable, "lw", offsetReg, Register.SP, hh);
                }
                new LiAsm(assemblyTable, Register.GP, length);
                new MdsAsm(assemblyTable, "mult", offsetReg, Register.GP);
                new HiLoAsm(assemblyTable, "mflo", Register.GP);
                new BinaryAsm(assemblyTable, "sll", Register.K1, Register.GP, 2);
                new BinaryAsm(assemblyTable, "addu", tarReg, Register.K1, pointerReg);
            }
        }
        else if (offset.size() > 2) {
            Value tmp = offset.get(1);
            if (tmp instanceof ConstantInt) {
                new BinaryAsm(assemblyTable, "addiu", Register.FP, pointerReg, 4 * (((ConstantInt) tmp).getTruth()) * length);
            } else {
                if (getRegOf(tmp) != null) {
                    offsetReg = getRegOf(tmp);
                } else {
                    Integer hh = getOffsetOf(tmp);
                    if (tmp == null) {
                        subCurOffset(4);
                        hh = curStackOffset;
                        addValueOffsetMap(tmp, hh);
                    }
                    new MemAsm(assemblyTable, "lw", offsetReg, Register.SP, hh);
                }
                new LiAsm(assemblyTable, Register.GP, length);
                new MdsAsm(assemblyTable, "mult", offsetReg, Register.GP);
                new HiLoAsm(assemblyTable, "mflo", Register.GP);
                new BinaryAsm(assemblyTable, "sll", Register.K1, Register.GP, 2);
                new BinaryAsm(assemblyTable, "addu", Register.FP, Register.K1, pointerReg);
            }
            Value tt = offset.get(2);
            Register oo = Register.K1;
            if (tt instanceof ConstantInt) {
                new BinaryAsm(assemblyTable, "addiu", tarReg, Register.FP, 4 * (((ConstantInt) tt).getTruth()));
            } else {
                if (getRegOf(tt) != null) {
                    oo = getRegOf(tt);
                } else {
                    Integer hh = getOffsetOf(tt);
                    if (tt == null) {
                        subCurOffset(4);
                        hh = curStackOffset;
                        addValueOffsetMap(tt, hh);
                    }
                    new MemAsm(assemblyTable, "lw", oo, Register.SP, hh);
                }
                new BinaryAsm(assemblyTable, "sll", Register.K1, oo, 2);
                new BinaryAsm(assemblyTable, "addu", tarReg, Register.K1, Register.FP);
            }
        }
        if (getRegOf(instr) == null) {
            subCurOffset(4);
            int curOffset = curStackOffset;
            addValueOffsetMap(instr, curOffset);
            new MemAsm(assemblyTable, "sw", tarReg, Register.SP, curOffset);
        }
    }

    public void parseReturnInstr(ReturnInstr instr) {
        if (instr.getNumOps() > 0) {
            Value val = instr.getOperand(0);
            if (val instanceof ConstantInt) {
                new LiAsm(assemblyTable, Register.V0, ((ConstantInt)val).getTruth());
            } else if (getRegOf(val) != null) {
                Register reg = getRegOf(val);
                new MoveAsm(assemblyTable, Register.V0, reg);
            } else {
                Integer offset = getOffsetOf(val);
                if (offset == null) {
                    subCurOffset(4);
                    offset = curStackOffset;
                    addValueOffsetMap(val, offset);
                }
                new MemAsm(assemblyTable, "lw", Register.V0, Register.SP, offset);
            }
        }
        //System.out.println(curFunction.getName());
        if(!curFunction.getName().equals("main")) {
            new JumpAsm(assemblyTable, "jr", Register.RA);
        } else {
            new LiAsm(assemblyTable, Register.V0, 10);
            new SyscallAsm(assemblyTable);
        }

    }

    public void parseMoveInstr(MoveInstr instr) {
        Value src = instr.getSrc();
        Value dst = instr.getDst();
        Register srcReg;
        Register dstReg = Register.K1;
        if (getRegOf(dst) != null && getRegOf(dst) == getRegOf(src)) {
            return;
        }
        if (getRegOf(dst) != null) {
            dstReg = getRegOf(dst);
        }
        if (src instanceof ConstantInt) {
            new LiAsm(assemblyTable, dstReg, ((ConstantInt)src).getTruth());
        } else if (getRegOf(src) != null) {
            srcReg = getRegOf(src);
            new MoveAsm(assemblyTable, dstReg, srcReg);
        } else {
            Integer offset = getOffsetOf(src);
            if (offset == null) {
                subCurOffset(4);
                offset = curStackOffset;
                addValueOffsetMap(src, offset);
            }
            new MemAsm(assemblyTable, "lw", dstReg, Register.SP, offset);
        }
        if (getRegOf(dst) == null)  {
            Integer offset = getOffsetOf(dst);
            if (offset == null) {
                subCurOffset(4);
                int curOffset = curStackOffset;
                addValueOffsetMap(dst, curOffset);
                new MemAsm(assemblyTable, "sw", dstReg, Register.SP, curOffset);
            } else {
                new MemAsm(assemblyTable, "sw", dstReg, Register.SP, offset);
            }
        }
    }






}
