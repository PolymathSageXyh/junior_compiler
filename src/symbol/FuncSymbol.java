package symbol;

import error.ErrorType;

import java.util.ArrayList;
import java.util.Objects;

public class FuncSymbol {
    private boolean isVoid; //类型
    private ArrayList<Integer> parameters; //parameters.size()为参数个数，内容为维度
    private String name;

    public FuncSymbol(boolean isVoid, ArrayList<Integer> parameters, String name) {
        this.isVoid = isVoid;
        this.parameters = new ArrayList<>();
        this.parameters.addAll(parameters); // 创建新对象并添加到新列表
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Integer> getParameters() {
        return parameters;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public ErrorType tryMatch(ArrayList<Integer> target) {
        if (parameters.size() != target.size()) {
            return ErrorType.FUNCPARAMS_NUM_UNMATCHED;
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (!Objects.equals(parameters.get(i), target.get(i))) {
                return ErrorType.FUNCPARAM_TYPE_UNMATCHED;
            }
        }
        return ErrorType.CORRECT;
    }
}
