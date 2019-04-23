package zzt.vrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class Solution implements Comparable<Solution>{
    public final Vehicle[] Vehicles;
    public double cost;
    public final Customer Depot;
    private final int capacity;

    private static final int DP_MAX_SIZE = 20;
    public Solution(CVRProblem problem){
        Depot=problem.Depot;
        Vehicles = new Vehicle[problem.NumOfVehicles];
        capacity = problem.Capacity;

    }

    @Override
    public int compareTo(Solution o) {
        return Double.compare(this.cost,o.cost);
    }

    public void dpOptimazer(){
        cost =0;
        IntStream.range(0,Vehicles.length)

//            .parallel()
            .forEach(
                x->{
                    if(Vehicles[x] !=null){
                        Vehicles[x] = Vehicles[x].dpConstructNewVechicle();
                        cost+=Vehicles[x].computeCost();
                    }
                }
            );

    }
    public Vehicle buildVehicle(List<Customer> routes){
        return new Vehicle(routes);
    }
    public void buildVehicles(Collection<List<Customer>> routes){
        int id =0;
        for(var e : routes){
            Vehicles[id] = new Vehicle(e);
            cost+= Vehicles[id].computeCost();
            id++;
        }
    }
    public class Vehicle{
        public final List<Customer> routes;
        public int left;
        public Vehicle(){
            routes = new ArrayList<>();
            left = capacity;
        }

        public Vehicle(List<Customer> routes){
            left = capacity;
            this.routes = routes;
        }
        public boolean tryAppend(Customer customer){
            if(left- customer.Demand>=0){
                left-=customer.Demand;
                var delta = costChangeIfAppend(customer);
                cost+= delta;
                routes.add(customer);
                return true;
            }else return false;
        }

        public boolean canAppend(Customer customer){
            return left - customer.Demand >=0;
        }

        public double costChangeIfAppend(Customer customer){
            if(routes.isEmpty())return 2 * Customer.dist(customer,Depot);
            else {
                var last = routes.get(routes.size()-1);
                return (Customer.dist(last,customer)) - Customer.dist(Depot,last)+Customer.dist(customer,last);
            }
        }
        
        public int leftLoad(){
            return left;
        }

        public double computeCost(){
            double sum =0;
            for(int i=1 ; i< this.routes.size() ; ++i){
                sum += Customer.dist(routes.get(i-1),routes.get(i));
            }
            sum += Customer.dist(routes.get(0),Depot) + Customer.dist(routes.get(routes.size()-1),Depot);
            return sum;
        }
        private Vehicle dpConstructNewVechicle(){
            if(routes==null || routes.size()>DP_MAX_SIZE || routes.size() <=1)
                return this;
            var dp = new double[1<<routes.size()][routes.size()];
            for(int i=0; i<routes.size() ; ++i)
                dp[1<<i][i] = Customer.dist(routes.get(i),Depot);

            for(int i=2,__ = routes.size(); i< (1 << __) ; ++i){
                var ii = i;
                var idx = IntStream.range(0,__).filter(x -> (1 &(ii>>x))!=0).toArray();
                Arrays.stream(idx).forEach(
                    x->{
                        int S = ii ^(1<<x);
                        if(S !=0){
                            dp[ii][x] = Arrays.stream(idx)
                                .filter(y->y!=x)
                                .mapToDouble(y->dp[S][y]+Customer.dist(routes.get(y),routes.get(x)))
                                .min()
                                .getAsDouble();
                        }
                    }
                );
            }
            var newRoutes = new ArrayList<Customer>(routes.size());
            var tailCustomer = Depot;
            for(int S =  (1 << routes.size()) -1,last; S!=0 ; S^=(1<<last)){
                last =0;
                double distance = Double.MAX_VALUE /2;
                for(int i=0 ; i< routes.size() ; ++i){
                    double otherDist;
                    if(((S>>i)&1)!=0&&distance > (otherDist=dp[S][i] + Customer.dist(routes.get(i),tailCustomer))){
                        last = i;
                        distance = otherDist;
                    }
                }
                tailCustomer = routes.get(last);
                newRoutes.add(tailCustomer);
            }
            return new Vehicle(newRoutes);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            for(var e : routes){
                sb.append(String.format("(%.2f,%.2f)",e.X,e.Y));
            }
            sb.append("]\n");
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        return "Solution{" +
            "Vehicles=" + Arrays.toString(Vehicles) +
            ", cost=" + cost +
            ", Depot=" + Depot +
            ", capacity=" + capacity +
            '}';
    }
}
