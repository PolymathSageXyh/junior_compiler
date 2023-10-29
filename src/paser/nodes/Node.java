package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;

import java.util.ArrayList;

public class Node {
    protected int startLine;
    protected int endLine;
    protected SyntaxType type;
    protected ArrayList<Node> children = new ArrayList<>();
    protected StringBuilder paserLog = new StringBuilder();

    public void addChild(Node child) {
        children.add(0,child);
    }

    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        this.paserLog.append("<").append(SyntaxType.getLog(type)).append(">").append("\n");
        return this.paserLog;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public SyntaxType getType() {
        return type;
    }

    public void setLeafNode(SyntaxType syntaxType, int line) {
        this.type = syntaxType;
        this.startLine = this.endLine = line;
    }

    public void setNonLeafNode(SyntaxType syntaxType) {
        this.type = syntaxType;
        this.startLine = children.get(0).startLine;
        this.endLine = children.get(children.size() - 1).endLine;
    }

    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        for (Node child : children) {
            child.checkError(errorList, ctx, ret);
        }
    }

}
