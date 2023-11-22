package mips;

public class BranchAsm extends Assembly {
    private String type;
    private Register rs;
    private Register rt;
    private String label;

    public BranchAsm(AssemblyTable assemblyTable, String type, Register rs, Register rt, String label) {
        super(assemblyTable);
        this.type = type;
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    @Override
    public String toString() {
        return type + " " + rs + " " + rt + " " + label;
    }

}
