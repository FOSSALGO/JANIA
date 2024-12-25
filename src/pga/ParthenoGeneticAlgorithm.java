package pga;

import mdmtsp.Algorithm;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.Operation;
import mdmtsp.SearchOption;

public class ParthenoGeneticAlgorithm implements Algorithm {

    // variables
    private MDMTSP mdmtsp = null;
    private Individual bestSolution = null;
    private double bestFitness = 0;
    Operation operation = new Operation();

    //GA parameters
    private final int MAX_GENERATION;
    private int populationSize;

    // List Of Operations used    
    // Search Options. Only SearchOption.NONE or SearchOption.PARTIAL allowed
    // SearchOption.DEFAULT = SearchOption.PARTIAL
    private SearchOption swapOperation = SearchOption.NONE;
    private SearchOption slideOperation = SearchOption.NONE;
    private SearchOption flipOperation = SearchOption.NONE;
    private SearchOption breakpointOperation = SearchOption.NONE;
    private SearchOption startDepotOperation = SearchOption.NONE;

    private int numberOfIndividualsSelected;
    private final double mutationRate;

    public ParthenoGeneticAlgorithm(MDMTSP mdmtsp, int populationSize, int MAX_GENERATION, double mutationRate) {
        this.mdmtsp = mdmtsp;
        this.populationSize = populationSize;
        this.MAX_GENERATION = MAX_GENERATION;
        this.mutationRate = mutationRate;
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
                && this.MAX_GENERATION >= 0
                && this.mutationRate >= 0) {
            if (this.populationSize < 2) {
                this.populationSize = 2;
            }
            if (numberOfIndividualsSelected <= 0) {
                numberOfIndividualsSelected = 2;
            } else if (numberOfIndividualsSelected > populationSize) {
                numberOfIndividualsSelected = (int) Math.ceil(populationSize / 2.0);
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
            for (int i = 0; i < population.length; i++) {
                population[i] = new Individual(mdmtsp);
                population[i].generateRandomChromosome();
                population[i].calculateFitness();
                // ELITISM
                if (population[i].getFitness() > bestFitness) {
                    bestSolution = population[i].clone();
                    bestFitness = bestSolution.getFitness();
                }
            }
            // end of Initialize random population------------------------------

            // Evolution Process------------------------------------------------
            for (int g = 1; g <= this.MAX_GENERATION; g++) {
                Individual[] newPopulation = new Individual[populationSize];

                // SORTING =====================================================                              
                double[][] fitness = new double[populationSize][2];//[index | fitness]
                for (int i = 0; i < populationSize; i++) {
                    fitness[i][0] = i;// index
                    fitness[i][1] = population[i].getFitness();//fitness value
                }
                fitness = operation.sortFitnessDescending(fitness);
                // save selected individual
                for (int i = 0; i < populationSize; i++) {
                    int index = (int) fitness[i][0];
                    newPopulation[i] = population[index].clone();
                }
                // end of SORTING ==============================================   

                // REPRODUCTION OPERATION=======================================
                for (int i = 0; i < populationSize; i++) {
                    double rm = operation.randomUniform();
                    if (rm > mutationRate) {
                        Individual newIndividual = mutation(newPopulation[i]);

                        // set mutant as new individual
                        newPopulation[i] = newIndividual.clone();
                    }
                }
                // end of REPRODUCTION OPERATION ===============================

                // SET NEW POPULATION ==========================================
                for (int i = 0; i < populationSize; i++) {
                    population[i] = newPopulation[i].clone();
                    population[i].calculateFitness();
                    // ELITISM
                    if (population[i].getFitness() > bestFitness) {
                        bestSolution = population[i];
                        bestFitness = bestSolution.getFitness();
                    }
                }
                // end of SET NEW POPULATION ===================================
            }
            // end of Evolution Process-----------------------------------------
        }
    }

    public Individual mutation(Individual individual) {
        Individual newIndividual = individual.clone();
        int routeSize = newIndividual.getRoute().length;

        // SWAP OPERATION ------------------------------------------------------
        if (swapOperation != SearchOption.NONE) {
            int numberOfMutationPoints = operation.randomBetween(1, (int) Math.floor(routeSize / 2.0));
            int[][] swapOperations = new int[numberOfMutationPoints][2];
            for (int j = 0; j < swapOperations.length; j++) {
                int index1 = operation.randomBetween(0, routeSize - 1);
                int index2 = index1;
                while (index2 == index1) {
                    index2 = operation.randomBetween(0, routeSize - 1);
                }
                swapOperations[j][0] = index1;
                swapOperations[j][1] = index2;
            }
            // set the result
            if (swapOperation == SearchOption.DEFAULT) {
                operation.swapSequence(newIndividual, swapOperations);
            } else if (swapOperation == SearchOption.PARTIAL) {
                newIndividual = operation.swapSequenceWithPartialSearch(newIndividual, swapOperations);
            }
        }// end of SWAP OPERATION -----------------------------------------------

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
