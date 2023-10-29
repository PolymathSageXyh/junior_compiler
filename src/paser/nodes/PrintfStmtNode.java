package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;

import java.util.ArrayList;

public class PrintfStmtNode extends Node {
    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String ss =  ((TokenNode)children.get(2)).getValue();
        int formatLine = children.get(2).endLine;
        String str = ss.substring(1, ss.length() - 1);
        int printfLine = children.get(0).endLine;
        int cnt = 0;
        int expNum = 0;
        for (int i = 0; i < str.length(); ) {
            char temp = str.charAt(i);
            if (temp == '%') {
                boolean flag = (i+1) < str.length() && str.charAt(i+1) == 'd';
                if(!flag) {
                    errorList.add(Mypair.of(ErrorType.INVALID_SYMBOL, formatLine));
                    return;
                } else {
                    cnt++;
                    i++;
                }
            } else if (temp == '\\') {
                boolean flag = (i+1) < str.length() && str.charAt(i+1) == 'n';
                if(!flag) {
                    errorList.add(Mypair.of(ErrorType.INVALID_SYMBOL, formatLine));
                    return;
                } else {
                    i++;
                }
            } else if (((int) temp >= 40 && (int) temp <= 126) || (int) temp == 32 || (int) temp == 33) {
                i++;
            } else {
                errorList.add(Mypair.of(ErrorType.INVALID_SYMBOL, formatLine));
                return;
            }
        }
        for (Node child : children) {
            if (child.getType() == SyntaxType.EXP) {
                child.checkError(errorList, ctx, ret);
                expNum++;
            }
        }
        if (cnt != expNum) errorList.add(Mypair.of(ErrorType.FORMAT_CHAR_NUM_UNMATCHED, printfLine));
    }
}
