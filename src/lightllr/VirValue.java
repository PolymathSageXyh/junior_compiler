package lightllr;

public class VirValue extends Value {
    private static int virtual_cnt = 0;

    public VirValue(IrType type) {
        super(type);
        name = "virop" + virtual_cnt++;
    }

}
