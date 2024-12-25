package mdmtsp;

public interface Algorithm {    
    public boolean init();
    public void process();
    public Individual getBestSolution();
    
}
