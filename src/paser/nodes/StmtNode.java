package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;

import java.util.ArrayList;

public class StmtNode extends Node {
    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        for (Node child : children) {
            if(child.getType() == SyntaxType.LVAL){
                ctx.isLVal = true;
            }
            child.checkError(errorList, ctx, ret);
            ctx.isLVal = false;
            ret.isReturn = child.getType() == SyntaxType.RETURN_STMT;
        }
    }

}
