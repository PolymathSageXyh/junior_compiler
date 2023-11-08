package paser.nodes;

import lexer.SyntaxType;

public class UnaryOpNode extends Node {
    public SyntaxType getContent() {
        return children.get(0).getType();
    }
}
