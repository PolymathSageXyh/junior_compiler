package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class FuncFParamNode extends Node{

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String name = "";
        int line = 0;
        int dim = 0;
        ArrayList<Integer> temp = new ArrayList<>();
        for (Node child : children) {
            if (child.getType() == SyntaxType.IDENFR) {
                name = ((TokenNode)child).getValue();
                line = child.endLine;
            } else if (child.getType() == SyntaxType.LBRACK) {
                dim++;
            }
            child.checkError(errorList, ctx, ret);
        }
        ret.dimension = dim;
        for(int i = 0; i < dim; i++) {
            temp.add(0); //占位
        }
        if (!SymbolTable.getInstance().addVar(false, temp, name)) {
            errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
        }
    }

}
