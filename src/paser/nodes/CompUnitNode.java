package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class CompUnitNode extends Node{
    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        SymbolTable.getInstance().startScope();
        for (Node child : children) {
            child.checkError(errorList, ctx, ret);
        }
        SymbolTable.getInstance().endScope();
    }
}
