import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class VulnParser {

    String myPath;


    VulnParser(String myPath){
        this.myPath = myPath;
    }


    public ArrayList<Dependency> readVuln() {
        //JSON parser object to parse read file
        ArrayList<Dependency> vulnerabilities = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(myPath))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONObject depList = (JSONObject) obj;
            Set<String> keys = depList.keySet();

            for (String key : keys) {
                JSONArray vulnArr = (JSONArray) depList.get(key);
                for (Object vuln : vulnArr) {
                    vulnerabilities.add(parseDepObject( (JSONObject) vuln));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return vulnerabilities;
    }

    private static Dependency parseDepObject(JSONObject dependency)
    {
        JSONArray infArr = (JSONArray) dependency.get("vulnerable_purls");
        String info = (String) infArr.get(0);

        String[] info1 = info.split("/");
        String[] info2 = info1[2].split("@");

        return new Dependency(info1[1], info2[0], info2[1]);
    }
}
