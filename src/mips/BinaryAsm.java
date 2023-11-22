package mips;

public class BinaryAsm extends Assembly {
    private final String type;
    private Register dst;
    private Register src1;
    private Register src2;
    private int imm;
    private boolean hasImm;
    public BinaryAsm(AssemblyTable assemblyTable, String type, Register dst, Register src1, Register src2) {
        super(assemblyTable);
        this.type = type;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        this.imm = 0;
        this.hasImm = false;
    }

    public BinaryAsm(AssemblyTable assemblyTable, String type, Register dst, Register src1, int imm) {
        super(assemblyTable);
        this.type = type;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = null;
        this.imm = imm;
        this.hasImm = true;
    }

    @Override
    public String toString() {
        if (hasImm) {
            return type + " " + dst + ", " + src1 + ", " + imm;
        } else {
            return type + " " + dst + ", " + src1 + ", " + src2;
        }
    }

}
