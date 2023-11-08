package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class MainFuncDefNode extends Node{
    public Node block = null;

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String name = "";
        int line = -1;
        boolean isVoid = false;
        ArrayList<Integer> params = new ArrayList<>();
        for (Node child : children) {
            if (child.getType() == SyntaxType.IDENFR) {
                name = ((TokenNode)child).getValue();
                line = child.endLine;
            } else if (child.getType() == SyntaxType.LPARENT) {
                SymbolTable.getInstance().startScope();
            } else if (child.getType() == SyntaxType.BLOCK) {
                block = child;
                if (!SymbolTable.getInstance().tryAddFunc(params, name, isVoid)) {
                    errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
                }
                ctx.afterFuncDef = true;
            }
            ErrorCheckReturn ret1 = new ErrorCheckReturn();
            child.checkError(errorList, ctx, ret1);
            if (child.getType() == SyntaxType.BLOCK) {
                ret.isReturn = ret1.isReturn;
            }
        }
        if(!ret.isReturn && !isVoid) {
            errorList.add(Mypair.of(ErrorType.INTFUNC_MISS_RETURN,endLine));
        }
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

}
