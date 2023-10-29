package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;

import java.util.ArrayList;

public class FuncRParamsNode extends Node {

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        for (Node child : children) {
            if (child.getType() == SyntaxType.EXP) {
                ErrorCheckReturn ret1 = new ErrorCheckReturn();
                child.checkError(errorList, ctx, ret1);
                ret.params.add(ret1.dimension);
            }
        }
    }
}
