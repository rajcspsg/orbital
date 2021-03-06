import orbital.algorithm.evolutionary.*;
import orbital.algorithm.evolutionary.GeneticAlgorithm.Configuration;
import orbital.logic.functor.Function;
import java.io.*;

/**
 * Knapsack problem solved with a genetic algorithm.
 * Knapsack is NP-complete.
 * 
 * @author Andr&eacute; Platzer
 * @version $Id$
 */
public class Knapsack implements Runnable, GeneticAlgorithmProblem {
    static final int MAXWEIGHT = 17;
    static final int ItemDesc[][] = {   // Array of {Weight, Value}
        {3,4}, {3,4}, {3,4}, {3,4}, {3,4},
        {4,5}, {4,5}, {4,5},
        {7,10},{7,10},{8,11},{8,11},{9,13}};

    public static void main(String arg[]) {
        new Knapsack().run();
    } 


    protected GeneticAlgorithm ga;
    private final PrintStream out = System.out;

    public Knapsack() {}

    public Function /* <Object, Number> */ getEvaluation() {
        return new KnapsackEvaluation(this);
    } 

    public Population getPopulation() {
        out.println(" creating population");
        int        populationSize = 6;
        Genome g = new Genome();
        g.add(new Gene.BitSet(ItemDesc.length));
        return Population.create(g, populationSize);
    } 

    public void init() {
        out.println("init() initializing");
    } 
    public void start() {
        out.println("start() start thread");
        new Thread(this).start();
    } 

    public void run() {
        double maxRecombination = 0.1;
        double maxMutation = 0.2;
        Configuration config =
            new GeneticAlgorithm.Configuration(new Knapsack(),
                                               Selectors.likelyBetter(),
                                               maxRecombination,
                                               maxMutation,
                                               IncrementalGeneticAlgorithm.class);
        System.out.println("breeding population");
        Object solution = config.solve();
        System.out.println("found solution");
        System.out.println(solution);
    } 

    int weight;
    int value;
    void calcVW(Genome g) {
        Gene.BitSet c = (Gene.BitSet) g.get(0);
        weight = 0;
        value = 0;
        for (int iBit = 0; iBit < c.length(); iBit++)
            if (c.get(iBit)) {
                weight += ItemDesc[iBit][0];
                value += ItemDesc[iBit][1];
            } 
    } 

    public boolean isSolution(Population pop) {
        final int ANSWER = 24;
        out.println("isSolution?\n" + pop);
        for (int iChrom = 0; iChrom < pop.size(); iChrom++) {
            calcVW(pop.get(iChrom));
            if (weight <= MAXWEIGHT && value == ANSWER)
                return true;
        } 
        return false;
    } 
}

class KnapsackEvaluation implements Function /* <Object, Number> */ {
    public KnapsackEvaluation(Knapsack t) {
        ks = t;
    }
    private Knapsack ks;

    final double         PENALTY = 3.0;
    public Object apply(Object genome) {
        try {
            Genome g = (Genome) genome;

            ks.calcVW(g);
            if (ks.weight > ks.MAXWEIGHT)
                return new Double(ks.value - PENALTY * (ks.weight - ks.MAXWEIGHT));
            else
                return new Double(ks.value);
        } catch (ClassCastException err) {
            throw new Error("panic");
        } 
    } 
}
