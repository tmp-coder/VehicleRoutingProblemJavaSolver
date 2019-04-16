package zzt.vrp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Solution implements Comparable<Solution>{
    public final Vehicle[] Vehicles;
    public double cost;
    public final Customer Depot;
    private final int capacity;

    private static final int DP_MAX_SIZE = 25;
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
        IntStream.range(0,Vehicles.length)
            .parallel()
            .forEach(
                x->{Vehicles[x] = Vehicles[x].dpConstructNewVechicle();cost+=Vehicles[x].computeCost();}
            );

    }

    public class Vehicle{
        public final List<Customer> routes;
        public int left;
        public Vehicle(){
            routes = new ArrayList<>();
            left = capacity;
        }

        public Vehicle(int cap,List<Customer> routes){
            left = cap;
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
            if(routes.size()>DP_MAX_SIZE)
                return this;
            var dp = new double[1<<routes.size()][routes.size()];
            for(int i=0; i<routes.size() ; ++i)
                dp[1<<i][i] = Customer.dist(routes.get(i),Depot);

            for(int i=2,__ = routes.size(); i< (1 << __) ; ++i){
                var ii = i;
                var idx = IntStream.range(0,__).parallel().filter(x -> (x &(ii>>x))!=0);
                idx.forEach(
                    x->{
                        int S = ii ^(1<<x);
                        double ans = idx.mapToDouble(y -> y==x? Double.MAX_VALUE : dp[S][y]+Customer.dist(routes.get(y),routes.get(x))).min().getAsDouble();
                        dp[ii][x] = Math.min(dp[ii][x],ans);
                    }
                );

            }
            var newRoutes = new ArrayList<Customer>(routes.size());
            var candidateStream = IntStream.range(0,routes.size()).parallel();
            for(int S =  (1 << routes.size()) -1,last; S!=0 ; S^=(1<<last)){
                int tmp = S;
                candidateStream = candidateStream
                    .filter(x -> (tmp&(1<<x))!=0);
                last = candidateStream
                    .reduce(0,(x,acc)-> dp[tmp][acc]+Customer.dist(routes.get(acc),Depot)<dp[tmp][x]+Customer.dist(routes.get(x),Depot)?acc:x);
                newRoutes.add(routes.get(last));
            }
            return new Vehicle(capacity,newRoutes);
        }


    }

}
