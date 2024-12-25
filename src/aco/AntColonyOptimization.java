package aco;

import java.util.ArrayList;
import mdmtsp.Algorithm;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.Operation;

public class AntColonyOptimization implements Algorithm {

    // variables
    private MDMTSP mdmtsp = null;
    private Individual bestSolution = null;
    private double bestFitness = 0;
    ArrayList<ArrayList<Integer>> bestRoutes = null;
    Operation operation = new Operation();

    //ACO parameters
    double[][] distanceMatrix = null;
    double[][] pheromone = null;//Tau
    double[][] visibility = null;//Eta
    int S = 0;//number of ants
    int NCMAX;//maxIterations (maximum ant cycle)
    double alpha;//pheromone importance (Konstanta pengendali pheromone (α), nilai α ≥ 0.) 
    double beta;//distance priority (Konstanta pengendali intensitas visibilitas (β), nilai β ≥ 0.)
    double rho;//Evaporation (Konstanta penguapan pheromone)
    double Q = 1;//pheromone left on train per ant (Konstanta Siklus Semut)

    public AntColonyOptimization(MDMTSP mdmtsp, int numberOfAnts, int maxIterations, double alpha, double beta, double evaporation) {
        this.mdmtsp = mdmtsp;
        this.S = numberOfAnts;
        this.NCMAX = maxIterations;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = evaporation;
    }

    @Override
    public boolean init() {
        boolean status = false;
        if (this.mdmtsp != null
                && this.mdmtsp.getDistanceMatrix() != null
                && this.S > 0
                && NCMAX > 0) {
            this.distanceMatrix = this.mdmtsp.getDistanceMatrix();
            this.visibility = new double[this.distanceMatrix.length][];
            this.pheromone = new double[this.distanceMatrix.length][];
            for (int i = 0; i < this.visibility.length; i++) {
                this.visibility[i] = new double[this.distanceMatrix[i].length];
                this.pheromone[i] = new double[this.distanceMatrix[i].length];
                for (int j = 0; j < this.visibility[i].length; j++) {
                    this.visibility[i][j] = 0;
                    this.pheromone[i][j] = 0;
                    if (this.distanceMatrix[i][j] > 0) {
                        this.visibility[i][j] = 1.0 / this.distanceMatrix[i][j];
                        this.pheromone[i][j] = 1;
                    }
                }
            }
            // initialize best solution
            bestSolution = null;
            bestFitness = 0;
            bestRoutes = null;
            status = true;
        }
        return status;
    }

    @Override
    public void process() {
        if (init()) {
            int numberOfSalesmans = mdmtsp.getNumberOfSalesmans();//NUMBER_OF_SALESMANS
            ArrayList<Integer> depots = mdmtsp.getDepots();
            ArrayList<Integer> customers = mdmtsp.getCustomers();
            //ant life cycle ---------------------------------------------------
            int c = 0;
            while (c < NCMAX) {
                // INITIALIZE delta Tau ----------------------------------------
                double[][] sigmaDeltaTau = new double[this.distanceMatrix.length][];
                for (int i = 0; i < sigmaDeltaTau.length; i++) {
                    sigmaDeltaTau[i] = new double[this.distanceMatrix[i].length];
                    for (int j = 0; j < sigmaDeltaTau[i].length; j++) {
                        sigmaDeltaTau[i][j] = 0;
                    }
                }
                // end of INITIALIZE delta Tau ---------------------------------

                // ant built the route -----------------------------------------
                for (int ant = 1; ant <= S; ant++) {
                    ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
                    ArrayList<Integer> visitedCustomers = new ArrayList<>();// collect the visited customers
                    while (visitedCustomers.size() < customers.size()) {
                        ArrayList<Candidate> candidates = new ArrayList<>();
                        double denominator = 0;
                        // check all depots
                        if (routes.size() < numberOfSalesmans) {// jika masih ada salesman yang belum digunakan
                            for (int d = 0; d < depots.size(); d++) {
                                // check all unvisited customers which adjacent to depot-d
                                int depotIndex = depots.get(d);
                                for (int j = 0; j < this.distanceMatrix[depotIndex].length; j++) {
                                    if (j != depotIndex && customers.contains(j) && !visitedCustomers.contains(j) && this.distanceMatrix[depotIndex][j] > 0) {
                                        int salesmanIndex = -1;
                                        int originIndex = depotIndex;
                                        int destinationIndex = j;
                                        double eta = visibility[originIndex][destinationIndex];
                                        double tau = pheromone[originIndex][destinationIndex];
                                        Candidate candidate = new Candidate(salesmanIndex, originIndex, destinationIndex, eta, tau, alpha, beta);
                                        denominator += candidate.tau_eta;
                                        candidates.add(candidate);
                                    }
                                }
                            }
                        }// end of if (routes.size() < numberOfSalesmans)
                        // check candidater from active salesmans
                        if (!routes.isEmpty()) {
                            for (int p = 0; p < routes.size(); p++) {
                                int salesmanIndex = p;
                                ArrayList<Integer> routeOfSalesman = routes.get(p);
                                if (!routeOfSalesman.isEmpty()) {
                                    int tail = routeOfSalesman.get(routeOfSalesman.size() - 1);
                                    // check all unvisited customers which adjacent to tail
                                    for (int j = 0; j < this.distanceMatrix[tail].length; j++) {
                                        if (j != tail && customers.contains(j) && !visitedCustomers.contains(j) && this.distanceMatrix[tail][j] > 0) {
                                            int originIndex = tail;
                                            int destinationIndex = j;
                                            double eta = visibility[originIndex][destinationIndex];
                                            double tau = pheromone[originIndex][destinationIndex];
                                            Candidate candidate = new Candidate(salesmanIndex, originIndex, destinationIndex, eta, tau, alpha, beta);
                                            denominator += candidate.tau_eta;
                                            candidates.add(candidate);
                                        }
                                    }
                                }

                            }
                        }// end of if (!routes.isEmpty())

                        // calculate the ants probability
                        if (!candidates.isEmpty() && denominator > 0) {
                            double[] cumulativeProbability = new double[candidates.size()];
                            double totalProbability = 0;
                            for (int i = 0; i < candidates.size(); i++) {
                                double numerator = candidates.get(i).tau_eta;
                                double probability = numerator / denominator;
                                totalProbability += probability;
                                cumulativeProbability[i] = totalProbability;
                            }

                            double randomProbability = operation.randomUniform() * totalProbability;
                            int selectedIndex = -1;
                            for (int i = 0; i < cumulativeProbability.length; i++) {
                                if (cumulativeProbability[i] >= randomProbability) {
                                    selectedIndex = i;
                                    break;
                                }
                            }
                            // if selectedIndex > -1 then set new edge
                            if (selectedIndex > -1) {
                                Candidate selected = candidates.get(selectedIndex);
                                int salesmanIndex = selected.salesmanIndex;
                                if (salesmanIndex == -1) {
                                    ArrayList<Integer> routeOfSalesman = new ArrayList<>();
                                    int origin = selected.originIndex;
                                    int destination = selected.destinationIndex;
                                    routeOfSalesman.add(origin);
                                    routeOfSalesman.add(destination);
                                    routes.add(routeOfSalesman);
                                    visitedCustomers.add(destination);
                                } else {
                                    int destination = selected.destinationIndex;
                                    routes.get(salesmanIndex).add(destination);
                                    visitedCustomers.add(destination);
                                }
                            }
                        }// end of if (!candidates.isEmpty() && denominator > 0)

                    }// end while

                    // calculate ant tour
                    double distance = 0;
                    if (!routes.isEmpty()) {
                        for (int p = 0; p < routes.size(); p++) {
                            ArrayList<Integer> routeOfSalesman = routes.get(p);
                            double d = 0;
                            for (int i = 1; i < routeOfSalesman.size(); i++) {
                                int origin = routeOfSalesman.get(i - 1);
                                int destination = routeOfSalesman.get(i);
                                d += this.distanceMatrix[origin][destination];
                            }
                            // go home
                            int origin = routeOfSalesman.get(routeOfSalesman.size() - 1);
                            int destination = routeOfSalesman.get(0);
                            d += this.distanceMatrix[origin][destination];
                            distance += d;
                        }
                    }

                    // calculate fitness
                    double fitness = 0;
                    if (distance > 0) {
                        fitness = 1.0 / distance;
                    }

                    //SAVE BEST SOLUTION
                    if (fitness > bestFitness) {
                        bestFitness = fitness;
                        bestRoutes = routes;
                    }

                    // Update sigmaDeltaTau for ants
                    if (!routes.isEmpty()) {
                        double Lk = distance;
                        double additionalPheromones = Q / Lk;
                        for (int p = 0; p < routes.size(); p++) {
                            ArrayList<Integer> routesOfSalesman = routes.get(p);
                            for (int i = 1; i < routesOfSalesman.size(); i++) {
                                int origin = routesOfSalesman.get(i - 1);
                                int destination = routesOfSalesman.get(i);
                                sigmaDeltaTau[origin][destination] += additionalPheromones;//update SIGMA delta Tau xy
                                sigmaDeltaTau[destination][origin] += additionalPheromones;
                            }
                            // go home
                            int origin = routesOfSalesman.get(routesOfSalesman.size() - 1);
                            int destination = routesOfSalesman.get(0);
                            sigmaDeltaTau[origin][destination] += additionalPheromones;//update SIGMA delta Tau xy
                            sigmaDeltaTau[destination][origin] += additionalPheromones;
                        }
                    }

                }// end of for (int ant = 1; ant <= S; ant++)

                //UPDATE PHEROMONE (Tau)
                for (int i = 0; i < pheromone.length; i++) {
                    for (int j = 0; j < pheromone[i].length; j++) {
                        pheromone[i][j] = (1.0 - rho) * pheromone[i][j] + sigmaDeltaTau[i][j];
                    }
                }

                c++; // increment cycle

            }// end of ant life cycle ------------------------------------------

            // Construct Ant Solution
            if (bestFitness > 0 && bestRoutes != null) {
                bestSolution = new Individual(mdmtsp);
                int[][] path = new int[bestRoutes.size()][];
                for (int i = 0; i < path.length; i++) {
                    ArrayList<Integer> row = bestRoutes.get(i);
                    path[i] = new int[row.size()];
                    for (int j = 0; j < path[i].length; j++) {
                        path[i][j] = row.get(j);
                    }
                }
                bestSolution.setPath(path);
                bestSolution.calculateFitness();
            }

        }//end of if (init())       
    }

    @Override
    public Individual getBestSolution() {
        return this.bestSolution;
    }

}
