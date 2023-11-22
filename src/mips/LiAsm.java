package mips;

public class LiAsm extends Assembly {
    private Register rd;
    private Integer number;

    public LiAsm(AssemblyTable assemblyTable, Register rd, Integer number) {
        super(assemblyTable);
        this.rd = rd;
        this.number = number;
    }

    @Override
    public String toString() {
        return "li " + rd + " " + number;
    }
}
