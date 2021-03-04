package circuit;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
/**
 * Classe que "implementa" o algoritmo genetico
 */
public class GeneticAlgorithm {

	public static final float DEFAULT_PCROSS = 0.6f;
	public static final float DEFAULT_PMUTATE = 0.001f;

	public static final int RUNTIME = 10;
	public static final int MAX_GENS = Integer.MAX_VALUE;
	public static final float MIN_FITNESS = Float.MIN_VALUE;

	private Population pop;
	private float pcrossover, pmutate;
	private Random rand;
	private long totalTime;
	private boolean random;
	private List<Individual> bestInd, worstInd;
	private int gens;
	private int maxGens;
	private long maxTime;
	private float fitMin;
	private long seed;

	/**
	 * Construtor
	 * @param pop uma populacao
	 */
	public GeneticAlgorithm(Population pop, long seed, boolean random, int maxGens, long maxTime, float fitMin) {
		this.pop = pop;
		this.pcrossover = DEFAULT_PCROSS;
		this.pmutate = DEFAULT_PMUTATE;
		this.rand = new Random(seed);
		this.random = random;
		this.bestInd = new ArrayList<Individual>();
		this.worstInd = new ArrayList<Individual>();
		this.maxGens = maxGens;
		this.maxTime = maxTime;
		this.fitMin = fitMin;
		this.seed = seed;
		this.gens = 0;
	}
	/**
	 * Construtor
	 * @param pop uma populacao
	 * @param pcrossover a probabilidade de crossover
	 * @param pmutate a probabilidade de mutacao
	 */
	public GeneticAlgorithm(Population pop, float pcrossover, float pmutate, long seed, boolean random, int maxGens, long maxTime, float fitMin) {
		this(pop, seed, random, maxGens, maxTime, fitMin);
		this.pcrossover = pcrossover;
		this.pmutate = pmutate;
	}
	
	/**
	 * 	Metodo que pesquisa e devolve o melhor individuo encontrado
	 * @return pop.getBestIndividual(), o melhor individuo
	 */
	public Individual search() {
		totalTime = System.nanoTime();
		do{
			addBest(pop.getBestIndividual());
			addWorst(pop.getWorstIndividual());
			Population newPop = new Population(pop.getPopSize(), pop.getEliteSize(), seed);
			for (int i = 0; i < (pop.getPopSize()-pop.getEliteSize())/2; i++) {

				Individual choice1;
				Individual choice2;
				if(random){
					choice1 = pop.selectRandom();
					choice2 = pop.selectRandom();
				} else {
					choice1 = pop.selectIndividual();
					choice2 = pop.selectIndividual();
				}

				Individual child1, child2;
				if(rand.nextFloat() <= pcrossover){
					Individual[] inds = choice1.crossover(choice2);
					child1 = inds[0];
					child2 = inds[1];
				} else {
					child1 = (Individual) choice1.clone();
					child2 = (Individual) choice2.clone();
				}
				if(rand.nextFloat() <= pmutate)
					child1.mutate();
				if(rand.nextFloat() <= pmutate)
					child2.mutate();
				newPop.addIndividual(child1);
				newPop.addIndividual(child2);
			}

			List<Individual> elite = pop.getElite();
			for(Individual ind : elite)
				newPop.addIndividual(ind);

			pop = newPop;

			incGen();

		} while(!exitTest());

		addBest(pop.getBestIndividual());
		addWorst(pop.getWorstIndividual());
		totalTime = System.nanoTime() - totalTime;
		return pop.getBestIndividual();
	}

	private boolean exitTest(){
		return (System.nanoTime() - totalTime) >= maxTime*(1E9) || gens >= maxGens || pop.getBestIndividual().fitness() <= fitMin;
	}

	private void addBest(Individual ind){
		bestInd.add(ind);
	}

	private void addWorst(Individual ind){
		worstInd.add(ind);
	}

	private void incGen(){
		gens++;
	}

	public int getGens(){
		return gens;
	}

	public List<Individual> getBest(){
		return bestInd;
	}

	public List<Individual> getWorst(){
		return worstInd;
	}

	public long getTotalTime(){
		return totalTime;
	}

}





























