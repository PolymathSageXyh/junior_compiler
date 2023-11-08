package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import lightllr.BasicBlock;
import paser.Mypair;

import java.util.ArrayList;

public class LAndExpNode extends Node {
    public Node lvar = null;
    public Node rvar = null;
    public SyntaxType op =null;
    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;

    public void setTrueBlock(BasicBlock trueBlock)
    {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock) {
        this.falseBlock = falseBlock;
    }

    public BasicBlock getFalseBlock() {
        return falseBlock;
    }

    public BasicBlock getTrueBlock() {
        return trueBlock;
    }
    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        Node child = children.get(0);
        if (child.getType() == SyntaxType.EQ_EXP) {
            child.checkError(errorList, ctx, ret);
            lvar = child;
        } else {
            for (Node item : children) {
                if (item.getType() == SyntaxType.LAND_EXP) {
                    lvar = item;
                } else if (item.getType() == SyntaxType.AND) {
                    op = SyntaxType.AND;
                } else if (item.getType() == SyntaxType.EQ_EXP) {
                    rvar = item;
                }
                item.checkError(errorList, ctx, ret);
            }
        }
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }
}
