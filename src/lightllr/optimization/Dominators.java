package lightllr.optimization;

import lightllr.BasicBlock;
import lightllr.Function;
import lightllr.Module;

import java.util.*;

public class Dominators extends Pass {
    private ArrayList<BasicBlock> reversePostOrder = new ArrayList<>();
    private HashMap<BasicBlock, Integer> postOrderId = new HashMap<>();   // the root has highest ID
    private HashMap<BasicBlock, HashSet<BasicBlock>> doms = new HashMap<>();   // dominance set
    private HashMap<BasicBlock, BasicBlock> idom = new HashMap<>();    // immediate dominance
    private HashMap<BasicBlock, LinkedHashSet<BasicBlock>> domFrontier = new HashMap<>();    // dominance frontier set
    private HashMap<BasicBlock, HashSet<BasicBlock>> domTreeSuccBlocks = new HashMap<>();

    private HashSet<BasicBlock> unreachableBlocks = new HashSet<>();

    public Dominators(Module module) {
        super(module);
    }

    public void addDom(BasicBlock bb, BasicBlock dom_bb) { doms.get(bb).add(dom_bb); }
    public HashSet<BasicBlock> getDoms(BasicBlock bb) { return doms.get(bb); }
    public void setDoms(BasicBlock bb, HashSet<BasicBlock> doms) {
        this.doms.get(bb).clear();
        this.doms.get(bb).addAll(doms);
    }

    public BasicBlock getIdom(BasicBlock bb) { return idom.get(bb); }
    public void setIdom (BasicBlock bb, BasicBlock idom) { this.idom.put(bb, idom); }

    public void addDominanceFrontier(BasicBlock bb, BasicBlock dom_frontier_bb) { domFrontier.get(bb).add(dom_frontier_bb); }
    public LinkedHashSet<BasicBlock> getDominanceFrontier(BasicBlock bb) { return domFrontier.get(bb); }
    public void setDominanceFrontier(BasicBlock bb, HashSet<BasicBlock> df) {
        domFrontier.get(bb).clear();
        domFrontier.get(bb).addAll(df);
    }

    // successor blocks of this node in dominance tree
    public HashSet<BasicBlock> getDomTreeSuccBlocks(BasicBlock bb) { return domTreeSuccBlocks.get(bb); }

    public void addDomTreeSuccBlock(BasicBlock bb, BasicBlock dom_tree_succ_bb) { domTreeSuccBlocks.get(bb).add(dom_tree_succ_bb); }

    @Override
    public void run() {
        for (Function f1 : module.getFunctions()) {
            Function f = f1;
            if (f.getBasicBlocks().size() == 0) continue;
            for (BasicBlock bb1 : f.getBasicBlocks()) {
                BasicBlock bb = bb1;
                doms.put(bb, new HashSet<>());
                domFrontier.put(bb, new LinkedHashSet<>());
                domTreeSuccBlocks.put(bb, new HashSet<>());
            }
            createReversePostOrder(f);
            createIdom(f);
            moveUnreachableBlocks(f);
            getIdomList(f);
            createDominanceFrontier(f);
            createDomTreeSucc(f);
            // for debug
            //printIdom(f);
            //printDominanceFrontier(f);
        }
    }

    public void createDoms(Function f) {
        for (BasicBlock bb : f.getBasicBlocks()) {
            addDom(bb, bb);
        }
        boolean changed = true;
        HashSet<BasicBlock> pre = new HashSet<>();
        while (changed) {
            changed = false;
            for (BasicBlock bb : f.getBasicBlocks()) {
                ArrayList<BasicBlock> bbs = bb.getPrebbs();
                HashSet<BasicBlock> res = new HashSet<>(getDoms(bbs.get(0)));
                for (int i = 1; i < bbs.size(); i++) {
                    res.retainAll(getDoms(bbs.get(i)));
                }
                res.add(bb);
                if (!getDoms(bb).equals(res)) {
                    setDoms(bb, res);
                    changed = true;
                }
            }
        }
    }

    public void createReversePostOrder(Function f) {
        reversePostOrder.clear();
        postOrderId.clear();
        HashSet<BasicBlock> visited = new HashSet<>();
        postOrderVisit(f.getEntryBB(), visited);
        Collections.reverse(reversePostOrder);
    }

    public void createIdom(Function f) {
        for (BasicBlock bb : f.getBasicBlocks()) {
            setIdom(bb, null);
        }
        BasicBlock root = f.getEntryBB();
        setIdom(root, root);

        // iterate
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock bb : this.reversePostOrder) {
                if (bb == root) {
                    continue;
                }
                // find one pred which has idom
                BasicBlock pred = null;
                for (BasicBlock p : bb.getPrebbs()) {
                    if (getIdom(p) != null) {
                        pred = p;
                        break;
                    }
                }
                assert(pred != null);
                BasicBlock new_idom = pred;
                for (BasicBlock p : bb.getPrebbs()) {
                    if (p == pred)
                        continue;
                    if (getIdom(p) != null) {
                        new_idom = intersect(p, new_idom);
                    }
                }
                if (getIdom(bb) != new_idom) {
                    setIdom(bb, new_idom);
                    changed = true;
                }
            }
        }
    }

    public void getIdomList(Function f) {
        for (BasicBlock bb : f.getBasicBlocks()) {
            ArrayList<BasicBlock> res = new ArrayList<>();
            for(HashMap.Entry<BasicBlock, BasicBlock> entry : idom.entrySet()) {
                if (entry.getValue() == bb && entry.getKey() != bb) {
                    res.add(entry.getKey());
                }
            }
            bb.setDomlist(res);
        }
    }

    public void moveUnreachableBlocks(Function f) {
        for (BasicBlock bb : f.getBasicBlocks()) {
            if (idom.get(bb) == null) {
                unreachableBlocks.add(bb);
            }
        }
        for (int i = 0; i < f.getBasicBlocks().size(); i++) {
            if (unreachableBlocks.contains(f.getBasicBlocks().get(i))) {
                for (BasicBlock item: f.getBasicBlocks().get(i).getSuccbbs()) {
                    item.getPrebbs().remove(f.getBasicBlocks().get(i));
                }
                f.getBasicBlocks().remove(f.getBasicBlocks().get(i));
                i--;
            }
        }
    }

    public void createDominanceFrontier(Function f) {
        for (BasicBlock bb : f.getBasicBlocks()) {
            if (bb.getPrebbs().size() >= 2) {
                for (BasicBlock p : bb.getPrebbs()) {
                    BasicBlock runner = p;
                    while (runner != getIdom(bb) && runner != null) {
                        addDominanceFrontier(runner, bb);
                        runner = getIdom(runner);
                    }
                }
            }
        }
    }

    public void createDomTreeSucc(Function f) {
        for (BasicBlock bb : f.getBasicBlocks()) {
            BasicBlock idom = getIdom(bb);
            // e.g, entry bb
            if (idom != bb && idom != null) {
                addDomTreeSuccBlock(idom, bb);
            }
        }
    }

    private void postOrderVisit(BasicBlock bb, HashSet<BasicBlock> visited) {
        visited.add(bb);
        for (BasicBlock b : bb.getSuccbbs()) {
            if (!visited.contains(b))
                postOrderVisit(b, visited);
        }
        postOrderId.put(bb, reversePostOrder.size());
        reversePostOrder.add(bb);
    }

    private BasicBlock intersect(BasicBlock b1, BasicBlock b2) {
        while (b1 != b2) {
            while (postOrderId.get(b1) < postOrderId.get(b2)) {
                assert(getIdom(b1) != null);
                b1 = getIdom(b1);
            }
            while (postOrderId.get(b2) < postOrderId.get(b1)) {
                assert(getIdom(b2) != null);
                b2 = getIdom(b2);
            }
        }
        return b1;
    }

    // for debug
    public void printIdom(Function f) {
        int counter = 0;
        HashMap<BasicBlock, String> bb_id = new HashMap<>();
        for (BasicBlock bb : f.getBasicBlocks()) {
            if (bb.getName().isEmpty()) {
                bb_id.put(bb, "bb" + counter);
            } else {
                bb_id.put(bb, bb.getName());
            }
            counter++;
        }
        System.out.println("Immediate dominance of function " + f.getName() +":\n");
        for (BasicBlock bb : f.getBasicBlocks()) {
            String output = "";
            output = bb_id.get(bb) + ": ";
            if (getIdom(bb) != null) {
                output += bb_id.get(getIdom(bb));
            } else {
                output += "null";
            }
            System.out.println(output);
        }
    }
    public void printDominanceFrontier(Function f) {
        int counter = 0;
        HashMap<BasicBlock, String> bb_id = new HashMap<>();
        for (BasicBlock bb : f.getBasicBlocks()) {
            if (bb.getName().isEmpty()) {
                bb_id.put(bb, "bb" + counter);
            } else {
                bb_id.put(bb, bb.getName());
            }
            counter++;
        }
        System.out.println("Dominance Frontier of function " + f.getName() +":\n");
        for (BasicBlock bb : f.getBasicBlocks()) {
            String output = "";
            output = bb_id.get(bb) + ": ";
            if (getDominanceFrontier(bb).isEmpty()) {
                output += "null";
            } else {
                boolean first = true;
                for (BasicBlock df : getDominanceFrontier(bb)) {
                    if (first) {
                        first = false;
                    } else {
                        output += ", ";
                    }
                    output += bb_id.get(df);
                }
            }
            System.out.println(output);
        }
    }

}
