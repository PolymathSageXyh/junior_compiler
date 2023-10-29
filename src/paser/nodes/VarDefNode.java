package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class VarDefNode extends Node {
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String name = "";
        int line = 0;
        ArrayList<Integer> dim = new ArrayList<>();
        for (Node child : children) {
            if(child.getType() == SyntaxType.IDENFR) {
                name = ((TokenNode)child).getValue();
                line = child.getEndLine();
            }
            child.checkError(errorList, ctx, ret);
            if(child.getType() == SyntaxType.CONST_EXP){
                dim.add(0); //站位
            }
        }
        if(!SymbolTable.getInstance().addVar(ctx.isConst, dim, name)) {
            errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
        }
    }

}
