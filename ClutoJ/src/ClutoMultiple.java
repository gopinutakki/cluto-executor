import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClutoMultiple {
	public static void main(String args[]) throws IOException {
		ClutoRun cluto = new ClutoRun();
		cluto.clutoExecute();
	}
}

class ClutoRun {
	List<String> datasets = new ArrayList<String>();
	List<String> clusteringMethods = new ArrayList<String>();
	List<String> similarityMeasures = new ArrayList<String>();
	List<String> criterionFunctions = new ArrayList<String>();

	static List<ClutoCmd> clutoResults = new ArrayList<ClutoCmd>();

	public ClutoRun() {
		datasets.add("new3");
		datasets.add("sports");

		clusteringMethods.add("rb");
		clusteringMethods.add("direct");
		clusteringMethods.add("agglo");
		clusteringMethods.add("graph");

		similarityMeasures.add("cos");
		similarityMeasures.add("corr");

		criterionFunctions.add("i1");
		criterionFunctions.add("i2");
		criterionFunctions.add("e1");
		criterionFunctions.add("g1");
		criterionFunctions.add("g1p");
		criterionFunctions.add("h1");
		criterionFunctions.add("h2");
		criterionFunctions.add("slink");
		criterionFunctions.add("wslink");
		criterionFunctions.add("clink");
		criterionFunctions.add("wclink");
		criterionFunctions.add("upgma");
	}

	public void clutoExecute() {
		String cmd = "vcluster sports.mat 10";
		ExecutorService clutoService = Executors.newFixedThreadPool(20);
		int step = 100;

		for (int d = 0; d < datasets.size(); d++) {
			for (int cm = 0; cm < clusteringMethods.size(); cm++) {
				for (int sm = 0; sm < similarityMeasures.size(); sm++) {
					for (int cf = 0; cf < criterionFunctions.size(); cf++) {
						for (int cls = 2; cls < 100; ) {
							cmd = "vcluster -clmethod="
									+ clusteringMethods.get(cm)
									+ " -rclassfile="
									+ datasets.get(d)
									+ ".rclass -clabelfile="
									+ datasets.get(d)
									+ ".mat.clabel -nfeatures=10 -showsummaries=cliques -showfeatures -showtree -labeltree -sim="
									+ similarityMeasures.get(sm) + " "
									+ datasets.get(d) + ".mat "
									+ cls;
							Runnable runCmd = new ClutoConcurrent(cmd);
							clutoService.execute(runCmd);
							//System.out.print(".");
							cls = cls + step;
						}
					}
				}
			}
			System.out.println("\n");
		}

		while (!clutoService.isTerminated()) {

		}
		System.out.println("Done!");
	}

	public static void runClutoCommand(String cmd) throws IOException {
		Process p = Runtime.getRuntime().exec(cmd);
		Scanner sc = new Scanner(p.getInputStream());
		String line = "";
		while (sc.hasNext()) {
			line = sc.nextLine();
			if (line.contains("Entropy: ") && line.contains("Purity: ")) {
				clutoResults.add(new ClutoCmd(cmd));
			}
		}
	}
}

class ClutoCmd {
	String cmd = "";
	double entropy = 0.0;
	double purity = 0.0;

	public ClutoCmd(String command) {
		this.cmd = command;
		String[] line = command.split(" ");
		for (int index = 0; index < line.length; index++) {
			if (line[index].contains("Entropy:"))
				this.entropy = Double.parseDouble(line[index + 1]);
			if (line[index].contains("Purity:"))
				this.purity = Double.parseDouble(line[index + 1]);
		}
		System.out.println(command);
	}
}

class ClutoConcurrent implements Runnable {

	String cmd = "";

	public ClutoConcurrent(String command) {
		this.cmd = command;
	}

	@Override
	public void run() {
		try {
			ClutoRun.runClutoCommand(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
