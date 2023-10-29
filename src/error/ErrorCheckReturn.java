package error;

import java.util.ArrayList;

public class ErrorCheckReturn {
    public int val;
    public int dimension;
    public boolean isConst;
    public boolean isReturn;
    public boolean isVoid;
    public ArrayList<Integer> params;

    public ErrorCheckReturn() {
        val = 0;
        dimension = 0;
        isConst = false;
        isReturn = false;
        isVoid = false;
        params = new ArrayList<>();
    }

    public void reset() {
        val = 0;
        dimension = 0;
        isConst = false;
        isReturn = false;
        isVoid = false;
        params.clear();
    }
}
