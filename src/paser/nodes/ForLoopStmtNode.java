package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class ForLoopStmtNode extends Node {
    public Node forstmt1 = null;
    public Node forstmt2 = null;
    public Node cond = null;
    public Node stmt = null;
    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        ctx.loopLevel++;
        int numOfse = 0;
        for (Node child : children) {
            if (child.getType() == SyntaxType.FOR_STMT && numOfse == 0) {
                forstmt1 = child;
            } else if (child.getType() == SyntaxType.SEMICN) {
                numOfse++;
            } else if (child.getType() == SyntaxType.FOR_STMT && numOfse > 0) {
                forstmt2 = child;
            } else if (child.getType() == SyntaxType.COND) {
                cond = child;
            } else if (child.getType() == SyntaxType.STMT) {
                stmt = child;
            }
            child.checkError(errorList, ctx, ret);
        }
        ctx.loopLevel--;
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }
}
