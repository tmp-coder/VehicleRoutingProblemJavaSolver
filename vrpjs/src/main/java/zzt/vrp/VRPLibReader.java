package zzt.vrp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VRPLibReader implements ProblemReader{
    private Map<String,String> headers;
    private static final String WHITESPACE = "\t|\n| ";
    private Map<Integer,List<Number>> rawCustomers;
    private BufferedReader reader;
    private List<Customer> customers;
    private final Map<String,String> nextSection= Map.ofEntries(
        Map.entry("HEADER","NODE_COORD_SECTION"),
        Map.entry("NODE_COORD_SECTION","DEMAND_SECTION"),
        Map.entry("DEMAND_SECTION","DEPOT_SECTION")
    );
    public VRPLibReader(String filePath) throws IOException {

        reader = new BufferedReader(new FileReader(filePath));
//        readHeaders();
//        readCoordinate();
//        readDemand();
        readHeaders(readToNextSection("HEADER"));
        var coor = readCoordinate(readToNextSection("NODE_COORD_SECTION"));
        var demand = readDemand();
        var depot = readDepot();
        if(depot!=null)
        {
            coor.put(0,List.of(depot.X,depot.Y));
            demand.put(0,0);
        }
        rawCustomers = new HashMap<>();
        coor.forEach(
            (x,y) ->{
                var z = new ArrayList<Number>(y);
                z.add(demand.get(x));
                rawCustomers.put(x,z);
            }
        );
        customers = getAllCustomers();
    }
    private List<String> readToNextSection(String nowSection) throws IOException{
        String line;
        var ret = new ArrayList<String>();
        var next = nextSection.get(nowSection.toUpperCase());
        while (!(line = reader.readLine().trim()).equalsIgnoreCase(next)){
            if(line.equalsIgnoreCase(""))
                continue;
            ret.add(line);
        }
        return ret;
    }

    private Customer readDepot() throws IOException {
        var line =reader.readLine();
        line = line.trim();
        var vals = line.split(WHITESPACE);
        if(vals.length>1){
            double x = Double.parseDouble(vals[0].trim());
            double y = Double.parseDouble(vals[1].trim());
            return new Customer(0,x,y,0);
        }
        return null;
    }
    private void readHeaders(List<String> lines){

        this.headers = lines.stream()
            .map(x-> x.split(":"))
            .collect(Collectors.toUnmodifiableMap(x->x[0].trim(),x->x[1].trim()));
    }

    private Map<Integer, List<Number>>  readCoordinate(List<String> lines) throws IOException{
        return lines.stream()
            .map(x->x.split(WHITESPACE))
            .collect(Collectors.toMap(x->Integer.parseInt(x[0].trim()),x->List.of(Double.parseDouble(x[1].trim()),Double.parseDouble(x[2].trim()))));
    }

    private Map<Integer,Integer> readDemand() throws IOException{
        return readToNextSection("DEMAND_SECTION").stream()
            .map(x->x.split(WHITESPACE))
            .collect(Collectors.toMap(x->Integer.parseInt(x[0].trim()),x->Integer.parseInt(x[1])));
    }

    @Override
    public int getCapacity() {
        return Integer.parseInt(headers.get("CAPACITY"));
    }

    @Override
    public int getNumOfVehicles() {
        String s;
        if((s = this.headers.get("VEHICLES"))!=null)
            return Integer.parseInt(s);
        else if((s = this.headers.get("NAME"))!=null){
            var namePatten = Pattern.compile(".+-k([0-9]+)");
            var matcher = namePatten.matcher(s);
            if(matcher.find())
                return Integer.parseInt(matcher.group(1));
        }
        return getnoCustomers();
    }

    private List<Customer> getAllCustomers() {
        var rawData = this.rawCustomers.values().stream().collect(Collectors.toList());
        rawData.sort(Comparator.comparingInt(x -> x.get(2).intValue()));

        var res =  IntStream.range(0,rawData.size())
            .mapToObj(x-> new Customer(x,rawData.get(x).get(0).doubleValue(),rawData.get(x).get(1).doubleValue(),rawData.get(x).get(2).intValue()))
            .collect(Collectors.toList());

        if (res.get(0).Demand != 0) throw new AssertionError("no depot");
        return res;
    }
    @Override
    public List<Customer> getCustomers(){
        return customers.subList(1,customers.size());
    }

    @Override
    public Customer getDepot() {
        return customers.get(0);
    }

    @Override
    public int getDimension() {
        return Integer.parseInt(headers.get("DIMENSION"));
    }

    @Override
    public int getnoCustomers() {
        return customers.size()-1;
    }
}
