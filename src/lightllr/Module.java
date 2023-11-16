package lightllr;

import paser.Mypair;

import java.util.ArrayList;
import java.util.HashMap;

public class Module {
    private ArrayList<GlobalVariable> globalList;   // The Global Variables in the module
    private ArrayList<Function> functionList;       // The Functions in the module
    private HashMap<String, Value> valueSym;   // Symbol table for values
    private static HashMap<Instruction.OpID, String> instrId2String = new HashMap<>();   // Instruction from opid to string
    private String moduleName;         // Human readable identifier for the module
    private String sourceFileName;    // Original source file name for module, for test and debug
    private IntegerType int1Ty = new IntegerType(1, this);
    private IntegerType int32Ty = new IntegerType(32, this);
    private IntegerType int8Ty = new IntegerType(8, this);
    private IrType labelTy = new IrType(IrType.TypeID.LabelTyID, this);
    private IrType voidTy = new IrType(IrType.TypeID.VoidTyID, this);
    private HashMap<IrType, PointerType> pointerMap;
    private HashMap<Mypair<IrType, Integer>, ArrayType> arrayMap;

    static {
        instrId2String.put(Instruction.OpID.ret, "ret");
        instrId2String.put(Instruction.OpID.br, "br");
        instrId2String.put(Instruction.OpID.add, "add" );
        instrId2String.put(Instruction.OpID.sub, "sub");
        instrId2String.put(Instruction.OpID.mul, "mul" );
        instrId2String.put(Instruction.OpID.sdiv, "sdiv" );
        instrId2String.put(Instruction.OpID.alloca, "alloca" );
        instrId2String.put(Instruction.OpID.load, "load" );
        instrId2String.put(Instruction.OpID.store, "store" );
        instrId2String.put(Instruction.OpID.cmp, "icmp" );
        instrId2String.put(Instruction.OpID.call, "call" );
        instrId2String.put(Instruction.OpID.getelementptr, "getelementptr");
    }
    // init instr_id2string


    public Module(String name) {
        this.moduleName = name;
        globalList = new ArrayList<>();
        functionList = new ArrayList<>();
        valueSym = new HashMap<>();
        instrId2String = new HashMap<>();
        pointerMap = new HashMap<>();
        arrayMap = new HashMap<>();
    }

    public String getInstrOpName(Instruction.OpID instr) {
        String ss = instrId2String.get(instr);
        return instrId2String.get(instr);
    }

    public IrType getVoidType() { return voidTy; }

    public IrType getLabelType() { return labelTy; }

    public IntegerType getInt1Type() { return int1Ty; }

    public IntegerType getInt32Type() { return int32Ty; }

    public IntegerType getInt8Type() { return int8Ty; }

    public PointerType getPointerType(IrType contains) {
        if(!pointerMap.containsKey(contains)) {
            pointerMap.put(contains, new PointerType(contains));
        }
        return pointerMap.get(contains);
    }

    public ArrayType getArrayType(IrType contains, int numElements) {
        if(!arrayMap.containsKey(Mypair.of(contains, numElements))) {
            arrayMap.put(Mypair.of(contains, numElements), new ArrayType(contains, numElements));
        }
        return arrayMap.get(Mypair.of(contains, numElements));
    }

    public PointerType getInt32PtrType() {
        return getPointerType(int32Ty);
    }

    public void addFunction(Function f) {
        functionList.add(f);
    }
    public ArrayList<Function> getFunctions(){
        return functionList;
    }
    public void addGlobalVariable(GlobalVariable g) {
        globalList.add(g);
    }
    public ArrayList<GlobalVariable> getGlobalVariable(){
        return globalList;
    }

    public void setPrintName() {
        for (Function func : this.getFunctions()) {
            func.setInstrName();
        }
    }

    public String print()
    {
        String module_ir = "";
        for (GlobalVariable global_val : this.globalList){
            module_ir += global_val.print();
            module_ir += "\n";
        }
        for (Function func : this.functionList){
            module_ir += func.print();
            module_ir += "\n";
        }
        return module_ir;
    }

}
