package payment.tools.auth;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * <pre>
 * Modify Information:
 * Author       Date          Description
 * ============ ============= ============================
 * liuzhicheng      2017年12月13日     使用HTTP进行jenkins鉴权(未使用)
 * </pre>
 */
public class JenkinsScraper {

    /**
     * <p>
     * 登录鉴权
     * </p>
     * 
     * @author liuzhicheng
     * @date 2017年12月13日 上午10:09:48
     * @param urlString
     * @param username
     * @param password
     * @return
     */
    public String scrape(String urlString, String xmlString, String username, String password) throws ClientProtocolException, IOException {
        URI uri = URI.create(urlString);
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(username, password));
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/xml");
        httpPost.setEntity(new StringEntity(xmlString));

        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        HttpResponse response = httpClient.execute(host, httpPost, localContext);

        return EntityUtils.toString(response.getEntity());
    }
}
