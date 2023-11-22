package mips;

public class LaAsm extends Assembly {
    private Register target;
    private String lable;

    public LaAsm(AssemblyTable assemblyTable, Register target, String lable) {
        super(assemblyTable);
        this.target = target;
        this.lable = lable;
    }

    @Override
    public String toString() {
        return "la " + target + " " + lable;
    }

}
