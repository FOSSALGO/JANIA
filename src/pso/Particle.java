package pso;

import mdmtsp.Individual;

public class Particle {

    private Individual previousBest;//P
    private int[][] velocity;
    private Individual X;// individual (solution)

    public Particle(Individual previousBest, int[][] velocity, Individual solution) {
        this.previousBest = previousBest;
        this.velocity = velocity;
        this.X = solution;
    }
    
    
    public Particle clone() {
        Particle cloneParticle = null;
        if(X!=null&&previousBest!=null){
            cloneParticle = new Particle(previousBest.clone(), velocity.clone(), X.clone());
        }
        return cloneParticle;
    }

    public Individual getPreviousBest() {
        return previousBest.clone();
    }

    public int[][] getVelocity() {
        return velocity;
    }

    public Individual getSolution() {
        return X.clone();
    }    
    
}
