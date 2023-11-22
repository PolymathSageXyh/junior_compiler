package mips;

public class Assembly {
    public Assembly(AssemblyTable assemblyTable) {
        if (this instanceof GlobalAsm) {
            assemblyTable.addAssemblyToData(this);
        } else {
            assemblyTable.addAssemblyToText(this);
        }
    }
}
