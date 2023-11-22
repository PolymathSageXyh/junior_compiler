package mips;

public class MdsAsm extends Assembly{
    private String type;
    private Register rs;
    private Register rt;

    public MdsAsm(AssemblyTable assemblyTable,  String type, Register rs, Register rt) {
        super(assemblyTable);
        this.type = type;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return type + " " + rs + " " + rt;
    }

}





