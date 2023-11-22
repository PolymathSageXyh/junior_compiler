package mips;

public class SyscallAsm extends Assembly {
    public SyscallAsm(AssemblyTable assemblyTable) {
        super(assemblyTable);
    }

    @Override
    public String toString() {
        return "syscall";
    }
}
