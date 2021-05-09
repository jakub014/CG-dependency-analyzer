package GitHubAPIRequests;

import java.util.Scanner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class GitHubAPIClient {
    private WebClient client;

    public GitHubAPIClient(String url) {

        Scanner in = new Scanner(System.in);

        System.out.println("Enter your github username:");
        String username = in.nextLine();
        System.out.println("Enter a corresponding access token:");
        String token = in.nextLine();


        client = WebClient.builder()
            .baseUrl(url)
            .defaultHeaders(header -> header.setBasicAuth(username, token))
            .build();
    }


    public Mono<String> getRepoInfo(String user, String repoName)
    {
        return client.get()
            .uri("/repos/"+ user +"/" + repoName)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(httpStatus -> HttpStatus.NOT_FOUND.equals(httpStatus),
                    clientResponse -> Mono.empty())
            .bodyToMono(String.class);
    }

}
