package GitHubAPIRequests;

import GitHubAPIRequests.GitHubAPIClient;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.JSONObject;
import reactor.core.publisher.Mono;
import org.json.simple.parser.JSONParser;

public class InformationGathering {

    public static void main(String[] args) throws OPALException, ParseException, IOException, org.json.simple.parser.ParseException {
        GitHubAPIClient client = new GitHubAPIClient("https://api.github.com");

        JSONParser parser = new JSONParser();
        JSONArray data = (JSONArray) parser.parse(new FileReader("src/main/resources/vuln-repos.json"));


        int failures = 0;
        int total = data.size();

        int counter = 0;

        org.json.JSONArray endResults = new org.json.JSONArray();


        for (Object o : data) {
            org.json.simple.JSONObject temp = (org.json.simple.JSONObject) o;

            String url = (String) temp.get("repository");

            String[] splitUrl = url.split("/");
            try {
                String user = "";
                String repo = "";
                boolean found = false;

                for(int i = 0; i < splitUrl.length; i++) {
                    if(splitUrl[i].endsWith(".git")) {
                        repo = splitUrl[i];
                        var tmp = splitUrl[i - 1].split(":");
                        user = tmp[tmp.length - 1];
                        found = true;
                    }
                }

                if(!found) {
                    user = splitUrl[3];
                    repo = splitUrl[4];
                }

                if(repo.endsWith(".git"))
                    repo = repo.substring(0, repo.length() - 4);

                //DO SOMETHING HERE
                endResults.put(processRepo(client, user, repo));
                counter++;
                System.out.println(counter);
            }
            catch (Exception e) {
                failures++;
            }

        }

        System.out.println("Parsed all URLs ~" + failures + " failures out of " + total);

        writeResultsToFile(endResults, "");
    }

    /**
     * Requests GitHubs REST API to gather information about specified repository
     * @param client api client
     * @param user the projects user
     * @param repo the repository name
     * @return a JSONOBject containing relevant response data.
     * @throws ParseException
     */
    public static JSONObject processRepo(GitHubAPIClient client, String user, String repo) throws ParseException {
        Mono<String> result = client.getRepoInfo(user, repo);

        if(Mono.empty().equals(result)) {
            System.out.print("---Failure: " + user + "/" + repo + " -----  ");
        } else {
            JSONObject jresult = new JSONObject(result.block());

            Integer stars = (Integer) jresult.get("stargazers_count");

            Date lastUpdated = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse((String) jresult.get("updated_at"));

            JSONObject re = new JSONObject();

            re.put("user", user);
            re.put("repository", repo);
            re.put("stars", stars);
            re.put("lastUpdated", lastUpdated.getTime());
            re.put("fullresponse", jresult);

            return re;
        }
        return null;
    }

    /**
     * Writes passed object to a file.
     * @param o the object
     * @param filename the filename to be written to
     */
    public static void writeResultsToFile(Object o, String filename) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(o.toString());
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
