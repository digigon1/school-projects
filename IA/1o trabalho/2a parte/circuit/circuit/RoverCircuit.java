package circuit;

import java.util.*;
import java.lang.Math;

/**
 * 	Classe que instancia a classe abstracta Individual
 */
public class RoverCircuit extends Individual{
	private static Random gen;
	private final boolean climb;
	private int size;

	public static final MutateType DEFAULT_MUT = MutateType.RAND;
	public static final CrossoverType DEFAULT_CROSS = CrossoverType.OX1;

	public int[] circuit;

	protected ObservationData data;
	private double fit;
	private MutateType mut;
	private CrossoverType cross;

	private long seed;

	@Override
	public int compareTo(Object o) {
		if(this.fit > ((RoverCircuit)o).fit){
			return -1;
		} else if(this.fit < ((RoverCircuit)o).fit){
			return 1;
		} else {
			return 0;
		}
	}

	public enum MutateType {
		MOV, SW, INV, INS, RAND
	}

	public enum CrossoverType {
		OX1, OX2, CX, PMX
	}

	public RoverCircuit(ObservationData data, long seed, boolean climb){
		this.size = data.getSize();
		this.data = data;
		
		List<Integer> c = new ArrayList<Integer>(size);
		int i;
		for(i = 0; i < size; i++){
			c.add(i);
		}
		Collections.shuffle(c);

		this.circuit = new int[size];
		for(i = 0; i < size; i++)
			this.circuit[i] = c.get(i);

		this.mut = DEFAULT_MUT;
		this.cross = DEFAULT_CROSS;
		this.fit = this.fitness();
		this.seed = seed;
		this.gen = new Random(seed);
		this.climb = climb;
	}

	public RoverCircuit(ObservationData data, int[] circuit, long seed, boolean climb){
		this.size = data.getSize();
		this.data = data;
		this.circuit = circuit;
		this.mut = DEFAULT_MUT;
		this.cross = DEFAULT_CROSS;
		this.fit = this.fitness();
		this.seed = seed;
		this.gen = new Random(seed);
		this.climb = climb;
	}

	public RoverCircuit(ObservationData data, CrossoverType cross, MutateType mut, long seed, boolean climb){
		this(data, seed, climb);
		this.cross = cross;
		this.mut = mut;
	}

	public RoverCircuit(ObservationData data, int[] circuit, CrossoverType cross, MutateType mut, long seed, boolean climb){
		this(data, circuit, seed, climb);
		this.cross = cross;
		this.mut = mut;
	}

	public double fitness(){
		return fitness(this.circuit);
	}

	private double fitness(int[] circuit){
		fit = data.getSpot(circuit[0]).firstTime();
		for(int i = 0; i < size; i++){
			fit += data.getSpot(i).durationObservation((int) fit);
			fit += data.getCost(circuit[i], circuit[(i+1)%size]);
		}
		return fit;
	}

	public Individual[] crossover(Individual other){
		Individual[] result = new Individual[2];

		switch(this.cross){
			case OX1: result = cross_ox1(other); break;
			case OX2: result = cross_ox2(other); break;
			case CX: result = cross_cx(other); break;
			case PMX: result = cross_pmx(other); break;
			default: System.err.println("Crossover Type doesnt exist"); break;
		}

		return result;
	}

	private Individual[] cross_pmx(Individual other){
		Individual[] result = new Individual[2];

		result[0] = pmx(this, other);
		result[1] = pmx(other, this);

		return result;
	}

	private Individual pmx(Individual p1, Individual p2){
		int[] circ = ((RoverCircuit) p1).circuit.clone();

		int cut1 = gen.nextInt(size);
		int cut2;
		do{
			cut2 = gen.nextInt(size);
		} while(cut1==cut2);

		int i;
		for(i = 0; i < size; i++){
			if(i >= Math.min(cut1, cut2) && i <= Math.max(cut1, cut2))
				continue;

			while(repeat(circ[i], circ)){
				for(int j = 0; j < circ.length; i++)
					if(circ[i] == circ[j] && j >= Math.min(cut1, cut2) && j <= Math.max(cut1, cut2))
						circ[i] = ((RoverCircuit) p2).circuit[j];
			}
		}

		return new RoverCircuit(((RoverCircuit) p1).data, circ, cross, mut, seed, climb);
	}

	private boolean repeat(int v, int[] circ){
		int found = 0;
		for(int i = 0; i < circ.length; i++)
			if(v == circ[i])
				found++;

		return found > 1;
	}

	private Individual[] cross_cx(Individual other){
		Individual[] result = new Individual[2];

		int[] child1 = new int[size];
		int[] child2 = new int[size];

		Map<Integer, Integer> seen = new HashMap<>();

		int i;
		int cycles = 0;
		for(i = 0; i < size; i++){
			int pos = i;
			while(!seen.containsKey(this.circuit[pos])) {
				seen.put(this.circuit[pos], cycles);
				pos = indexOf(this.circuit, ((RoverCircuit)other).circuit[pos]);
				if(seen.containsKey(this.circuit[pos]))
					cycles++;
			}
		}

		for(i = 0; i < size; i++) {
			if(seen.get(this.circuit[i])%2 == 0){
				child1[i] = this.circuit[i];
				child2[i] = ((RoverCircuit)other).circuit[i];
			} else {
				child2[i] = this.circuit[i];
				child1[i] = ((RoverCircuit)other).circuit[i];
			}
		}

		result[0] = new RoverCircuit(data, child1, cross, mut, seed, climb);
		result[1] = new RoverCircuit(data, child2, cross, mut, seed, climb);

		return result;
	}

	private int indexOf(int[] array, int value){
		for(int i=0; i<array.length; i++)
			if(array[i] == value)
				return i;
		return -1;
	}

	private Individual[] cross_ox1(Individual other){
		Individual[] result = new Individual[2];

		int[] child1;
		int[] child2;

		List<Integer> circuit1_list = new ArrayList<>(size);
		List<Integer> circuit2_list = new ArrayList<>(size);

		int i;
		for (i=0; i<size;i++)
			circuit1_list.add(this.circuit[i]);

		for (i=0; i<size;i++)
			circuit2_list.add(((RoverCircuit)other).circuit[i]);

		int cut1 = gen.nextInt(size);
		int cut2;
		do{
			cut2 = gen.nextInt(size);
		} while(cut1==cut2);

		List<Integer> c1_first_cut  = circuit1_list.subList(0, Math.min(cut1,cut2));
		List<Integer> sublist1 = circuit1_list.subList(Math.min(cut1,cut2), Math.max(cut1,cut2)+1);
		List<Integer> c1_second_cut = circuit1_list.subList(Math.max(cut1,cut2)+1, size);

		List<Integer> to_fill_c2 = new ArrayList<>();
		to_fill_c2.addAll(c1_second_cut);
		to_fill_c2.addAll(c1_first_cut);
		to_fill_c2.addAll(sublist1);

		List<Integer> c2_first_cut  = circuit2_list.subList(0, Math.min(cut1,cut2));
		List<Integer> sublist2 = circuit2_list.subList(Math.min(cut1,cut2), Math.max(cut1,cut2)+1);
		List<Integer> c2_second_cut = circuit2_list.subList(Math.max(cut1,cut2)+1, size);

		List<Integer> to_fill_c1 = new ArrayList<Integer>();
		to_fill_c1.addAll(c2_second_cut);
		to_fill_c1.addAll(c2_first_cut);
		to_fill_c1.addAll(sublist2);

		child1 = fill_ox1(sublist1,to_fill_c1,cut1,cut2);
		child2 = fill_ox1(sublist2,to_fill_c2,cut1,cut2);

		result[0] = new RoverCircuit(data, child1, cross, mut, seed, climb);
		result[1] = new RoverCircuit(data, child2, cross, mut, seed, climb);

		return result;
	}

	private int[] fill_ox1(List<Integer> sublist, List<Integer> to_fill_arr, int cut1, int cut2){
		int[] final_c = new int[size];
		int index = 0, i=0;
		for(i=0;i<size;i++){
			if(i>=Math.min(cut1,cut2) && i<=Math.max(cut1,cut2)){
				final_c[i]=sublist.get(index++);
			}
			else
				final_c[i]=-1;
		}

		i=(Math.max(cut1,cut2)+1)%size;
		boolean hasValue;
		for(Integer value : to_fill_arr){
			if(i==Math.min(cut1,cut2))
				break;
			hasValue=false;
			for(int j=0; j<size;j++){
				if(final_c[j]==value){
					hasValue=true;
					break;
				}
			}
			if(!hasValue){
				final_c[i]=value;
				i=(i+1)%size;
			}
		}
		return final_c;
	}

	private Individual[] cross_ox2(Individual other){
		Individual[] result = new Individual[2];

		int[] child1;
		int[] child2;

		List<Integer> circuit1_list = new ArrayList<>(size);
		List<Integer> circuit2_list = new ArrayList<>(size);

		int i;
		for (i=0; i<size;i++)
			circuit1_list.add(this.circuit[i]);

		for (i=0; i<size;i++)
			circuit2_list.add(((RoverCircuit)other).circuit[i]);

		int cut1 = gen.nextInt(size);
		int cut2;
		do{
			cut2 = gen.nextInt(size);
		} while(cut1==cut2);

		List<Integer> sublist1 = circuit1_list.subList(Math.min(cut1,cut2), Math.max(cut1,cut2)+1);
		List<Integer> sublist2 = circuit2_list.subList(Math.min(cut1,cut2), Math.max(cut1,cut2)+1);

		child1 = fill_ox2(sublist1,((RoverCircuit)other).circuit,cut1,cut2);
		child2 = fill_ox2(sublist2,this.circuit,cut1,cut2);

		result[0] = new RoverCircuit(data, child1, cross, mut, seed, climb);
		result[1] = new RoverCircuit(data, child2, cross, mut, seed, climb);

		return result;
	}

	private int[] fill_ox2(List<Integer> sublist, int[] to_fill_arr, int cut1, int cut2){
		int[] final_c = new int[size];
		int index = 0, i=0;
		for(i=0;i<size;i++){
			if(i>=Math.min(cut1,cut2) && i<=Math.max(cut1,cut2)){
				final_c[i]=sublist.get(index++);
			}
			else
				final_c[i]=-1;
		}

		i=0;
		boolean hasValue;
		for(Integer value : to_fill_arr){
			if(i==size)
				break;
			else if(i==Math.min(cut1,cut2))
				i=(i+sublist.size())%size;
			hasValue=false;
			for(int j=0; j<size;j++){
				if(final_c[j]==value){
					hasValue=true;
					break;
				}
			}
			if(!hasValue){
				final_c[i]=value;
				i=(i+1)%size;
			}
		}
		return final_c;
	}

	public void mutate(){
		switch(this.mut){
			case SW: mutate_troca(); break;
			case INV: mutate_inversao(); break;
			case INS: mutate_insercao(); break;
			case MOV: mutate_move(); break;
			case RAND: mutate_rand(); break;
			default: System.err.println("Mutate Type doesnt exist");
		}

		if(climb){
			int[] best = this.circuit;
			for (int i = 0; i < size - 1; i++) {
				for (int j = 0; j < size; j++) {
					int[] temp = swap(this.circuit, i, j);
					if (fitness(best) > fitness(temp)) {
						best = temp;
					}

				}
			}
			this.circuit = best;
		}

		this.fitness();
	}

	private void mutate_rand() {
		int r = gen.nextInt(MutateType.values().length - 1);
		switch(r){
			case 0: mutate_troca(); break;
			case 1: mutate_inversao(); break;
			case 2: mutate_insercao(); break;
			case 3: mutate_move(); break;
			default: mutate_troca(); break;
		}
	}

	private int[] swap(int[] circuit, int i, int j) {
		int[] result = circuit.clone();

		result[i] = circuit[j];
		result[j] = circuit[i];

		return result;
	}

	private void mutate_move(){
		List<Integer> circuit_list = new ArrayList<Integer>(circuit.length);
		int i;
		for (i=0; i<circuit.length;i++)
		    circuit_list.add(circuit[i]);
		
		List<Integer> circuit_temp = new ArrayList<Integer>(circuit_list);
			
		int cut1 = gen.nextInt(size);
		int cut2;
		do{
			cut2 = gen.nextInt(size);
		} while(cut1==cut2);
		
		List<Integer> sublist = circuit_list.subList(Math.min(cut1, cut2), Math.max(cut1, cut2)+1);
		circuit_temp.removeAll(sublist);

		if(circuit_temp.size() == 0)
			return;

		int insert_pos;
		do{
			insert_pos = gen.nextInt(circuit_temp.size() + 1);
		} while(insert_pos == Math.min(cut1, cut2));
		
		List<Integer> final_circuit = circuit_temp.subList(0,insert_pos); 
		final_circuit.addAll(sublist);
		final_circuit.addAll(circuit_temp.subList(insert_pos+sublist.size(),circuit_list.size()));
		
		for(i = 0; i < circuit.length; i++)
			circuit[i] = final_circuit.get(i);
	}

	private void mutate_insercao(){
		int[] circuit_list = circuit.clone();
		
		int swap1 = gen.nextInt(size);
		int swap2;
		do{
			swap2 = gen.nextInt(size);
		} while(swap1==swap2);

		circuit_list[swap2] = circuit[swap1];

		int i;
		if(swap1 < swap2){
			for(i = swap1; i < swap2; i++)
				circuit_list[i] = circuit[i+1];
		} else {
			for(i = swap2 + 1; i < swap1 + 1; i++)
				circuit_list[i] = circuit[i - 1];
		}

		circuit = circuit_list;

	}

	private void mutate_inversao(){
		List<Integer> circuit_list = new ArrayList<Integer>(size);
		int i;
		for (i=0; i<size;i++)
		    circuit_list.add(circuit[i]);
			
		int swap1 = gen.nextInt(size);
		int swap2;
		do{
			swap2 = gen.nextInt(size);
		} while(swap1==swap2);
		
		int min,max;
		if(swap1<swap2){
			min=swap1;
			max=swap2;
		} else {
			min=swap2;
			max=swap1;
		}
		
		List<Integer> sublist = circuit_list.subList(min, max+1);
		Collections.reverse(sublist);
		
		for(i = min; i <= max; i++)
			circuit[i] = sublist.get(i-min);
	}
	
	private void mutate_troca(){
		int swap1 = gen.nextInt(size);
		int swap2;
		do{
			swap2 = gen.nextInt(size);
		} while(swap1==swap2);
		
		int temp = circuit[swap1];
		circuit[swap1]=circuit[swap2];
		circuit[swap2]=temp;
	}

	public Object clone(){
		return new RoverCircuit(data, circuit, cross, mut, seed, climb);
	}
}
