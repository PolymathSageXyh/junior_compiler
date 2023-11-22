package mips;

import java.util.ArrayList;

public class GlobalAsm extends Assembly{

    private String name;
    private boolean isInit;
    private boolean isStr;
    private int size;
    private ArrayList<Integer> elements;
    private String content;

    public GlobalAsm(AssemblyTable assemblyTable, String name, ArrayList<Integer> elements){
        super(assemblyTable);
        this.name = name;
        this.isInit = true;
        this.isStr = false;
        this.size = 4 * elements.size();
        this.elements = elements;
        this.content = null;
    }

    public GlobalAsm(AssemblyTable assemblyTable, String name, String content){
        super(assemblyTable);
        this.name = name;
        this.isInit = true;
        this.isStr = true;
        this.size = content.replace("\n", "\\n").length();
        this.elements = null;
        this.content = content;
    }

    public GlobalAsm(AssemblyTable assemblyTable, String name, int size) {
        super(assemblyTable);
        this.name = name;
        this.isInit = false;
        this.isStr = false;
        this.size = size;
        this.elements = null;
        this.content = null;
    }

    @Override
    public String toString() {
        StringBuilder ss = new StringBuilder("");
        ss.append(name).append(":\n");
        // 初始化了就用 .word 或者 .ascii
        if (isInit) {
            if (isStr) {
                ss.append(".asciiz \"").append(content.replace("\n", "\\n")).append("\"");
            } else {
                int i = 0;
                ss.append(".word ");
                for (; i < elements.size()-1; i++) {
                    ss.append(elements.get(i)).append(", ");
                }
                ss.append(elements.get(i));
            }
        } else {
            ss.append(".space ").append(size);
        }
        return ss.toString();
    }

}
