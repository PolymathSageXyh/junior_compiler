package paser.nodes;

import lightllr.AstVisitor;

public class ExpNode extends Node {
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit((AddExpNode) children.get(0));
    }
}
