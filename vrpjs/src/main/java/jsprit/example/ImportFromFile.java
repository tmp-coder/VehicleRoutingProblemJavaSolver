package jsprit.example;

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer.Label;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import config.Config;
import zzt.vrp.CVRProblem;
import zzt.vrp.VRPLibReader;

import java.io.IOException;
import java.util.Collection;
public class ImportFromFile {
    public static void main(String[] args) throws IOException {
        /*
         * some preparation - create output folder
         */
        /*
         * some preparation - create output folder
         */
        Utils.createOutputFile();
        /*
         * Build the problem.
         *
         * But define a problem-builder first.
         */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        /*
         * A solomonReader reads solomon-instance files, and stores the required information in the builder.
         */
//        new SolomonReader(vrpBuilder).read(Config.inputDir+"A-n32-k5.vrp");
//        new TSPLIB95Reader(vrpBuilder).read(Config.inputDir+"A-n32-k5.vrp");

        /*
         * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
         */
        VehicleRoutingProblem vrp = Utils.buildProblemFromCVRP(new CVRProblem(new VRPLibReader(Config.SMALL_CUSTOMERS+"A-n32-k5.vrp")));

        new Plotter(vrp).plot("output/solomon_C101.png", "C101");

        /*
         * Define the required vehicle-routing algorithms to solve the above problem.
         *
         * The algorithm can be defined and configured in an xml-file.
         */
        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);

        /*
         * Solve the problem.
         *
         *
         */
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        /*
         * Retrieve best solution.
         */
        VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);


        /*
         * print solution
         */
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        /*
         * Plot solution.
         */
        Plotter plotter = new Plotter(vrp, solution);
//		plotter.setBoundingBox(30, 0, 50, 20);
        plotter.plot("output/solomon_C101_solution.png", "C101");

        new GraphStreamViewer(vrp, solution).setCameraView(30, 30, 0.25).labelWith(Label.ID).setRenderDelay(100).display();

    }
}
