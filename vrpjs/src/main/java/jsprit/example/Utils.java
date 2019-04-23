package jsprit.example;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import zzt.vrp.CVRProblem;
import zzt.vrp.Customer;
import zzt.vrp.Solution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public final class Utils {
    public static void createOutputFile(){
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if (result) System.out.println("./output created");
        }
    }
    public static VehicleRoutingProblem buildProblemFromCVRP(final CVRProblem cvrp){
        VehicleRoutingProblem problem = null;

        /*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
         */
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(WEIGHT_INDEX, cvrp.Capacity);
        VehicleType vehicleType = vehicleTypeBuilder.build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        // add vehicles
        IntStream.range(0,cvrp.NumOfVehicles)
//                .parallel()
            .forEach(i->{
                var vehicle = VehicleImpl.Builder.newInstance(String.valueOf(i))
                    .setStartLocation(Location.newInstance(cvrp.Depot.X,cvrp.Depot.Y))
                    .setType(vehicleType)
                    .build();
                vrpBuilder.addVehicle(vehicle);
            });

        // add service

        Arrays.stream(cvrp.customers)
//                .parallel()
            .forEach(
                x->vrpBuilder.addJob(
                    Service.Builder.newInstance(String.valueOf(x.Id))
                        .addSizeDimension(WEIGHT_INDEX, x.Demand)
                        .setLocation(Location.newInstance(x.X, x.Y))
                        .build()
                )

            );
        problem = vrpBuilder.build();

        return problem;
    }

    public static Solution convert2cvrpSolution(VehicleRoutingProblemSolution solution,CVRProblem problem){
        var routes = solution.getRoutes();
        var solu = new Solution(problem);
//        int id =0;
//        Integer id =0;
        Collection<List<Customer>> rts = new ArrayList<>(routes.size());
        routes.forEach(
            x->{
                var route=  new ArrayList<Customer>();
                x.getActivities().forEach(
                    y->{
                        route.add(problem.customers[y.getIndex()-1]);
                    }
                );
                rts.add(route);
            }
        );
        solu.buildVehicles(rts);
        return solu;
    }
}
