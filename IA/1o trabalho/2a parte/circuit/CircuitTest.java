import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import circuit.*;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class CircuitTest extends Application{

	private static String[] arguments;

	private static float pcrossover = GeneticAlgorithm.DEFAULT_PCROSS, pmutate = GeneticAlgorithm.DEFAULT_PMUTATE;
	private static RoverCircuit.CrossoverType cross = RoverCircuit.DEFAULT_CROSS;
	private static RoverCircuit.MutateType mut = RoverCircuit.DEFAULT_MUT;
	private static int eliteSize = Population.ELITE_SIZE, popSize = Population.CAP;
	private static long seed = System.currentTimeMillis();
	private static boolean random = true;
	private static boolean climb = true;
	private static int maxGens = GeneticAlgorithm.MAX_GENS;
	private static long maxTime = GeneticAlgorithm.RUNTIME;
	private static float fitMin = GeneticAlgorithm.MIN_FITNESS;


	public static void main(String[] args) {
		arguments = args;

		switch(arguments.length){
			case 5: cross = crossType(arguments[3]); mut = mutType(arguments[4]);
			case 3: pcrossover = Float.parseFloat(arguments[1]); pmutate = Float.parseFloat(arguments[2]);
			case 1: break;
			default: System.err.println("usage: Main filename [pcrossover pmutate [crossType mutType]]]]"); System.exit(1);
		}

		String arg;
		Scanner in = new Scanner(System.in);
		do{
			System.out.println("Enter your command (help for list of commands):");
			arg = in.nextLine();
			switch(arg.toUpperCase()){
				case "CTYPE":
				case "CROSSTYPE":
				case "CT":
				case "CROSS": System.out.print("Crossover type (OX1, OX2, CX, PMX): "); cross = crossType(in.nextLine()); break;
				case "PCROSS":
				case "PC": System.out.print("Crossover probability (between 0 and 1): "); pcrossover = in.nextFloat(); pcrossover = ((pcrossover >= 0 && pcrossover <= 1.0) ? pcrossover : GeneticAlgorithm.DEFAULT_PCROSS); in.nextLine(); break;
				case "MTYPE":
				case "MUTTYPE":
				case "MT":
				case "MUT": System.out.print("Mutate type (INV, INS, MOV, SW, RAND for a random mutate): "); mut = mutType(in.nextLine()); break;
				case "PMUT":
				case "PM": System.out.print("Mutate probability (between 0 and 1): "); pmutate = in.nextFloat(); pmutate = ((pmutate >= 0 && pmutate <= 1.0) ? pmutate : GeneticAlgorithm.DEFAULT_PMUTATE); in.nextLine(); break;
				case "EL":
				case "ES":
				case "ELITE":
				case "ELITESIZE": System.out.print("Size of elite (0 if no elite is to be used, must be a pair and less than " + popSize + "): ");  eliteSize = in.nextInt(); in.nextLine(); eliteSize = ((eliteSize%2 == 0 && eliteSize < 0 && eliteSize < popSize)? Population.ELITE_SIZE : eliteSize); break;
				case "POP":
				case "PS":
				case "POPSIZE": System.out.print("Size of population: "); popSize = in.nextInt(); in.nextLine(); popSize = (popSize < 0 ? Population.CAP : popSize); break;
				case "SEED": System.out.print("New seed: "); seed = in.nextLong(); in.nextLine(); break;
				case "RANDOM":
				case "SELECT": System.out.print("True if random choice, false if weighted choice: "); random = in.nextBoolean(); in.nextLine(); break;
				case "CLIMB":
				case "HILLCLIMB":
				case "HC": System.out.print("True if hillclimb is to be used, false if otherwise: "); climb = in.nextBoolean(); in.nextLine(); break;
				case "MG":
				case "GENS":
				case "MAXGENS": System.out.print("Max generation number: "); maxGens = in.nextInt(); in.nextLine(); break;
				case "TIME":
				case "MAXTIME": System.out.print("Max time in seconds: "); maxTime = in.nextLong(); in.nextLine(); break;
				case "FM":
				case "FIT":
				case "FITMIN": System.out.print("Minimum fitness: "); fitMin = in.nextFloat(); in.nextLine(); break;
				case "RUN":
				case "EXEC":
				case "EXECUTE":
				case "LAUNCH": System.out.print("Launching!\n"); launch(args); break;
				case "EX": arg = "EXIT";
				case "EXIT": System.out.println("Goodbye!"); break;
				case "HELP": help(); break;
				default: System.out.println("Invalid command!"); help(); break;
			}
			System.out.println();
		} while(!arg.toUpperCase().equals("EXIT"));

	}

	private static void help() {
		System.out.print("Valid commands:\n");
		System.out.print("CTYPE (set type of crossover)\n");
		System.out.print("PCROSS (set probability of crossover)\n");
		System.out.print("MTYPE (set type of mutate)\n");
		System.out.print("PMUT (set probability of mutate)\n");
		System.out.print("ELITE (set size of elite)\n");
		System.out.print("POP (set size of population)\n");
		System.out.print("SEED (set seed of random generators)\n");
		System.out.print("RANDOM (chooses between random or weighted choice during search)\n");
		System.out.print("CLIMB (decide if you should use hillclimb on mutate)\n");
		System.out.print("GENS (set number of generations to run)\n");
		System.out.print("TIME (set max time of execution)\n");
		System.out.print("FIT (set minimum fitness)\n");
		System.out.print("RUN (runs genetic algorithm and then exits)\n");
		System.out.print("EXIT (exits program)\n");
		System.out.print("HELP (prints this help message)\n");
	}

	public void start(Stage stage){

		String file = arguments[0];

		try {
			FileReader fRead = new FileReader(file);
			BufferedReader buf = new BufferedReader(fRead);

			String temp;
			file = "";
			while((temp = buf.readLine()) != null)
				file += temp + "\n";
		} catch(Exception e){
			System.err.println("Error while opening file.");
			return;
		}

		System.out.println("new run: "+cross+" "+mut);
		ObservationData obs = new ObservationData(file);

		Population pop = new Population(popSize, eliteSize, seed);

		for (int i = 0; i < popSize; i++) {
			Individual ind = new RoverCircuit(obs, cross, mut, seed, climb);
			pop.addIndividual(ind);
		}

		GeneticAlgorithm alg = new GeneticAlgorithm(pop, pcrossover, pmutate, seed, random, maxGens, maxTime, fitMin);

		RoverCircuit solution = (RoverCircuit) alg.search();

		try {
			new File("results").mkdir();
			String folderName = arguments[0] + "_" + pcrossover + "_" + pmutate + "_" + cross + "_" + mut + "_" +popSize+ "_" + eliteSize + "_" + seed + "_" + random + "_" + climb;
			new File("results" + File.separator + folderName).mkdir();

			FileWriter fWrite = new FileWriter("results" + File.separator + folderName + File.separator + "results.txt");

			fWrite.write("pCross: "+pcrossover+", pMut: "+pmutate+", Cross Type: "+cross+", Mut Type: "+mut+"\n");
			System.out.println("pCross: "+pcrossover+", pMut: "+pmutate+", Cross Type: "+cross+", Mut Type: "+mut);

			fWrite.write("Seed: "+seed+"\n");
			fWrite.write("Random choice: "+random+"\n");
			fWrite.write("Uses hillclimb: "+climb+"\n");

			fWrite.write("Generations: " + alg.getGens()+"\n");
			fWrite.write("Population size: "+popSize+"\n");
			fWrite.write("Elite size: "+eliteSize+"\n");
			fWrite.write("Total execution time: "+((1.0*alg.getTotalTime())/1E9)+"\n");
			System.out.println("Total execution time: "+((1.0*alg.getTotalTime())/1E9));

			fWrite.write("Best final fit: "+solution.fitness()+"\n");
			System.out.println(solution.fitness());

			fWrite.write("Final best solution: "+Arrays.toString(solution.circuit)+"\n");
			System.out.println(Arrays.toString(solution.circuit));

			List<Individual> best = alg.getBest();
			List<Individual> worst = alg.getWorst();

			fWrite.write("Worst final fit: "+(worst.get(worst.size()-1)).fitness()+"\n");
			fWrite.write("Final worst solution: "+Arrays.toString(((RoverCircuit)worst.get(worst.size()-1)).circuit)+"\n");

			fWrite.close();

			Axis xAxis = new NumberAxis();
			Axis yAxis = new NumberAxis();
			LineChart<Integer, Double> chart = new LineChart<>(xAxis, yAxis);

			xAxis.setLabel("Generation");
			yAxis.setLabel("Fitness");

			chart.setTitle("Improvement of fitness over time");

			XYChart.Series bestSeries = new XYChart.Series();
			bestSeries.setName("Best values");
			int i = 0;
			System.out.println("adding best");
			for(Individual ind : best){
				if(i%GeneticAlgorithm.RUNTIME == 0)
					bestSeries.getData().add(new XYChart.Data<>(i, ind.fitness()));
				i++;
			}

			XYChart.Series worstSeries = new XYChart.Series();
			worstSeries.setName("Worst values");
			i = 0;
			System.out.println("adding worst");
			for(Individual ind : worst){
				if(i%GeneticAlgorithm.RUNTIME == 0)
					worstSeries.getData().add(new XYChart.Data<>(i, ind.fitness()));
				i++;
			}

			System.out.println("putting data in chart");
			chart.setAnimated(false);
			chart.getData().add(bestSeries);
			chart.getData().add(worstSeries);
			chart.setCreateSymbols(false);

			System.out.println("creating scene");
			Scene scene = new Scene(chart, 1920, 1080);
			stage.setScene(scene);
			System.out.println("doing snapshot");
			WritableImage image = chart.snapshot(new SnapshotParameters(), null);

			File fileChart = new File("results" + File.separator + folderName + File.separator + "chart.png");
			System.out.println("writing image");
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", fileChart);
			System.out.println("cycle done, next one");
		} catch(Exception e){
			System.err.println("Error opening file");
		}


		System.exit(0);
	}

	private static RoverCircuit.MutateType mutType(String argument) {
		switch (argument.toUpperCase()){
			case "INV": return RoverCircuit.MutateType.INV;
			case "INS": return RoverCircuit.MutateType.INS;
			case "MOV": return RoverCircuit.MutateType.MOV;
			case "SW": return RoverCircuit.MutateType.SW;
			case "RAND": return RoverCircuit.MutateType.RAND;
			default: return RoverCircuit.DEFAULT_MUT;
		}
	}

	private static RoverCircuit.CrossoverType crossType(String argument) {
		switch (argument.toUpperCase()){
			case "OX1": return RoverCircuit.CrossoverType.OX1;
			case "OX2": return RoverCircuit.CrossoverType.OX2;
			case "PMX": return RoverCircuit.CrossoverType.PMX;
			case "CX": return RoverCircuit.CrossoverType.CX;
			default: return RoverCircuit.DEFAULT_CROSS;
		}
	}

}