package zzt.vrp;

import java.util.List;

public interface ProblemReader {
    int getCapacity();

    int getNumOfVehicles();

    List<Customer> getCustomers();

    Customer getDepot();

    int getDimension();

    int getnoCustomers();
}
