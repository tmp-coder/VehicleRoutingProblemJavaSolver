package zzt.vrp;

import java.util.Arrays;

/**
 *  this is a simple greedy solver
 */
public abstract class VRPSolver {
    public final CVRProblem problem;
    public VRPSolver(CVRProblem problem){
        this.problem = problem;
    }

    public Solution solve(){
        Solution ret = new Solution(problem);
        if(!ret.Vehicles[0].tryAppend(problem.customers[1])){
            throw new AssertionError("can't solve");
        }
        int minLeft = ret.Vehicles[0].leftLoad();
        if(minLeft==0)
            minLeft = problem.Capacity;
        for(int i=2; i< problem.customers.length; ++i){
            var now = problem.customers[i];
            int canLeft = 0;
            Solution.Vehicle candidate=null;
            for(var e : ret.Vehicles){
                int left = e.leftLoad() - now.Demand;
                if(left <0)continue;
                if(left == 0){
                    candidate = e;
                    canLeft = Arrays.stream(ret.Vehicles).mapToInt(Solution.Vehicle::leftLoad).min().getAsInt();
                    break;
                }
                // maximal (min left load)
                if((left = Math.min(minLeft,left)) >canLeft){
                    canLeft = left;
                    candidate = e;
                }
            }
            if(candidate == null)throw  new AssertionError("cannot solve");

            minLeft = canLeft;
            candidate.tryAppend(now);
        }
        return ret;
    }
}
