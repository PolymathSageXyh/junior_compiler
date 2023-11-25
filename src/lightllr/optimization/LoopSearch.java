package lightllr.optimization;

import lightllr.BasicBlock;
import lightllr.Function;
import lightllr.Module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import static java.lang.Math.min;

public class LoopSearch  extends Pass {
    private int index_count;
    public Stack<CFGNode> stack = new Stack<>();
    // loops found
    public HashSet<HashSet<BasicBlock>> loop_set = new HashSet<>();
    // loops found in a function
    public HashMap<Function, HashSet<HashSet<BasicBlock>>> func2loop = new HashMap<>();
    // { entry bb of loop : loop }
    public HashMap<BasicBlock, HashSet<BasicBlock>> base2loop = new HashMap<>();
    // { loop : entry bb of loop }
    public HashMap<HashSet<BasicBlock>, BasicBlock> loop2base = new HashMap<>();
    // { bb :  entry bb of loop} 默认最低层次的loop
    public HashMap<BasicBlock, BasicBlock> bb2base = new HashMap<>();
    public LoopSearch(Module module) {
        super(module);
    }

    public BasicBlock get_loop_base(HashSet<BasicBlock> loop) { return loop2base.get(loop); }

    // 得到bb所在最低层次的loop
    public HashSet<BasicBlock> get_inner_loop(BasicBlock bb){
        if(!bb2base.containsKey(bb))
            return null;
        return base2loop.get(bb2base.get(bb));
    }

    public void build_cfg(Function func, HashSet<CFGNode> result) {
        HashMap<BasicBlock, CFGNode> bb2cfg_node = new HashMap<>();  // Block.CFG节点映射表(Map)，用于查找CFGNode
        for (BasicBlock bb : func.getBasicBlocks()) {
            CFGNode node = new CFGNode();
            node.bb = bb;
            node.index = node.lowlink = -1;
            node.onStack = false;
            bb2cfg_node.put(bb, node);
            result.add(node);
        }
        for (BasicBlock bb : func.getBasicBlocks()) {
            CFGNode node = bb2cfg_node.get(bb);  // 获取Block对应的CFGNode
            for (BasicBlock succ : bb.getSuccbbs()) {
                node.succs.add(bb2cfg_node.get(succ));  // 后继节点
            }
            for (BasicBlock prev : bb.getPrebbs()) {
                node.prevs.add(bb2cfg_node.get(prev));  // 前继节点
            }
        }
    }

    public boolean strongly_connected_components(HashSet<CFGNode> nodes, HashSet<HashSet<CFGNode>> result) {
        index_count = 0;
        stack.clear();
        for (CFGNode n : nodes) {
            if (n.index == -1)
                traverse(n, result);
        }
        return result.size() != 0;
    }

    public void traverse(CFGNode n, HashSet<HashSet<CFGNode>> result) {
        n.index = index_count++;   // 深搜次序
        n.lowlink = n.index;      // LOW 标记
        stack.add(n);         // DFS栈
        n.onStack = true;          // 标记在栈中
        for (CFGNode su : n.succs) {
            // has not visited su
            if (su.index == -1) {
                traverse(su, result);
                n.lowlink = min(su.lowlink, n.lowlink);
            }
            // has visited su
            else if (su.onStack) {
                n.lowlink = min(su.index, n.lowlink);
            }
            // 已搜索并添加至result的节点上述两种情况都不符合
        }
        // nodes that in the same strongly connected component will be popped out of stack
        if (n.index == n.lowlink) {
            // 找到强连通分量
            HashSet<CFGNode> set = new HashSet<>();
            CFGNode tmp;
            // 弹出n的强连通分量包含的所有节点
            do {
                tmp = stack.peek();
                tmp.onStack = false;
                set.add(tmp);
                stack.pop();
            } while (tmp != n);
            // 本实现不将单个节点视为强连通分量
            if (set.size() > 1) {
                result.add(set);
            }
        }
    }

    public CFGNode find_loop_base(HashSet<CFGNode> set, HashSet<CFGNode> reserved) {
        CFGNode base = null;
        for (CFGNode n : set) {
            for (CFGNode prev : n.prevs) {
                // 没找到（BasicBlock来自强连通分量外部）
                if (!set.contains(prev)) {
                    base = n;
                }
            }
        }
        if (base != null) {
            return base;
        }
        for (CFGNode res : reserved) {
            for (CFGNode succ : res.succs) {
                if (set.contains(succ)) {
                    base = succ;
                }
            }
        }
        return base;
    }

    @Override
    public void run() {
        for (Function func : module.getFunctions()) {
            if (func.getBasicBlocks().size() > 0) {
                HashSet<CFGNode> nodes = new HashSet<>();
                HashSet<CFGNode> reserved = new HashSet<>();
                HashSet<HashSet<CFGNode>> sccs = new HashSet<>();
                build_cfg(func, nodes);
                while (strongly_connected_components(nodes, sccs)) {
                    if (sccs.size() == 0) {
                        break;
                    } else {
                        for (HashSet<CFGNode> scc : sccs) {
                            CFGNode base = find_loop_base(scc, reserved);
                            // step 4: store result
                            HashSet<BasicBlock> bb_set = new HashSet<>();
                            // 遍历强连通分量的所有BasicBlock并插入bb_set
                            for (CFGNode n : scc) {
                                bb_set.add(n.bb);
                            }
                            loop_set.add(bb_set);// 所有循环的BasicBlock集合
                            if (!func2loop.containsKey(func)) {
                                HashSet<HashSet<BasicBlock>> val = new HashSet<>();
                                func2loop.put(func, val);
                            }
                            func2loop.get(func).add(bb_set);       // 该函数体包含的所有循环的BasicBlock集合
                            base2loop.put(base.bb, bb_set);   // 由循环入口Block获取Set
                            loop2base.put(bb_set, base.bb);   // 由Set获取循环入口
                            for (BasicBlock bb : bb_set) {
                                bb2base.put(bb, base.bb);
                            }
                            // 处理循环嵌套
                            // step 6: remove loop base node for researching inner loop
                            reserved.add(base);
                            //dump_graph(*scc, func.get_name() + '_' + std::to_string(scc_index));
                            nodes.remove(base);
                            for (CFGNode su : base.succs) {
                                su.prevs.remove(base);
                            }
                            for (CFGNode prev : base.prevs) {
                                prev.succs.remove(base);
                            }

                        }
                        sccs.clear();
                        for (CFGNode n : nodes) {
                            n.index = n.lowlink = -1;
                            n.onStack = false;
                        }
                    }
                }
                reserved.clear();
                nodes.clear();
            }
        }
    }

    public HashSet<BasicBlock> get_parent_loop(HashSet<BasicBlock> loop) {
        BasicBlock base = loop2base.get(loop);
        for (BasicBlock prev : base.getPrebbs()) {
            if (loop.contains(prev))
                continue;
            HashSet<BasicBlock> lloop = get_inner_loop(prev);
            if (lloop == null || !lloop.contains(base))
                return null;
            else {
                return lloop;
            }
        }
        return null;
    }

    public HashSet<HashSet<BasicBlock>> get_loops_in_func(Function func) {
        return func2loop.containsKey(func) ? func2loop.get(func) : new HashSet<>();
    }



















    public class CFGNode {
        public HashSet<CFGNode> succs;
        public HashSet<CFGNode> prevs;
        public BasicBlock bb;
        public int index;   // the index of the node in CFG
        public int lowlink; // the min index of the node in the strongly connected componets
        public boolean onStack;
        public CFGNode() {
            this.succs = new HashSet<>();
            this.prevs = new HashSet<>();
        }
    }

}



