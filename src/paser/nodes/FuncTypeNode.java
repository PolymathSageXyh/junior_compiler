package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;

import java.util.ArrayList;

public class FuncTypeNode extends Node{
    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if(children.get(0).getType() == SyntaxType.VOIDTK){
            ret.isVoid = true;
        }
    }
}
