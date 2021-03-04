package circuit;

/**
 * 	Classe abstracta para representar um individuo da populacao.
 */
public abstract class Individual implements Comparable{
	/**
	 * 	fitness: representa a "habilidade"/adequabilidade do individuo para "resolver" o problema,
	 * isto e, o custo que se pretende o menor possivel
	 * @return fitness
	 */
	public abstract double fitness();
	/**
	 * metodo abstracto que cruza dois individuos e gera um array de individuos.
	 * @return Individual[], array de individuos
	 */
	public abstract Individual[] crossover(Individual other);
	/**
	 * 	Metodo abstracto que opera uma mutacao num individuo.
	 */
	public abstract void mutate();
	
	public abstract Object clone();
	
}
