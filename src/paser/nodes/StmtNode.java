package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;

import java.util.ArrayList;

public class StmtNode extends Node {
    private SyntaxType type;
    public Node lval = null;
    public Node exp = null;

    public StmtNode(SyntaxType type) {
        super();
        this.type = type;
    }

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        for (Node child : children) {
            if(child.getType() == SyntaxType.LVAL){
                ctx.isLVal = true;
                lval = child;
            } else if (child.getType() == SyntaxType.EXP) {
                exp = child;
            }
            child.checkError(errorList, ctx, ret);
            ctx.isLVal = false;
            ret.isReturn = child.getType() == SyntaxType.RETURN_STMT;
        }
    }

    public void accept(AstVisitor astVisitor) {
        if (type == SyntaxType.FOR_LOOP_STMT) {
            ((ForLoopStmtNode)children.get(0)).accept(astVisitor);
        } else if (type == SyntaxType.RETURN_STMT) {
            ((ReturnStmtNode)children.get(0)).accept(astVisitor);
        } else if (type == SyntaxType.BREAK_STMT) {
            ((BreakStmtNode)children.get(0)).accept(astVisitor);
        } else if (type == SyntaxType.CONTINUE_STMT) {
            ((ContinueStmtNode)children.get(0)).accept(astVisitor);
        } else if (type == SyntaxType.PRINTF_STMT) {
            ((PrintfStmtNode)children.get(0)).accept(astVisitor);
        } else if (type == SyntaxType.IF_STMT) {
            ((IfStmtNode)children.get(0)).accept(astVisitor);
        } else if (type == SyntaxType.BLOCK) {
            ((BlockNode)children.get(0)).acccept(astVisitor);
        } else if (type == SyntaxType.EXP) {
            ((ExpNode)children.get(0)).accept(astVisitor);
        } else if (type == SyntaxType.ASSIGN) {
            astVisitor.visit(this);
        }
    }

}
