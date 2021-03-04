package rest.indexing.rest;

import api.Document;
import api.ServerConfig;
import api.rest.IndexerService;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

public class ThirdPartyProxy implements IndexerService {

    private String secret;
    private OAuth10aService service;
    private Cache<String, List<String>> cache;
    private OAuth1AccessToken accessToken;

    public ThirdPartyProxy(String secret){
        this.secret = secret;
        this.cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(1000, TimeUnit.MINUTES).build();
    }

    @Override
    public List<String> search(String keywords) {
        List<String> results = null;
        String[] words = keywords.split("[ \\+]");
        Arrays.sort(words);
        String key = Arrays.stream(words).collect(Collectors.joining("+"));

        try {
            results = cache.get(key, () -> {
                List<String> r = new ArrayList<>();
                try {
                    // Ready to execute operations
                    OAuthRequest followersReq = new OAuthRequest(Verb.GET,
                            "https://api.twitter.com/1.1/search/tweets.json?q="
                                    + URLEncoder.encode(key, "UTF-8"));

                    service.signRequest(accessToken, followersReq);
                    final Response followersRes = service.execute(followersReq);
                    if (followersRes.getCode() != 200)
                        throw new WebApplicationException(FORBIDDEN);

                    JSONParser parser = new JSONParser();
                    JSONObject res = (JSONObject) parser.parse(followersRes.getBody());

                    JSONArray users = (JSONArray) res.get("statuses");
                    for (Object user : users) {
                        String url = "http://www.twitter.com/statuses/" + ((JSONObject) user).get("id");
                        r.add(url);
                    }

                    return r;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public void configure(String secret, ServerConfig config) {
        if(!this.secret.equals(secret))
            throw new WebApplicationException(FORBIDDEN);

        String tkn = config.getToken();
        final String apiKey = config.getApiKey();
        final String apiSecret = config.getApiSecret();

        service = new ServiceBuilder().apiKey(apiKey).apiSecret(apiSecret)
                .build(TwitterApi.instance());
        if(tkn == null || tkn.equals("")) { //DEBUG, IS NEVER REACHED
            String code;
            try {
                //AUTHENTICATION
                final Scanner in = new Scanner(System.in);

                OAuth1RequestToken requestToken = service.getRequestToken();
                config.setToken(requestToken.getToken());
                config.setTokenSecret(requestToken.getTokenSecret());

                System.err.println(requestToken.getToken()+"+"+ requestToken.getTokenSecret());

                // Obtain the Authorization URL
                final String authorizationUrl = service.getAuthorizationUrl(requestToken);
                System.out.println("Necessario dar permissao neste URL:");
                System.out.println(authorizationUrl);
                System.out.println("e copiar o codigo obtido para aqui:");
                System.out.print(">>");
                code = in.nextLine();

                accessToken = service.getAccessToken(requestToken, code);
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
                System.err.println("Failed Authentication");
                System.exit(1);
            }
        } else {
            try {
                accessToken = new OAuth1AccessToken(config.getToken(), config.getTokenSecret());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void add(String id, String secret, Document doc) {
        throw new WebApplicationException(FORBIDDEN);
    }

    @Override
    public void remove(String id, String secret) {
        throw new WebApplicationException(FORBIDDEN);
    }

    @Override
    public String ping() {
        return "pong";
    }
}
