package smo;

import mdmtsp.Algorithm;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.Operation;
import mdmtsp.SearchOption;

public class SpiderMonkeyOptimization implements Algorithm {

    // SMO Parameters
    //INPUT
    private int I;      //Total Number of Iterations
    private int N;      //Total Number of Spider Monkey
    private int MG;     //Allowed Maximum Group
    private double pr;  //Perturbation Rate
    private int LLL;    //Local Leader Limit
    private int GLL;    //Global Leader Limit

    // variables
    private MDMTSP mdmtsp = null;
    private Individual bestSolution = null;
    Operation operation = new Operation();

    // List Of Operations used    
    // Search Options
    private SearchOption swapOperation = SearchOption.DEFAULT;
    private SearchOption slideOperation = SearchOption.NONE;
    private SearchOption flipOperation = SearchOption.NONE;
    private SearchOption breakpointOperation = SearchOption.NONE;
    private SearchOption startDepotOperation = SearchOption.NONE;

    //VARIABLES
    private int t = 0;//iteration counter
    private int g = 0;//Current Number of Group
    private int groupSize = 1;//banyaknya spider monkey di setiap group
    private Individual[] spiderMonkey = null;//SM = Population of Spider Monkey
    private Individual globalLeader = null;
    private int globalLeaderLimitCounter = 0;//GLLc = Global Leader Limit Counter
    private Individual[] localLeader = null;//LL = List of Local Leader
    private int[] localLeaderLimitCounter = null;// = new int[g];//LLLc = Local Leader Limit Counter of kth Group

    public SpiderMonkeyOptimization(MDMTSP mdmtsp, int populationSize, int MAX_ITERATION, int ALLOWED_MAX_GROUP, double perturbationRate, int localLeaderLimit, int globalLeaderLimit) {
        this.mdmtsp = mdmtsp;
        this.N = populationSize;
        this.I = MAX_ITERATION;
        this.MG = ALLOWED_MAX_GROUP;
        this.pr = perturbationRate;
        this.LLL = localLeaderLimit;
        this.GLL = globalLeaderLimit;
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
            // Initialization---------------------------------------------------
            if (this.MG < (N / 2)) {
                this.MG = N / 2;
            }
            if (this.MG <= 0) {
                this.MG = 1;
            }
            this.t = 1;//(1) t ← 1
            this.spiderMonkey = new Individual[N];//(2) create N spider moneys and append them to SM

            //(3)Assign each SMi in SM with a random solution
            int indexOfGlobalLeader = -1;
            double globalFitness = 0;
            for (int i = 0; i < N; i++) {
                spiderMonkey[i] = new Individual(this.mdmtsp);
                spiderMonkey[i].generateRandomChromosome();
                spiderMonkey[i].calculateFitness();
                //System.out.println(spiderMonkey[i]);
                if (globalFitness < spiderMonkey[i].getFitness()) {
                    globalFitness = spiderMonkey[i].getFitness();
                    indexOfGlobalLeader = i;
                }
            }

            //(4) g = 1 initially consider all spiderMonkey into one group
            this.g = 1;
            this.groupSize = (int) Math.floor((double) N / (double) g);

            //(5) Select Local Leader and Global Leader // Both leaders are same due to single group
            //GLOBAL LEADER
            this.globalLeader = spiderMonkey[indexOfGlobalLeader].clone();
            this.globalLeaderLimitCounter = 0;//GLLc

            //LOCAL LEADER
            this.localLeader = new Individual[g];
            this.localLeader[0] = this.globalLeader.clone();
            this.localLeaderLimitCounter = new int[g];//LLLc
            status = true;
        }
        return status;
    }

    public int[] getBound(int k) {
        int[] bound = null;
        //set lower and upper bound
        int lowerBound = k * groupSize;
        int upperBound = lowerBound + groupSize - 1;
        if (k == g - 1) {
            upperBound = N - 1;
        }
        if (lowerBound >= 0 && lowerBound < N && upperBound >= 0 && upperBound < N) {
            bound = new int[2];
            bound[0] = lowerBound;
            bound[1] = upperBound;
        }
        return bound;
    }

    @Override
    public void process() {
        if (init()) {
            while (t <= I) {
                //System.out.println("GROUP: "+g);
                //==============================================================
                // [1] Update of Spider Monkeys
                //==============================================================                
                //[1.1] UPDATE Spider Monkeys base on local Leader ------------- 
                for (int k = 0; k < g; k++) {
                    int[] bound = getBound(k);
                    if (bound != null) {
                        int lowerBound = bound[0];
                        int upperBound = bound[1];
                        //update spider monkey
                        for (int i = lowerBound; i <= upperBound; i++) {
                            double u = operation.randomUniform();//U(0,1)
                            if (u >= pr) {
                                int r = operation.randomBetween(lowerBound, upperBound);
                                Individual SMi = spiderMonkey[i];
                                Individual LLk = localLeader[k];
                                Individual RSMr = spiderMonkey[r];

                                // substract
                                int[][] ss1 = operation.subtraction(SMi, LLk);
                                int[][] ss2 = operation.subtraction(SMi, RSMr);
                                int[][] ss = operation.merge(ss1, ss2);

                                // Apply SS into SMi to calculate newSM
                                if (swapOperation == SearchOption.PARTIAL) {
                                    spiderMonkey[i] = operation.swapSequenceWithPartialSearch(SMi, ss);
                                } else {
                                    operation.swapSequence(spiderMonkey[i], ss);
                                }

                            }
                        }
                    }// end of if (bound != null)
                }// end of UPDATE Spider Monkeys base on local Leader

                //[1.2] UPDATE Spider Monkeys base on global Leader ------------
                for (int i = 0; i < N; i++) {
                    double u = operation.randomUniform();//U(0,1)
                    double prob = 0.9 * ((double) globalLeader.getDistance() / (double) spiderMonkey[i].getDistance()) + 0.1;//prob(i)
                    if (u <= prob) {
                        int r = operation.randomBetween(0, N - 1);
                        Individual SMi = spiderMonkey[i];
                        Individual GL = globalLeader;
                        Individual RSMr = spiderMonkey[r];
                        // substract
                        int[][] ss1 = operation.subtraction(SMi, GL);
                        int[][] ss2 = operation.subtraction(SMi, RSMr);
                        int[][] ss = operation.merge(ss1, ss2);

                        // Apply SS into SMi to calculate newSM
                        if (swapOperation == SearchOption.PARTIAL) {
                            spiderMonkey[i] = operation.swapSequenceWithPartialSearch(SMi, ss);
                        } else {
                            operation.swapSequence(spiderMonkey[i], ss);
                        }
                    }
                }// end of UPDATE Spider Monkeys base on global Leader

                //[1.2] UPDATE Spider Monkeys using improve operation ----------
                for (int i = 0; i < N; i++) {
                    double u = operation.randomUniform();//U(0,1)
                    if (u >= pr) {
                        Individual newSMi = mutation(spiderMonkey[i]);
                        spiderMonkey[i] = newSMi;
                    }// end of if (u >= pr)
                    spiderMonkey[i].calculateFitness();
                }// end of UPDATE Spider Monkeys using improve operation -------

                //==============================================================
                //[2] Update of Local Leaders and Global Leader
                //==============================================================
                Individual newGlobalLeader = globalLeader.clone();
                //[2.1] check new local leader ---------------------------------
                for (int k = 0; k < g; k++) {
                    int[] bound = getBound(k);
                    if (bound != null) {
                        int lowerBound = bound[0];
                        int upperBound = bound[1];
                        Individual newLocalLeader = localLeader[k].clone();
                        for (int i = lowerBound; i <= upperBound; i++) {
                            if (spiderMonkey[i].getFitness() > newLocalLeader.getFitness()) {
                                newLocalLeader = spiderMonkey[i];
                            }
                        }

                        if (newLocalLeader.getFitness() > localLeader[k].getFitness()) {
                            localLeader[k] = newLocalLeader.clone();
                            localLeaderLimitCounter[k] = 0;
                        } else {
                            localLeaderLimitCounter[k]++;//localLeaderLimitCounter[k] = localLeaderLimitCounter[k] + 1;
                        }

                        if (localLeader[k].getFitness() > newGlobalLeader.getFitness()) {
                            newGlobalLeader = localLeader[k];
                        }
                    }
                }// end of check new local leader

                //[2.2] check new global leader --------------------------------
                if (newGlobalLeader.getFitness() > globalLeader.getFitness()) {
                    globalLeader = newGlobalLeader.clone();
                    globalLeaderLimitCounter = 0;
                } else {
                    globalLeaderLimitCounter++;
                }// end of check new global leader

                //==============================================================
                //[3] Decision Phase of Local Leader and Global Leader
                //==============================================================
                newGlobalLeader = globalLeader.clone();
                //[3.1] Decision Phase of Local Leader
                for (int k = 0; k < g; k++) {
                    // check if LLL reached
                    if (localLeaderLimitCounter[k] > LLL) {
                        localLeaderLimitCounter[k] = 0;//LLLk ← 0

                        //set lower and upper bound
                        int lowerBound = k * groupSize;
                        int upperBound = lowerBound + groupSize - 1;
                        if (k == g - 1) {
                            upperBound = N - 1;
                        }

                        Individual newLocalLeader = localLeader[k].clone();
                        for (int i = lowerBound; i <= upperBound; i++) {
                            double u = operation.randomUniform();//U(0,1)
                            if (u >= pr) {
                                spiderMonkey[i] = new Individual(this.mdmtsp);
                                spiderMonkey[i].generateRandomChromosome();
                                spiderMonkey[i].calculateFitness();
                            } else {
                                Individual SMi = spiderMonkey[i];
                                Individual GL = globalLeader;
                                Individual LLk = localLeader[k];
                                // substract
                                int[][] ss1 = operation.subtraction(SMi, GL);
                                int[][] ss2 = operation.subtraction(SMi, LLk);
                                int[][] ss = operation.merge(ss1, ss2);

                                // Apply SS into SMi to calculate newSM
                                if (swapOperation == SearchOption.PARTIAL) {
                                    spiderMonkey[i] = operation.swapSequenceWithPartialSearch(SMi, ss);
                                } else {
                                    operation.swapSequence(spiderMonkey[i], ss);
                                }

                                // calculate distance & fitness
                                spiderMonkey[i].calculateFitness();
                            }

                            // check local leader
                            if (spiderMonkey[i].getFitness() > newLocalLeader.getFitness()) {
                                newLocalLeader = spiderMonkey[i];
                            }
                        }

                        // check new Local Leader after decision Local Leader Phase
                        if (newLocalLeader.getFitness() > localLeader[k].getFitness()) {
                            localLeader[k] = newLocalLeader.clone();
                            localLeaderLimitCounter[k] = 0;
                        }

                        if (localLeader[k].getFitness() > newGlobalLeader.getFitness()) {
                            newGlobalLeader = localLeader[k];
                        }

                    }// end of if (localLeaderLimitCounter[k] > LLL)
                }// end of Decision Phase of Local Leader

                // check new Global Leader after Decision Phase of Local Leader
                if (newGlobalLeader.getFitness() > globalLeader.getFitness()) {
                    globalLeader = newGlobalLeader.clone();
                    globalLeaderLimitCounter = 0;
                }

                //[3.2] Decision Phase of Global Leader
                if (globalLeaderLimitCounter > GLL) {
                    globalLeaderLimitCounter = 0;
                    if (g < MG) {
                        //Divide the spider monkeys into g + 1 number of groups
                        g++;// g = g + 1

                        groupSize = (int) Math.floor((double) N / (double) g);
                        this.localLeader = new Individual[g];
                        this.localLeaderLimitCounter = new int[g];//LLLc

                        int indexGlobalLeader = -1;
                        double globalFitness = 0;

                        //check new local leader and global leader
                        for (int k = 0; k < g; k++) {
                            //set lower and upper bound
                            int[] bound = getBound(k);
                            if (bound != null) {
                                int lowerBound = bound[0];
                                int upperBound = bound[1];

                                //find new local leader
                                Individual newLocalLeader = spiderMonkey[lowerBound];
                                for (int i = lowerBound + 1; i <= upperBound; i++) {
                                    if (spiderMonkey[i].getFitness() > newLocalLeader.getFitness()) {
                                        newLocalLeader = spiderMonkey[i];
                                    }

                                }
                                this.localLeader[k] = newLocalLeader.clone();

                                //find new global leader
                                if (globalFitness < this.localLeader[k].getFitness()) {
                                    globalFitness = this.localLeader[k].getFitness();
                                    indexGlobalLeader = k;
                                }

                            }// end of if (bound != null)

                        }//end of for (int k = 0; k < g; k++)

                        //update GLOBAL LEADER
                        if (this.localLeader[indexGlobalLeader].getFitness() > this.globalLeader.getFitness()) {
                            this.globalLeader = this.localLeader[indexGlobalLeader].clone();
                        }

                    } else {
                        //Disband all the groups and Form a single group.
                        //System.out.println("DISBAND");
                        g = 1;
                        groupSize = (int) Math.floor((double) N / (double) g);
                        this.localLeader = new Individual[g];
                        this.localLeader[0] = this.globalLeader.clone();
                        this.localLeaderLimitCounter = new int[g];//LLLc
                    }
                }// end of Decision Phase of Global Leader                

                //INCREMENT of t -----------------------------------------------
                t++;//increment t = t+1

                bestSolution = globalLeader.clone();

            }// end of while            
        }
    }

    public Individual mutation(Individual individual) {
        Individual newSMi = individual.clone();
        int routeSize = newSMi.getRoute().length;

        // SLIDE OPERATION -------------------------------------
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
                operation.slideRoute(newSMi, fromIndex, toIndex, slideUnit);
            } else if (slideOperation == SearchOption.PARTIAL) {
                newSMi = operation.slideRouteWithPartialSearch(newSMi, fromIndex, toIndex, slideUnit);
            }
        }// end of SLIDE OPERATION -----------------------------

        // FLIP OPERATION --------------------------------------
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
                operation.flipRoute(newSMi, fromIndex, toIndex);
            } else if (flipOperation == SearchOption.PARTIAL) {
                newSMi = operation.flipRouteWithPartialSearch(newSMi, fromIndex, toIndex);
            }
        }// end of FLIP OPERATION ------------------------------

        // BREAKPOINT OPERATION --------------------------------
        if (breakpointOperation != SearchOption.NONE) {
            // set the result
            if (breakpointOperation == SearchOption.DEFAULT) {
                operation.breakpointMutation(newSMi);
            } else if (breakpointOperation == SearchOption.PARTIAL) {
                newSMi = operation.breakpointMutationWithPartialSearch(newSMi);
            }
        }// end of BREAKPOINT OPERATION ------------------------

        // START DEPOT OPERATION -------------------------------
        if (startDepotOperation != SearchOption.NONE) {
            // set the result
            if (startDepotOperation == SearchOption.DEFAULT) {
                operation.startDepotMutation(newSMi);
            } else if (startDepotOperation == SearchOption.PARTIAL) {
                newSMi = operation.startDepotMutationWithPartialSearch(newSMi);
            }
        }// end of START DEPOT OPERATION -----------------------

        // set new Spider Monkey - i
        //newSMi.calculateFitness();
        return newSMi;
    }

    @Override
    public Individual getBestSolution() {
        return this.bestSolution;
    }

}
