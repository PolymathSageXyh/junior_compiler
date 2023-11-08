package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class ReturnStmtNode extends Node {
    public Node exp = null;
    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        int line = 0;
        for (Node child : children) {
            if (child.getType() == SyntaxType.EXP) {
                exp = child;
            }
            if (child.getType() == SyntaxType.RETURNTK) {
                line = child.endLine;
            }
            child.checkError(errorList, ctx, ret);
            if (child.getType() == SyntaxType.EXP && ret.isVoid) {
                errorList.add(Mypair.of(ErrorType.VOIDFUNC_RETURN_EXP, line));
            }
        }
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

}
