import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;
import config.Config;
import jsprit.example.Utils;
import zzt.vrp.CVRProblem;
import zzt.vrp.VRPLibReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Main {
    private static final String benchmarkFileName = "Li_21";
    private static final String benchmarkSuffix=".vrp";
    private static final String testFilePath = Config.BENCHMARK+Config.Li+benchmarkFileName+benchmarkSuffix;
    private static final String outputDir = "output/";

    public static void main(String[] args) throws IOException {
//        var ans = dpOptimizedFinalAns(Config.Golden);
//        dpOptimizedFinalAns(Config.Li);
//        dpOptimizedFinalAns(Config.X);

        List.of(Config.Li,Config.Golden,Config.X)
            .parallelStream()
            .forEach(
                x->{
                    try {
                        dpOptimizedFinalAns(x)
                            .forEach(System.out::println);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            );
//        System.out.println(ans);
//        singleFileTest();
    }
    public static void singleFileTest() throws IOException {
        Utils.createOutputFile();
        var cvrp = new CVRProblem(new VRPLibReader(testFilePath));
        VehicleRoutingProblem vrp = Utils.buildProblemFromCVRP(cvrp);

        /*
         * get the algorithm out-of-the-box.
         */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
//        algorithm.setMaxIterations(Math.max(algorithm.getMaxIterations(),cvrp.customers.length*10));
//        algorithm.setMaxIterations(10000);
        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
//        algorithm.setMaxIterations(4000);
        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        var solu = Utils.convert2cvrpSolution(bestSolution,cvrp);
        System.out.println("mycost : "+solu.cost);
        solu.dpOptimazer();
        System.out.println(solu);
        System.out.println("after dp optimize:"+solu.cost);
        new VrpXMLWriter(vrp, solutions).write(outputDir+benchmarkFileName+".xml");

        SolutionPrinter.print(vrp, bestSolution, SolutionPrinter.Print.VERBOSE);

        /*
         * plot
         */
        new Plotter(vrp,bestSolution).plot(outputDir+benchmarkFileName+".png",benchmarkFileName);

        /*
        render problem and solution with GraphStream
         */
        new GraphStreamViewer(vrp, bestSolution).labelWith(GraphStreamViewer.Label.ID).setRenderDelay(200).display();

    }

    public static List<String> dpOptimizedFinalAns(String benchmarkDir) throws IOException {
        List<String> ret = new LinkedList<>();
        var dir = new File(Config.BENCHMARK+benchmarkDir);

        var writer = new BufferedWriter(new FileWriter(outputDir+benchmarkDir.substring(0,benchmarkDir.length()-1)+".csv"));
        writer.write("name,noCustomers,meta-heuristic,meta-heuristic+dp\n");

        for(var e : dir.listFiles()){
            if(e.getName().endsWith(".sol"))
                continue;
            var cvrp = new CVRProblem(new VRPLibReader(e.getPath()));
            var algo = Jsprit.createAlgorithm(Utils.buildProblemFromCVRP(cvrp));
//            algo.setMaxIterations();
            var vrpSolus = algo
                .searchSolutions();
            var solu = Solutions.bestOf(vrpSolus);
            var mySolu = Utils.convert2cvrpSolution(solu,cvrp);

            mySolu.dpOptimazer();
            if(solu.getCost() - mySolu.cost > 1e-3)
            {
                System.out.println("find: "+e.getName());
                ret.add(e.getName());
            }
            System.out.println(String.format(e.getName()+"   : meta.cots: %.2f dpCost : %.2f",solu.getCost(),mySolu.cost));
            writer.write(e.getName()+","+String.format("%.4f,%.4f\n",solu.getCost(),mySolu.cost));
        }
        writer.close();
        return ret;
    }
}
