package aco;

public class Candidate {
    public int salesmanIndex = -1;// default = -1
    public int originIndex;
    public int destinationIndex;
    public double visibility;//eta
    public double pheromone;//tau
    public double tau_eta;

    public Candidate(int salesmanIndex, int originIndex, int destinationIndex, double visibility, double pheromone, double alpha, double beta) {
        this.salesmanIndex = salesmanIndex;
        this.originIndex = originIndex;
        this.destinationIndex = destinationIndex;
        this.visibility = visibility;
        this.pheromone = pheromone;
        this.tau_eta = Math.pow(pheromone, alpha) * Math.pow(visibility, beta);
    }    
}
