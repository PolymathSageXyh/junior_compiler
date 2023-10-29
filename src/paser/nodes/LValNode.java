package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class LValNode extends Node {

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        TokenNode temp = null;
        int cnt = 0;
        for (Node child : children) {
            if(child.getType() == SyntaxType.IDENFR) {
                temp = (TokenNode)child;
                if (SymbolTable.getInstance().getVar(temp.getValue()) == null) {
                    errorList.add(Mypair.of(ErrorType.UNDEFINED_IDENT, temp.endLine));
                    return;
                } else if (ctx.isLVal && SymbolTable.getInstance().getVar(temp.getValue()).isConst()) {
                    errorList.add(Mypair.of(ErrorType.CONSTANT_ASSIGNING, temp.endLine));
                    return;
                }
                ctx.isLVal = false;
            }
            if(child.getType() ==SyntaxType.EXP) cnt++;
            child.checkError(errorList, ctx, ret);
        }
        if (temp != null) {
            ret.dimension = SymbolTable.getInstance().getVar(temp.getValue()).getArrayDim() - cnt;
        }
    }

}
