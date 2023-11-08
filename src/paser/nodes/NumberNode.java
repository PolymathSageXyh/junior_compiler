package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import paser.Mypair;

import java.util.ArrayList;

public class NumberNode extends Node {
    public int truth = 0;

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        truth = Integer.parseInt(((TokenNode) children.get(0)).getValue());
        if (ctx.isConstExp) {
            TokenNode child = ((TokenNode) children.get(0));
            ret.val = Integer.parseInt(child.getValue());
        }
    }
}
