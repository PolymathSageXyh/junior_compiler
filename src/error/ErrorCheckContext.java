package error;

public class ErrorCheckContext {
    public int loopLevel;
    public boolean afterFuncDef;
    public boolean isConst;
    public boolean isLVal;

    public ErrorCheckContext() {
        loopLevel = 0;
        afterFuncDef =false;
        isConst = false;
        isLVal = false;
    }
}
