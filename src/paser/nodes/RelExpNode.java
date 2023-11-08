package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class RelExpNode extends Node {
    public Node lvar = null;
    public Node rvar = null;
    public SyntaxType op =null;
    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        Node child = children.get(0);
        if (child.getType() == SyntaxType.ADD_EXP) {
            child.checkError(errorList, ctx, ret);
            lvar = child;
        } else {
            for (Node item : children) {
                if (item.getType() == SyntaxType.REAL_EXP) {
                    lvar = item;
                } else if (item.getType() == SyntaxType.GRE || item.getType() == SyntaxType.GEQ
                        || item.getType() == SyntaxType.LEQ || item.getType() == SyntaxType.LSS) {
                    op = item.getType();
                } else if (item.getType() == SyntaxType.ADD_EXP) {
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
