package mips;

public class MemAsm extends Assembly {
    private String type;
    private Register rd;
    private Register base;
    private Integer offset;
    private String label;

    public MemAsm(AssemblyTable assemblyTable,  String type, Register rd, Register base, Integer offset) {
        super(assemblyTable);
        this.type = type;
        this.rd = rd;
        this.base = base;
        this.offset = offset;
        this.label = null;
    }

    public MemAsm(AssemblyTable assemblyTable,  String type, Register rd, String label, Integer offset) {
        super(assemblyTable);
        this.type = type;
        this.rd = rd;
        this.base = null;
        this.offset = offset;
        this.label = label;
    }

    @Override
    public String toString() {
        if (label == null) {
            return type + " " + rd + " " + offset + "(" + base + ")";
        }
        return type + " " + rd + " " + label + "+" + offset;
    }
}
