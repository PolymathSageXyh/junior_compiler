package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AllocaInstr;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class IfStmtNode extends Node {
    public Node cond = null;
    public Node if_stmt = null;
    public Node else_stmt = null;
    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        boolean flag = false;
        for (Node child : children) {
            if (child.getType() == SyntaxType.COND) {
                cond = child;
            } else if (child.getType() == SyntaxType.STMT && !flag) {
                if_stmt = child;
                flag = true;
            } else if (child.getType() == SyntaxType.STMT) {
                else_stmt = child;
            }
            child.checkError(errorList, ctx, ret);
        }
    }

}
