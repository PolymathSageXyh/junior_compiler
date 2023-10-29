package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import paser.Mypair;

import java.util.ArrayList;

public class ContinueStmtNode extends Node {
    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if (ctx.loopLevel == 0) {
            errorList.add(Mypair.of(ErrorType.BREAK_CONTINUE_OUT_LOOP, children.get(0).getStartLine()));
        }
    }

}
