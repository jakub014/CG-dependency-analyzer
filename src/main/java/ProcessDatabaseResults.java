import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ProcessDatabaseResults {

    public static void main(String[] args) throws IOException {
        processDatabaseResults();
    }

    public static void processDatabaseResults() throws IOException {
        Scanner sc = new Scanner(new File("data-15-07/fastenDBquery15-07.txt"));
        FileWriter writer = new FileWriter("data-15-07/extractedRepositories.txt");
        while (sc.hasNextLine()) {
            String result = extractGitHubURL(sc.nextLine());
            if(result != null) {
                if(result.endsWith(".git"))
                    result = result.substring(0, result.length() - 4);
                writer.append(result + "\n");
            }
        }
    }

    public static String extractGitHubURL(String s) {
        String ss =s.trim();
        String[] splitUrl = s.split("/");
        try {
            String user = "";
            String repo = "";
            boolean found = false;

            if(ss.startsWith("https:github.com") ||
               ss.startsWith("github.com")) {
                return splitUrl[1] + "/" + splitUrl[2];
            }

            if(ss.startsWith("scm:git:git@github.com:")) {
                return ss.replace("scm:git:git@github.com:","");
            }
            if(ss.startsWith("git@github.com:")) {
                return ss.replace("git@github.com:","");
            }
            if(ss.startsWith("https://github.com:")) {
                return ss.replace("https://github.com:","");
            }

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
            String githubURL = user + "/" + repo;
            return githubURL;
        }
        catch (Exception e) {
            System.out.println(s);
            return null;
        }
    }
}
