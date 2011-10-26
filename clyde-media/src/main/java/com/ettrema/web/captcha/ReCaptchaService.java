package com.ettrema.web.captcha;

import com.bradmcevoy.http.Request;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author brad
 */
public class ReCaptchaService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReCaptchaService.class);
    private String paramChallenge = "recaptcha_challenge_field";
    private String paramResponse = "recaptcha_response_field";
    private String recaptchaUrl = "http://www.google.com/recaptcha/api/verify";

    public boolean isValid(String privateKey, Map<String, String> parameters, Request request) throws IOException {
        String challenge = parameters.get(paramChallenge);
        String response = parameters.get(paramResponse);
        if (StringUtils.isEmpty(challenge)) {
            log.warn("challenge is emtpy");
            return false;
        }
        if (StringUtils.isEmpty(response)) {
            log.trace("response is emtpty");
            return false;
        }
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(recaptchaUrl);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        
        nameValuePairs.add(new BasicNameValuePair("privatekey", "privateKey"));
        nameValuePairs.add(new BasicNameValuePair("remoteip", request.getRemoteAddr()));
        nameValuePairs.add(new BasicNameValuePair("challenge", challenge));
        nameValuePairs.add(new BasicNameValuePair("response", response));
        
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        
        if (log.isTraceEnabled()) {
            log.trace("remoteIP: " + request.getRemoteAddr());
            log.trace("challenge: " + challenge);
            log.trace("response: " + response);
        }
        HttpResponse resp = httpClient.execute(post);
        int respCode = resp.getStatusLine().getStatusCode();
        if (respCode >= 200 && respCode < 300) {
            if (log.isTraceEnabled()) {
                log.trace("isValid. Got response code: " + respCode);
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            resp.getEntity().writeTo(bout);
            String body = bout.toString("UTF-8");
            if (StringUtils.isEmpty(body)) {
                throw new RuntimeException("Error response from recaptcha server. Body is empty. Response code: " + respCode);
            } else {
                body = body.trim();
                if (body.startsWith("true")) {
                    log.trace("validated ok");
                    return true;
                } else {
                    String[] arr = body.split("\n");
                    if (log.isTraceEnabled()) {
                        if (arr.length < 2) {
                            log.trace("validation result is false, but no reason given");
                        } else {
                            log.trace("validaion result is false, reason: " + arr[1]);
                        }
                    }
                    return false;
                }
            }
        } else {
            throw new RuntimeException("Error response from recaptcha server. HTTP Response code it: " + respCode);
        }
    }

    public String getParamChallenge() {
        return paramChallenge;
    }

    public void setParamChallenge(String paramChallenge) {
        this.paramChallenge = paramChallenge;
    }

    public String getParamResponse() {
        return paramResponse;
    }

    public void setParamResponse(String paramResponse) {
        this.paramResponse = paramResponse;
    }

    public String getRecaptchaUrl() {
        return recaptchaUrl;
    }

    public void setRecaptchaUrl(String recaptchaUrl) {
        this.recaptchaUrl = recaptchaUrl;
    }
}
