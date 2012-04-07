import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClutoMultiple {
	public static void main(String args[]) throws IOException {
		ClutoRun cluto = new ClutoRun();
		cluto.clutoExecute();
		cluto.sortEntropyPurity();
		cluto.printTop();
		System.out.println("DONE!");
	}
}

class ClutoRun {
	List<String> datasets = new ArrayList<String>();
	List<String> clusteringMethods = new ArrayList<String>();
	List<String> similarityMeasures = new ArrayList<String>();
	List<String> criterionFunctions = new ArrayList<String>();

	static List<ClutoCmd> clutoResults = new ArrayList<ClutoCmd>();

	public ClutoRun() {

		datasets.add("sports");
		// datasets.add("new3");

		clusteringMethods.add("rb");
		clusteringMethods.add("direct");
		clusteringMethods.add("agglo");
		clusteringMethods.add("graph");

		similarityMeasures.add("cos");
		// similarityMeasures.add("corr");

		// criterionFunctions.add("i1");
		criterionFunctions.add("i2");
		// criterionFunctions.add("e1");
		// criterionFunctions.add("g1");
		// criterionFunctions.add("g1p");
		// criterionFunctions.add("h1");
		// criterionFunctions.add("h2");
		// criterionFunctions.add("slink");
		// criterionFunctions.add("wslink");
		// criterionFunctions.add("clink");
		// criterionFunctions.add("wclink");
		// criterionFunctions.add("upgma");
	}

	public void clutoExecute() {
		String cmd = "";
		ExecutorService clutoService = Executors.newFixedThreadPool(200);
		int step = 10;
		for (String d : datasets) {
			for (String cm : clusteringMethods) {
				for (String sm : similarityMeasures) {
					for (String cf : criterionFunctions) {
						for (int cls = 2; cls < 100;) {
							cmd = "./vcluster -crfun="
									+ cf
									+ " -clmethod="
									+ cm
									+ " -rclassfile="
									+ d
									+ ".mat.rclass -clabelfile="
									+ d
									+ ".mat.clabel -nfeatures=10 -showsummaries=cliques -showfeatures -showtree -labeltree -sim="
									+ sm + " " + d + ".mat " + cls;
							String ss = d + " " + cm + " " + sm + " " + cf
									+ " " + cls;
							Runnable runCmd = new ClutoConcurrent(cmd, ss);
							clutoService.execute(runCmd);
							cls = cls + step;
						}
					}
				}
			}
		}

		clutoService.shutdown();
		while (!clutoService.isTerminated()) {
		}
	}

	public static void runClutoCommand(String cmd, String ss)
			throws IOException {
		Process p = Runtime.getRuntime().exec(cmd);
		Scanner sc = new Scanner(p.getInputStream());
		String line = "";
		while (sc.hasNext()) {
			line = sc.nextLine();
			if (line.contains("Entropy: ") && line.contains("Purity: ")) {
				clutoResults.add(new ClutoCmd(ss, line.replaceAll(",", " ")));
			}
		}
	}

	void sortEntropyPurity() {
		Collections.sort(clutoResults, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				ClutoCmd c1 = (ClutoCmd) o1;
				ClutoCmd c2 = (ClutoCmd) o2;

				if (c1.entropy < c2.entropy)
					return 1;
				else if (c1.entropy > c2.entropy)
					return -1;
				return 0;
			}
		});

		Collections.sort(clutoResults, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				ClutoCmd c1 = (ClutoCmd) o1;
				ClutoCmd c2 = (ClutoCmd) o2;

				if (c1.purity > c2.purity)
					return 1;
				else if (c1.purity < c2.purity)
					return -1;
				return 0;
			}
		});
	}

	void printTop() {
		for (ClutoCmd ccmd : clutoResults) {
			System.out
					.println(ccmd.ss + " " + ccmd.entropy + " " + ccmd.purity);
		}
	}
}

class ClutoCmd {
	String ss = "";
	double entropy = -1.0;
	double purity = -1.0;

	public ClutoCmd(String command, String resLine) {
		this.ss = command;
		String[] line = resLine.split(" ");
		for (int index = 0; index < line.length; index++) {
			if (line[index].contains("Entropy:"))
				this.entropy = Double.parseDouble(line[index + 1]);
			if (line[index].contains("Purity:"))
				this.purity = Double.parseDouble(line[index + 1]);
		}
	}
}

class ClutoConcurrent implements Runnable {

	String cmd = "";
	String setupString = "";

	public ClutoConcurrent(String command, String ss) {
		this.cmd = command;
		this.setupString = ss;
	}

	@Override
	public void run() {
		try {
			ClutoRun.runClutoCommand(cmd, setupString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
