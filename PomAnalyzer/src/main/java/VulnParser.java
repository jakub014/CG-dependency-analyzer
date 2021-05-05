import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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

            JSONArray depList = (JSONArray) obj;

            for (Object dep : depList) {
                String[] depInfo = ((String) dep).split(":");
                if (depInfo.length == 3) {
                    vulnerabilities.add(new Dependency(depInfo[0], depInfo[1], depInfo[2]));
                }
            }


            //for (Object dep : depList) {
               // vulnerabilities.add(parseDepObject( (JSONObject) dep));
            //}

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

        String version = (String) dependency.get("version");

        String packageName = (String) dependency.get("package_name");
        String[] packageInfo = packageName.split(":");

        return new Dependency(packageInfo[0], packageInfo[1], version);
    }
}
