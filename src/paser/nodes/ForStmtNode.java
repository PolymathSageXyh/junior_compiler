package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class ForStmtNode extends Node {

    public Node lval = null;
    public Node exp = null;

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        for (Node child : children) {
            if (child.getType() == SyntaxType.LVAL) {
                lval = child;
            } else if (child.getType() == SyntaxType.EXP) {
                exp = child;
            }
            child.checkError(errorList, ctx, ret);
        }
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }
}
