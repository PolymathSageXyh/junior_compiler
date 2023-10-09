package paser.nodes;

public class DeclNode extends Node{

    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

}
