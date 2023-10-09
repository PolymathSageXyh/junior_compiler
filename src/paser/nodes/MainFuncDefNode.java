package paser.nodes;

public class MainFuncDefNode extends Node{
    BlockNode blockNode = null;

    @Override
    public void addChild(Node blockNode) {
        super.addChild(blockNode);
        if (blockNode instanceof BlockNode) {
            this.blockNode = (BlockNode) blockNode;
        }
    }

}
