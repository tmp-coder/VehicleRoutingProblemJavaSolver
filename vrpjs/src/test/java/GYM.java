import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class GYM {
    @Test
    public void testPattern(){

        var namePatten = Pattern.compile(".+-k([0-9]+)");
        String s = "A-n50-k19";
        var matcher = namePatten.matcher(s);
        while (matcher.find()){
            for(int i=0 ; i<= matcher.groupCount() ; ++i)
                System.out.println(matcher.group(i));
        }
//        Assertions.assertTrue(Integer.parseInt(matcher.group(1))!=19,matcher.group(1));
    }
}
