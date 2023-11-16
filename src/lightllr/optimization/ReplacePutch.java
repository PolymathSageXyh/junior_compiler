package lightllr.optimization;

import lightllr.*;
import lightllr.Module;

import java.util.ArrayList;
import java.util.HashMap;

public class ReplacePutch extends Pass {
    private int cnt = 0;
    private HashMap<String, GlobalVariable> map = new HashMap<>();
    private Function putStr;

    public ReplacePutch(Module module) {
        super(module);
        IrType tyVoid = IrType.getVoidType(module);
        PointerType pp = PointerType.getPointerType(new IntegerType(8, module));
        ArrayList<IrType> putStrParams = new ArrayList<>();
        putStrParams.add(pp);
        FunctionType ff = FunctionType.get(tyVoid, putStrParams);
        putStr = new Function(ff, "putstr");
    }

    @Override
    public void run() {
        for (Function func : module.getFunctions()) {
            if (func.getBasicBlocks().size() > 0) {
                for (BasicBlock bb : func.getBasicBlocks()) {
                    ArrayList<Instruction> instrs = bb.getInstrList();
                    for (int i = 0; i < instrs.size(); i++) {
                        Instruction instr = instrs.get(i);
                        // 需要特殊处理putch，将多个字符打印合并为字符串打印
                        if (instr instanceof CallInstr) {
                            CallInstr cc = (CallInstr) instr;
                            String functionName = cc.getOperand(0).getName();
                            if (functionName.equals("putch")) {
                                StringBuilder ss = new StringBuilder();
                                Instruction temp = instr;
                                while (temp instanceof CallInstr &&
                                        (temp).getOperand(0).getName().equals("putch")) {
                                    Value value = temp.getOperand(1);
                                    ss.append((char) (((ConstantInt) value).getTruth()));
                                    i += 1;
                                    if (i >= instrs.size()) {
                                        break;
                                    }
                                    temp = instrs.get(i);
                                }
                                i -= 1;
                                String tmp = ss.toString();
                                GlobalVariable gg = addConstStr(tmp);
                                ArrayList<Value> offset = new ArrayList<>();
                                offset.add(ConstantInt.get(0, module));
                                offset.add(ConstantInt.get(0, module));
                                GetElementPtrInstr ptrInstr = new GetElementPtrInstr(bb, gg, offset);
                                ArrayList<Value> args = new ArrayList<>();
                                args.add(ptrInstr);
                                CallInstr callInstr = new CallInstr(bb, putStr, args);
                                instrs.add(i+1, ptrInstr);
                                instrs.add(i+2, callInstr);
                            }
                        }

                    }
                    for (int i = 0; i < instrs.size(); i++) {
                        Instruction temp = instrs.get(i);
                        if (temp instanceof CallInstr &&
                                (temp).getOperand(0).getName().equals("putch")) {
                            instrs.remove(i);
                            i--;
                        }
                    }
                }

            }
        }
    }

    public GlobalVariable addConstStr(String tmp) {
        if (!map.containsKey(tmp)) {
            ArrayType arrayType = ArrayType.get(IrType.getInt8Type(module), tmp.length()+1);
            ConstantStr cstr = ConstantStr.get(arrayType, tmp);
            GlobalVariable gg = GlobalVariable.create("str"+cnt++, module, arrayType, true, cstr);
            map.put(tmp, gg);
            return gg;
        } else {
            return map.get(tmp);
        }
    }

}