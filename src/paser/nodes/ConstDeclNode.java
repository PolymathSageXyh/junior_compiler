package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import paser.Mypair;

import java.util.ArrayList;

public class ConstDeclNode extends Node{
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        ctx.isConst = true;
        for (Node child : children) {
            child.checkError(errorList, ctx, ret);
        }
        ctx.isConst = false;
    }
}
