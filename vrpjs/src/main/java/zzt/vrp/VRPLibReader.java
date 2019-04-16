package zzt.vrp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VRPLibReader implements ProblemReader{
    private Map<String,String> headers;

    private Map<Integer,List<Number>> rawCustomers;
    private BufferedReader reader;

    private final Map<String,String> nextSection= Map.ofEntries(
        Map.entry("HEADER","NODE_COORD_SECTION"),
        Map.entry("NODE_COORD_SECTION","DEMAND_SECTION"),
        Map.entry("DEMAND_SECTION","DEPOT_SECTION")
    );
    public VRPLibReader(String filePath) throws IOException {

        reader = new BufferedReader(new FileReader(filePath));
        readHeaders();
        readCoordinate();
        readDemand();
    }
    private List<String> readToNextSection(String nowSection) throws IOException{
        String line;
        var ret = new ArrayList<String>();
        var next = nextSection.get(nowSection.toUpperCase());
        while (!(line = reader.readLine()).equalsIgnoreCase(next)){
            if(line.equalsIgnoreCase(""))
                continue;
            ret.add(line);
        }
        return ret;
    }
    private void readHeaders() throws IOException{

        this.headers = readToNextSection("HEADER").stream()
            .map(x-> x.split(":"))
            .collect(Collectors.toUnmodifiableMap(x->x[0].trim(),x->x[1].trim()));
    }

    private void readCoordinate() throws IOException{
        this.rawCustomers = readToNextSection("NODE_COORD_SECTION").stream()
            .map(x->x.split(" "))
            .collect(Collectors.toMap(x->Integer.parseInt(x[0].trim()),x->List.of(Double.parseDouble(x[1].trim()),Double.parseDouble(x[2].trim()))));
    }

    private void readDemand() throws IOException{
        readToNextSection("DEMAND_SECTION").stream()
            .map(x->x.split(" "))
            .forEach(
                x->{
                    var key = Integer.parseInt(x[0].trim());
                    this.rawCustomers.get(key).add(Integer.parseInt(x[1].trim()));
                }
            );
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

            return Integer.parseInt(matcher.group(1));
        }
        return getCapacity();
    }

    @Override
    public List<Customer> getCustomers() {
        var rawData = this.rawCustomers.values().stream().collect(Collectors.toList());
        rawData.sort(Comparator.comparingInt(x -> x.get(2).intValue()));

        var res =  IntStream.range(0,rawData.size())
            .mapToObj(x-> new Customer(x,rawData.get(x).get(0).doubleValue(),rawData.get(x).get(1).doubleValue(),rawData.get(x).get(2).intValue()))
            .collect(Collectors.toList());

        if (res.get(0).Demand != 0) throw new AssertionError("no depot");
        return res;
    }

}
