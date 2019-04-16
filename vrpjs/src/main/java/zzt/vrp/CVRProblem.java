package zzt.vrp;

public class CVRProblem {
    public final int Capacity;
    public final int NumOfVehicles;
    public final Customer Depot;
    public final Customer[] customers;

    public CVRProblem(VRPLibReader reader){
        Capacity = reader.getCapacity();
        Depot = reader.getDepot();
        customers = reader.getCustomers().toArray(new Customer[0]);
        NumOfVehicles = reader.getNumOfVehicles();
    }
}
