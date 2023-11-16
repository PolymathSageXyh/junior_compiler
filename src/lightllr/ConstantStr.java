package lightllr;

public class ConstantStr extends Constant {
    private String content;

    private ConstantStr(ArrayType ty, String content) {
        super(ty, 0);
        this.content = content;
    }

    public static ConstantStr get(ArrayType ty, String content) {
        return new ConstantStr(ty, content);
    }

    @Override
    public String print() {
        String const_ir = "";
        const_ir += " c\"" + content.replace("\n", "\\0a") + "\\00\"";
        return const_ir;
    }
}
