package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class PrimaryExpNode extends Node {

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if (ctx.isConstExp) {
            if (children.get(0).getType() == SyntaxType.LPARENT) {
                ret.val = 0;
                children.get(1).checkError(errorList, ctx, ret);
            } else {
                ret.val = 0;
                children.get(0).checkError(errorList, ctx, ret);
            }
        } else {
            for (Node child : children) {
                child.checkError(errorList, ctx, ret);
            }
        }

    }

    public void accept(AstVisitor astVisitor) {
        if (children.get(0).getType() == SyntaxType.LVAL) {
            astVisitor.visit(((LValNode)children.get(0)));
        } else if (children.get(0).getType() == SyntaxType.NUMBER) {
            astVisitor.visit(((NumberNode)children.get(0)));
        } else {
            ((ExpNode)children.get(1)).accept(astVisitor);
        }
    }

}
