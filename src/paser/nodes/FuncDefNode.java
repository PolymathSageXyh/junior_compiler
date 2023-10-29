package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class FuncDefNode extends Node{

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String name = "";
        int line = -1;
        for (Node child : children) {
            if (child.getType() == SyntaxType.IDENFR) {
                name = ((TokenNode)child).getValue();
                line = child.endLine;
            } else if (child.getType() == SyntaxType.LPARENT) {
                SymbolTable.getInstance().startScope();
            } else if (child.getType() == SyntaxType.BLOCK) {
                if (!SymbolTable.getInstance().tryAddFunc(ret.params, name, ret.isVoid)) {
                    errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
                }
                ctx.afterFuncDef = true;
                ret.params.clear();
            }
            child.checkError(errorList, ctx, ret);
        }
        if(!ret.isReturn && !ret.isVoid) {
            errorList.add(Mypair.of(ErrorType.INTFUNC_MISS_RETURN,endLine));
        }
        ret.reset();
    }
}
