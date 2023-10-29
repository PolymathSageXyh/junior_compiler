package paser.nodes;
import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import paser.Mypair;

import java.util.ArrayList;

public class ErrorNode extends Node {
    public ErrorType errorType;
    public int line;

    public ErrorNode(ErrorType errorType, int line) {
        this.errorType = errorType;
        this.line = line;
    }

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        errorList.add(Mypair.of(this.errorType,this.line));
    }
}
