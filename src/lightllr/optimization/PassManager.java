package lightllr.optimization;

import paser.Mypair;
import lightllr.Module;

import java.util.ArrayList;

import static paser.Mypair.of;

public class PassManager {
    public PassManager(Module m) {
        this.module = m;
    }
    public void addPass(Pass pass, boolean print){
        passes.add(Mypair.of(pass, print));
    }
    public void run(){
        for(Mypair<Pass, Boolean> pass : passes){
            pass.first.run();
            if(pass.second){
                module.print();
            }
        }
    }

    private ArrayList<Mypair<Pass, Boolean>> passes = new ArrayList<>();
    private Module module;
}
