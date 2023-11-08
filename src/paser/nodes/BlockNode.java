package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class BlockNode extends Node{
    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if (ctx.afterFuncDef) {
            ctx.afterFuncDef = false;
        } else {
            SymbolTable.getInstance().startScope();
        }
        int i = 0;
        for ( ;i < children.size() - 1; i++) {
            children.get(i).checkError(errorList, ctx, ret);
        }
        if (children.get(i-1).getType() != SyntaxType.STMT) {
            ret.isReturn = false;
        }
        SymbolTable.getInstance().endScope();
    }

    public void acccept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

}
