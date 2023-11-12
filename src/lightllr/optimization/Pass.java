package lightllr.optimization;
import lightllr.Module;
public class Pass {
    protected Module module;

    public Pass(Module module) {
        this.module = module;
    }

    public void run() {}
}
