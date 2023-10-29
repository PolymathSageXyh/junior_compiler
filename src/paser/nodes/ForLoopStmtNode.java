package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import paser.Mypair;

import java.util.ArrayList;

public class ForLoopStmtNode extends Node {
    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        ctx.loopLevel++;
        for (Node child : children) {
            child.checkError(errorList, ctx, ret);
        }
        ctx.loopLevel--;
    }
}
