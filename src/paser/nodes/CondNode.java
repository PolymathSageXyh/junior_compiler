package paser.nodes;

import lightllr.AstVisitor;
import lightllr.BasicBlock;

public class CondNode extends Node {
    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;

    public void setTrueBlock(BasicBlock trueBlock)
    {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock) {
        this.falseBlock = falseBlock;
    }

    public void accept(AstVisitor astVisitor) {
        LOrExpNode ll = ((LOrExpNode)children.get(0));
        ll.setFalseBlock(falseBlock);
        ll.setTrueBlock(trueBlock);
        astVisitor.visit(ll);
    }
}
