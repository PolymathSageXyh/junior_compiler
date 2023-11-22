package mips;

public class JumpAsm extends Assembly {
    private String type;
    private String target;
    private Register rd;

    public JumpAsm(AssemblyTable assemblyTable, String type, Register rd) {
        super(assemblyTable);
        this.type = type;
        this.target = null;
        this.rd = rd;
    }

    public JumpAsm(AssemblyTable assemblyTable, String type, String target) {
        super(assemblyTable);
        this.type = type;
        this.target = target;
        this.rd = null;
    }



    @Override
    public String toString() {
        if (type.equals("jr")) return type + " " + rd;
        return type + " " + target;
    }



}
