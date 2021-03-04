package circuit;

import java.util.*;

/**
 * 	Classe usada para a representacao de uma populacao.
 */
public class Population {
	
	public final static int CAP = 100;
	public final static int ELITE_SIZE = 10;
	
	private static Random gen;
	private double sumOfFitness;
	private int size;
	private boolean currupt;
	private Individual bestInd;
	private Individual worstInd;
	private double bestFit;
	private double worstFit;
	private int eliteSize;
	private int popSize;

	private ArrayList<Individual> pop;

	private ArrayList<Double> acum;

	/**
	 * 	Construtor relativo a classe Population
	 */
	public Population(long seed){
		this.size = 0;
		this.pop = new ArrayList<>(CAP);
		this.acum = new ArrayList<>(CAP);
		this.sumOfFitness = 0.0;
		this.currupt = true;
		this.bestInd = null;
		this.worstInd = null;
		this.bestFit = Double.POSITIVE_INFINITY;
		this.worstFit = Double.NEGATIVE_INFINITY;
		this.eliteSize = ELITE_SIZE;
		this.popSize = CAP;
		this.gen = new Random(seed);
	}

	public Population(int popSize, long seed){
		this(seed);
		this.popSize = popSize;
	}

	public Population(int popSize, int eliteSize, long seed){
		this(popSize, seed);
		this.eliteSize = eliteSize;
	}

	public Individual getBestIndividual(){
		return bestInd;
	}

	public Individual getWorstIndividual(){
		return worstInd;
	}

	public int getSize(){
		return size;
	}
	
	/**
	 * 	Construtor onde se especifica a populacao
	 * @param p um array de individuos
	 */
	public Population(Individual[] p, long seed){
		this(seed);
		for(Individual i : p)
			addIndividual(i);

	}

	public Individual selectRandom(){
		return pop.get(gen.nextInt(size));
	}
	
	/**
	 * Selecciona e devolve um individuo da populacao, tendo em conta a sua fitness
	 * @return um array de individuos
	 */
	public Individual selectIndividual() {
		recalculateSum();

		double total=0.0;
		if( currupt ) {

			for(int i=0; i < pop.size(); i++) {
				total += 1/pop.get(i).fitness();
				acum.add(total/sumOfFitness);
			}
			currupt = false;
		}

		double r = gen.nextDouble();

		int pos = Collections.binarySearch(acum, r);

		if( pos >= 0)
			return pop.get(pos);
		else
			return pop.get(-(pos+1));
		
	}

	private void recalculateSum() {
		sumOfFitness = 0.0;
		for (Individual ind: pop) {
			double f = ind.fitness();
			sumOfFitness += 1/f;
		}
	}

	/**
	 * Adiciona um individuo a populacao
	 * @param ind, um individuo
	 */
	public void addIndividual(Individual ind) {
		size++;
		pop.add(ind);
		double f = ind.fitness();
		sumOfFitness += 1/f; 
		if( f > worstFit ) {
			worstFit = f;
			worstInd = ind;
		}
		if( f < bestFit ) {
			bestFit = f;
			bestInd = ind;
		}
	}

	public List<Individual> getElite(){
		List<Individual> list = (ArrayList<Individual>)pop.clone();
		Collections.sort(list);
		Collections.reverse(list);
		list = list.subList(0, ELITE_SIZE);
		return list;
	}

	public int getEliteSize() {
		return eliteSize;
	}

	public int getPopSize() {
		return popSize;
	}
}
