package zzt.vrp;

import java.util.List;

public interface ProblemReader {
    int getCapacity();

    int getNumOfVehicles();

    List<Customer> getCustomers();

    default Customer getDepot(){
        var customers = getCustomers();
        for(var e : customers)
            if(e.Demand==0)
                return e;

        System.err.println("no depot");
        return null;
    }
}
