package pso;

import java.util.ArrayList;
import mdmtsp.Algorithm;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.Operation;
import mdmtsp.SearchOption;

public class ParticleSwarmOptimization implements Algorithm {

    // variables
    private MDMTSP mdmtsp = null;
    private Individual bestSolution = null;
    Operation operation = new Operation();

    // List Of Operations used    
    // Search Options. Only SearchOption.NONE or SearchOption.PARTIAL allowed
    // SearchOption.DEFAULT = SearchOption.PARTIAL
    private SearchOption swapOperation = SearchOption.NONE;
    private SearchOption slideOperation = SearchOption.NONE;
    private SearchOption flipOperation = SearchOption.NONE;
    private SearchOption breakpointOperation = SearchOption.NONE;
    private SearchOption startDepotOperation = SearchOption.NONE;

    // PSO PARAMETERS
    private int I;      //Total Number of Iterations
    private int N;      //Total Number of Particles
    private double pr;  //Perturbation Rate

    //VARIABLES
    private int t = 0;//iteration counter
    private Particle[] particle = null;//particles = population of particles
    private Individual globalBest = null;

    public ParticleSwarmOptimization(MDMTSP mdmtsp, int populationSize, int MAX_ITERATION, double perturbationRate) {
        this.mdmtsp = mdmtsp;
        this.N = populationSize;
        this.I = MAX_ITERATION;
        this.pr = perturbationRate;
    }
    
    public void setSearchOption(SearchOption swapOperation, SearchOption slideOperation, SearchOption flipOperation, SearchOption breakpointOperation, SearchOption startDepotOperation) {
        this.swapOperation = swapOperation;
        this.slideOperation = slideOperation;
        this.flipOperation = flipOperation;
        this.breakpointOperation = breakpointOperation;
        this.startDepotOperation = startDepotOperation;
    }

    @Override
    public boolean init() {
        boolean status = false;
        if (this.mdmtsp != null
                && I > 0
                && N > 0) {
            // initialize particles
            particle = new Particle[N];
            // generate random particle
            double globalFitness = 0;
            int indexOfGLobalSolution = -1;
            for (int i = 0; i < N; i++) {
                Individual previousBest = null;
                int[][] velocity = null;
                Individual solution = new Individual(this.mdmtsp);
                solution.generateRandomChromosome();
                solution.calculateFitness();
                previousBest = solution.clone();
                particle[i] = new Particle(previousBest, velocity, solution);
                // Elitism
                if (globalFitness < solution.getFitness()) {
                    globalFitness = solution.getFitness();
                    indexOfGLobalSolution = i;
                }
            }
            globalBest = particle[indexOfGLobalSolution].getSolution().clone();
            status = true;
        }
        return status;
    }

    @Override
    public void process() {
        if (init()) {
            int customerSize = mdmtsp.getCustomers().size();
            while (t <= I) {
                for (int i = 0; i < N; i++) {
                    Individual previousParticle = particle[i].getSolution();// xi(t-1)
                    Individual previousBest = particle[i].getPreviousBest();
                    int[][] previousVelocity = particle[i].getVelocity();

                    // CALCULATE NEW VELOCITY
                    double alpha = operation.randomUniform();//U(0,1)
                    double beta = operation.randomUniform();//U(0,1)

                    ArrayList<int[]> velocity = new ArrayList<>();
                    if (previousVelocity != null) {
                        for (int[] v : previousVelocity) {
                            velocity.add(v.clone());
                        }
                    }

                    // interaction with previousBest
                    if (alpha >= pr) {
                        int[][] velocityToPreviousBest = operation.subtraction(previousParticle, previousBest);
                        if (velocityToPreviousBest != null) {
                            for (int[] v : velocityToPreviousBest) {
                                velocity.add(v.clone());
                            }
                        }
                    }

                    // interaction with global solution
                    if (beta >= pr) {
                        int[][] velocityToGlobalBest = operation.subtraction(previousParticle, globalBest);
                        if (velocityToGlobalBest != null) {
                            for (int[] v : velocityToGlobalBest) {
                                velocity.add(v.clone());
                            }
                        }
                    }

                    int[][] VSS = null;//velocity swap sequence
                    if (!velocity.isEmpty()) {
                        VSS = new int[velocity.size()][2];
                        for (int j = 0; j < velocity.size(); j++) {
                            VSS[j] = velocity.get(j).clone();
                        }
                    }

                    // transform to BSS (basic swap sequence
                    int[][] BSS = operation.callBasicSwapSequence(VSS, customerSize);

                    // UPDATE Xi(t)
                    Individual newParticle = previousParticle.clone();
                    if (BSS != null && BSS.length > 0) {
                        if (swapOperation == SearchOption.DEFAULT) {
                            operation.swapSequence(newParticle, BSS);
                        } else if (swapOperation == SearchOption.PARTIAL) {
                            newParticle = operation.swapSequenceWithPartialSearch(newParticle, BSS);
                        }
                    }

                    // UPDATE Xi using Reproduction Mechanism
                    double u = operation.randomUniform();//U(0,1)
                    if (u >= pr) {
                        newParticle = mutation(newParticle);
                    }

                    // UPDATE VELOCITY
                    int[][] newVelocity = operation.subtraction(previousParticle, newParticle);
                    newParticle.calculateFitness();

                    // UPDATE Pi
                    if (newParticle.getFitness() > previousBest.getFitness()) {
                        previousBest = newParticle.clone();
                    }

                    // UPDATE PARTICLE_i
                    particle[i] = new Particle(previousBest, newVelocity, newParticle);

                    // UPDATE G
                    if (previousBest.getFitness() > globalBest.getFitness()) {
                        globalBest = previousBest.clone();
                    }
                }

                //increment t = t+1
                t++;
            }
            bestSolution = globalBest;
        }// end of if (init())
    }

    public Individual mutation(Individual individual) {
        Individual newIndividual = individual.clone();
        int routeSize = newIndividual.getRoute().length;       

        // SLIDE OPERATION -----------------------------------------------------
        if (slideOperation != SearchOption.NONE) {
            // random two crossover points
            int fromIndex = operation.randomBetween(0, routeSize - 1);
            int toIndex = fromIndex;
            while (toIndex == fromIndex) {
                toIndex = operation.randomBetween(0, routeSize - 1);
            }
            if (fromIndex > toIndex) {
                int temp = fromIndex;
                fromIndex = toIndex;
                toIndex = temp;
            }
            int slideUnit = operation.randomBetween(1, Math.abs(toIndex - fromIndex));
            // set the result
            if (slideOperation == SearchOption.DEFAULT) {
                operation.slideRoute(newIndividual, fromIndex, toIndex, slideUnit);
            } else if (slideOperation == SearchOption.PARTIAL) {
                newIndividual = operation.slideRouteWithPartialSearch(newIndividual, fromIndex, toIndex, slideUnit);
            }
        }// end of SLIDE OPERATION ---------------------------------------------

        // FLIP OPERATION ------------------------------------------------------
        if (flipOperation != SearchOption.NONE) {
            // random two crossover points
            int fromIndex = operation.randomBetween(0, routeSize - 1);
            int toIndex = fromIndex;
            while (toIndex == fromIndex) {
                toIndex = operation.randomBetween(0, routeSize - 1);
            }
            if (fromIndex > toIndex) {
                int temp = fromIndex;
                fromIndex = toIndex;
                toIndex = temp;
            }
            // set the result
            if (flipOperation == SearchOption.DEFAULT) {
                operation.flipRoute(newIndividual, fromIndex, toIndex);
            } else if (flipOperation == SearchOption.PARTIAL) {
                newIndividual = operation.flipRouteWithPartialSearch(newIndividual, fromIndex, toIndex);
            }
        }// end of FLIP OPERATION ----------------------------------------------

        // BREAKPOINT OPERATION ------------------------------------------------
        if (breakpointOperation != SearchOption.NONE) {
            // set the result
            if (breakpointOperation == SearchOption.DEFAULT) {
                operation.breakpointMutation(newIndividual);
            } else if (breakpointOperation == SearchOption.PARTIAL) {
                newIndividual = operation.breakpointMutationWithPartialSearch(newIndividual);
            }
        }// end of BREAKPOINT OPERATION ----------------------------------------

        // START DEPOT OPERATION -----------------------------------------------
        if (startDepotOperation != SearchOption.NONE) {
            // set the result
            if (startDepotOperation == SearchOption.DEFAULT) {
                operation.startDepotMutation(newIndividual);
            } else if (startDepotOperation == SearchOption.PARTIAL) {
                newIndividual = operation.startDepotMutationWithPartialSearch(newIndividual);
            }
        }// end of START DEPOT OPERATION ---------------------------------------

        // set new Spider Monkey - i
        //newIndividual.calculateFitness();
        return newIndividual;
    }

    @Override
    public Individual getBestSolution() {
        return this.bestSolution;
    }

}
