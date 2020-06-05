package p_heu.entity.sequence;

public class ChildNode {
    private String index;
    private Boolean isFullyExplored;

    public ChildNode()
    {

    }

    public ChildNode(String index,boolean ifFullyExplored)
    {
        this.index=index;
        this.isFullyExplored=ifFullyExplored;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Boolean getFullyExplored() {
        return isFullyExplored;
    }

    public void setFullyExplored(Boolean fullyExplored) {
        isFullyExplored = fullyExplored;
    }

    public ChildNode copy()
    {
        return new ChildNode(this.index,this.isFullyExplored);
    }

}
