package io.github.undercovergoose.bazaarNotifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Request {
    public final CloseableHttpClient httpClient = HttpClients.createDefault();
    public String sendGet(String url) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();
        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        HttpGet request = new HttpGet(url);

        try {
            CloseableHttpResponse response = (CloseableHttpResponse)httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if(entity != null) return EntityUtils.toString(entity);
            else return null;

        }catch(Exception e) {
            return null;
        }
    }
    public JsonObject jsonGet(String url) throws Exception {
        Request req = new Request();
        String res = null;

        res = req.sendGet(url);

        if(res == null) {
            return null;
        }else {
            try {
                Object data = new JsonParser().parse(res);
                return (JsonObject) data;
            }catch(Exception e) {
//                System.out.println(e);
                return null;
            }
        }
    }

}
