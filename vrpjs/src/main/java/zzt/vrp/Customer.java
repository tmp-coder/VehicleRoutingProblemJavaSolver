package zzt.vrp;

public class Customer {
    public final int Id;
    public final double X,Y;
    public final int Demand;

    private int belongTo;
    public Customer(int id,double x,double y,int demand){
        this.Id = id;
        this.X = x;
        this.Y = y;
        this.Demand = demand;
        this.belongTo = -1;
    }
    public static double dist(final Customer a, final Customer b){
        double dx = a.X - b.X;
        double dy = a.Y - b.Y;
        return Math.sqrt(dx*dx + dy*dy);
    }
    public int getVehicle(){
        return belongTo;
    }

    public boolean isAssigned(){
        return belongTo!=-1;
    }
    public void setVehicle(int x){
        this.belongTo = x;
    }
}