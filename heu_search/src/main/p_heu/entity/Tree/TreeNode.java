package p_heu.entity.Tree;

import p_heu.entity.sequence.Sequence;

public class TreeNode {
    Sequence  treenode;
    boolean isRoot;
    boolean isLeaf;
    String id;//当前Sequence的编号 例如 0,1,2,3
    int success;
    int fail;
    int accessedtimes;//当前节点被访问的次数
}
