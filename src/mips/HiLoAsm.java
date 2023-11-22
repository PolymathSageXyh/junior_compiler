package mips;

public class HiLoAsm extends Assembly {
    private String type;
    private Register rd;

    public HiLoAsm(AssemblyTable assemblyTable, String type, Register rd) {
        super(assemblyTable);
        this.type = type;
        this.rd = rd;
    }

    @Override
    public String toString() {
        return type + " " + rd;
    }
}
