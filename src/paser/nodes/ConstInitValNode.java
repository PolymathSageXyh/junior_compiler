package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;

import java.util.ArrayList;

public class ConstInitValNode extends Node {

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if (children.get(0).getType() == SyntaxType.CONST_EXP) {
            ctx.isConstExp = true;
            ret.val = 0;
            children.get(0).checkError(errorList, ctx, ret);
            ctx.isConstExp = false;
            ret.inits.add(ret.val);
        } else {
            for (Node item : children) {
                if (item.getType() == SyntaxType.CONST_INIT_VAL) {
                    item.checkError(errorList, ctx, ret);
                }
            }
        }
    }
}
