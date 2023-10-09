package paser.nodes;

import lexer.SyntaxType;

public class TokenNode extends Node{
    private String value;
    public TokenNode(SyntaxType type, int line, String value) {
        this.setLeafNode(type, line);
        this.value = value;
    }

    @Override
    public StringBuilder getPaserLog() {
        this.paserLog.append(this).append("\n");
        return this.paserLog;
    }

    @Override
    public String toString()
    {
        return type.toString() + " " + value;
    }
}
