package mips;

public class LabelAsm extends Assembly {
    private String label;

    public LabelAsm(AssemblyTable assemblyTable, String label) {
        super(assemblyTable);
        this.label = label;
    }

    @Override
    public String toString() {
        return label + ":";
    }
}
