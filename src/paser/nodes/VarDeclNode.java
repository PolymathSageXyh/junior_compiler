package paser.nodes;

import lightllr.AstVisitor;

public class VarDeclNode extends Node{
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

}
