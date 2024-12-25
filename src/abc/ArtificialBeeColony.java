package abc;

import java.util.Random;
import mdmtsp.Algorithm;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.Operation;
import mdmtsp.SearchOption;

public class ArtificialBeeColony implements Algorithm {

    // variables
    private MDMTSP mdmtsp = null;
    private Individual bestSolution = null;
    private double bestFitness = 0;
    Operation operation = new Operation();

    // List Of Operations used    
    // Search Options. Only SearchOption.NONE or SearchOption.PARTIAL allowed
    // SearchOption.DEFAULT = SearchOption.PARTIAL
    private SearchOption swapOperation = SearchOption.NONE;
    private SearchOption slideOperation = SearchOption.NONE;
    private SearchOption flipOperation = SearchOption.NONE;
    private SearchOption breakpointOperation = SearchOption.NONE;
    private SearchOption startDepotOperation = SearchOption.NONE;

    // ABC parameters
    private int populationSize;
    private int limit = 3; // lim is a positive integer which is used as the upper limit of the food source counter
    private int MAX_ITERATION = 100;

    // VARIABLE
    private int numberOfEmployeeBee;
    private int numberOfOnlookerBee;
    private int numberOfScoutBee;

    public ArtificialBeeColony(MDMTSP mdmtsp, int populationSize, int MAX_GENERATION, int upperLimitOfFoodSourceCounter) {
        this.mdmtsp = mdmtsp;
        this.populationSize = populationSize;
        this.MAX_ITERATION = MAX_GENERATION;
        this.limit = upperLimitOfFoodSourceCounter;
        //---------------------------------------
        numberOfEmployeeBee = this.populationSize;
        numberOfOnlookerBee = this.populationSize;
        numberOfScoutBee = this.populationSize;
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
                && this.populationSize > 0
                && this.MAX_ITERATION >= 0
                && this.limit >= 0) {
            if (this.populationSize < 2) {
                this.populationSize = 2;
            }
            if (this.limit < 0) {
                this.limit = 0;
            }
            status = true;
        }
        return status;
    }

    @Override
    public void process() {
        if (init()) {
            // Initialize random population-------------------------------------
            Individual[] population = new Individual[populationSize];
            int[] trial = new int[populationSize];

            for (int i = 0; i < population.length; i++) {
                population[i] = new Individual(mdmtsp);
                population[i].generateRandomChromosome();
                population[i].calculateFitness();
                trial[i] = 0;
                // ELITISM
                if (population[i].getFitness() > bestFitness) {
                    bestSolution = population[i].clone();
                    bestFitness = bestSolution.getFitness();
                }
            }
            // end of Initialize random population------------------------------

            // BEE COLONY ITERATION --------------------------------------------
            for (int iteration = 1; iteration <= MAX_ITERATION; iteration++) {
                // EMPLOYEE BEE PHASE ==========================================
                for (int i = 0; i < numberOfEmployeeBee; i++) {
                    Individual candidate = mutation(population[i]);
                    // GREEDY RULE
                    candidate.calculateFitness();
                    if (candidate.getFitness() > population[i].getFitness()) {
                        population[i] = candidate;
                        trial[i] = 0;
                    } else {
                        trial[i]++;
                    }

                }
                // end of EMPLOYEE BEE PHASE ===================================

                // ONLOOKER BEE PHASE ==========================================
                int k = 0;
                while (k < numberOfOnlookerBee) {
                    double totalFitness = 0;
                    for (int i = 0; i < populationSize; i++) {
                        totalFitness += population[i].getFitness();
                    }
                    double[] probability = new double[populationSize];
                    for (int i = 0; i < populationSize; i++) {
                        probability[i] = population[i].getFitness() / totalFitness;
                    }
                    for (int i = 0; i < populationSize; i++) {
                        if (k < numberOfOnlookerBee) {
                            double r = new Random().nextDouble();
                            if (r < probability[i]) {
                                k++;
                                
                                Individual candidate = mutation(population[i]);

                                // GREEDY RULE
                                candidate.calculateFitness();
                                if (candidate.getFitness() > population[i].getFitness()) {
                                    population[i] = candidate;
                                    trial[i] = 0;
                                } else {
                                    trial[i]++;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                // end of ONLOOKER BEE PHASE ===================================

                // SCOUT BEE PHASE =============================================
                for (int i = 0; i < numberOfScoutBee; i++) {
                    if (trial[i] > limit) {
                        population[i] = new Individual(mdmtsp);
                        population[i].generateRandomChromosome();
                        population[i].calculateFitness();
                        trial[i] = 0;
                    }
                }
                // end of SCOUT BEE PHASE ======================================

                // SAVE BEST SOLUTION ==========================================
                for (int i = 0; i < populationSize; i++) {
                    // ELITISM
                    if (population[i].getFitness() > bestSolution.getFitness()) {
                        bestSolution = population[i].clone();
                    }
                }
                // end of SAVE BEST SOLUTION ===================================

            }
            // end of BEE COLONY ITERATION -------------------------------------

        }// end of if (init())
    }

    public Individual mutation(Individual individual) {
        Individual candidate = individual.clone();
        int routeSize = candidate.getRoute().length;

        // SWAP OPERATION ------------------------------------------
        if (swapOperation == SearchOption.DEFAULT || swapOperation == SearchOption.PARTIAL) {
            int numberOfSwapOperations = operation.randomBetween(1, (int) Math.floor(routeSize / 2.0));
            int[][] swapOperations = new int[numberOfSwapOperations][2];
            for (int j = 0; j < swapOperations.length; j++) {
                int index1 = operation.randomBetween(0, routeSize - 1);
                int index2 = index1;
                while (index2 == index1) {
                    index2 = operation.randomBetween(0, routeSize - 1);
                }
                swapOperations[j][0] = index1;
                swapOperations[j][1] = index2;
            }
            if (swapOperation == SearchOption.PARTIAL) {
                candidate = operation.swapSequenceWithPartialSearch(candidate, swapOperations);
            } else {
                operation.swapSequence(candidate, swapOperations);
            }
        }
        // end of SWAP OPERATION -----------------------------------

        // SLIDE OPERATION -----------------------------------------
        if (slideOperation == SearchOption.DEFAULT || slideOperation == SearchOption.PARTIAL) {
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
                operation.slideRoute(candidate, fromIndex, toIndex, slideUnit);
            } else if (slideOperation == SearchOption.PARTIAL) {
                candidate = operation.slideRouteWithPartialSearch(candidate, fromIndex, toIndex, slideUnit);
            }
        }
        // end of SLIDE OPERATION ----------------------------------

        // FLIP OPERATION ------------------------------------------
        if (flipOperation == SearchOption.DEFAULT || flipOperation == SearchOption.PARTIAL) {
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
                operation.flipRoute(candidate, fromIndex, toIndex);
            } else if (flipOperation == SearchOption.PARTIAL) {
                candidate = operation.flipRouteWithPartialSearch(candidate, fromIndex, toIndex);
            }
        }
        // end of FLIP OPERATION -----------------------------------

        // BREAKPOINT OPERATION ------------------------------------
        if (breakpointOperation == SearchOption.DEFAULT || breakpointOperation == SearchOption.PARTIAL) {
            // set the result
            if (breakpointOperation == SearchOption.DEFAULT) {
                operation.breakpointMutation(candidate);
            } else if (breakpointOperation == SearchOption.PARTIAL) {
                candidate = operation.breakpointMutationWithPartialSearch(candidate);
            }
        }
        // end of BREAKPOINT OPERATION -----------------------------

        // START DEPOT OPERATION -----------------------------------
        if (startDepotOperation == SearchOption.DEFAULT || startDepotOperation == SearchOption.PARTIAL) {
            // set the result
            if (startDepotOperation == SearchOption.DEFAULT) {
                operation.startDepotMutation(candidate);
            } else if (startDepotOperation == SearchOption.PARTIAL) {
                candidate = operation.startDepotMutationWithPartialSearch(candidate);
            }
        }
        // end of START DEPOT OPERATION ----------------------------
        //candidate.calculateFitness();
        return candidate;
    }

    @Override
    public Individual getBestSolution() {
        return this.bestSolution;
    }

}
