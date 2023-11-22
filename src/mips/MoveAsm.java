package mips;
public class MoveAsm extends Assembly {
    private Register dst;
    private Register src;

    public MoveAsm(AssemblyTable assemblyTable, Register dst, Register src) {
        super(assemblyTable);
        this.dst = dst;
        this.src = src;
    }

    @Override
    public String toString() {
        return "move " + dst + " " + src;
    }
}