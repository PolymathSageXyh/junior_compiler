package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class AddExpNode extends Node {
    public Node lvar = null;
    public Node rvar = null;
    public SyntaxType op = null;
    public boolean isPrintNeZero = false;

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if (ctx.isConstExp) {
            Node child = children.get(0);
            if (child.getType() == SyntaxType.MUL_EXP) {
                ret.val = 0;
                child.checkError(errorList, ctx, ret);
                lvar = child;
            } else {
                int operand1 = 0, operand2 = 0;
                boolean isPLus = true;
                for (Node item : children) {
                    if (item.getType() == SyntaxType.ADD_EXP) {
                        lvar = item;
                        ret.val = 0;
                        item.checkError(errorList, ctx, ret);
                        operand1 = ret.val;
                    } else if (item.getType() == SyntaxType.PLUS || item.getType() == SyntaxType.MINU) {
                        op = item.getType();
                        isPLus = item.getType() == SyntaxType.PLUS;
                    } else if (item.getType() == SyntaxType.MUL_EXP) {
                        rvar = item;
                        ret.val = 0;
                        item.checkError(errorList, ctx, ret);
                        operand2 = ret.val;
                    }
                }
                if (isPLus) { ret.val = operand1 + operand2; }
                else { ret.val = operand1 - operand2;}
            }
        } else {
            Node child = children.get(0);
            if (child.getType() == SyntaxType.MUL_EXP) {
                child.checkError(errorList, ctx, ret);
                lvar = child;
            } else {
                for (Node item : children) {
                    if (item.getType() == SyntaxType.ADD_EXP) {
                        lvar = item;
                    } else if (item.getType() == SyntaxType.PLUS || item.getType() == SyntaxType.MINU) {
                        op = item.getType();
                    } else if (item.getType() == SyntaxType.MUL_EXP) {
                        rvar = item;
                    }
                    item.checkError(errorList, ctx, ret);
                }
            }
        }

    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }
}
