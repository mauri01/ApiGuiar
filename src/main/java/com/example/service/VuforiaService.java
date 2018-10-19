package com.example.service;

import com.example.model.TargetManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.io.FileUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

@Service("vuforiaService")
public class VuforiaService {

    //Server Keys
    private String accessKey = "fc6a1939b8d8b69213b5862a534950165e44c43b";
    private String secretKey = "2cfd440c40a6375af4c84065000490f083abdee5";

    private String url = "https://vws.vuforia.com";
    private String targetName = "Prueba1";
    private String imageLocation = "E:\\Tesis\\Targets\\prueba1.jpg";

    private TargetStatusPoller targetStatusPoller;
    private final float pollingIntervalMinutes = 60;//poll at 1-hour interval

    //Endpoint para obtener todos los targets de vuforia

    public void getTargets() throws URISyntaxException, ClientProtocolException, IOException {
        HttpGet getRequest = new HttpGet();
        HttpClient client = new DefaultHttpClient();
        getRequest.setURI(new URI(url + "/targets"));
        setHeaders(getRequest);

        HttpResponse response = client.execute(getRequest);
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }

    // Endpoint para insertar un nuevo target

    public JSONObject postTarget(String name, String pathImage, String pathVideo) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        HttpPost postRequest = new HttpPost();
        HttpClient client = new DefaultHttpClient();
        postRequest.setURI(new URI(url + "/targets"));
        JSONObject requestBody = new JSONObject();

        setRequestBody(requestBody,name,pathImage,pathVideo);
        postRequest.setEntity(new StringEntity(requestBody.toString()));
        setHeadersPost(postRequest); // Must be done after setting the body

        HttpResponse response = client.execute(postRequest);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);

        JSONObject jobj = new JSONObject(responseBody);

        //String uniqueTargetId = jobj.has("target_id") ? jobj.getString("target_id") : "";
        //System.out.println("\nCreated target with id: " + uniqueTargetId);

        return jobj;
    }

    private void setRequestBody(JSONObject requestBody,String name, String pathImage,String pathVideo) throws IOException, JSONException {
        File imageFile = new File(pathImage);
        if(!imageFile.exists()) {
            System.out.println("File location does not exist!");
            System.exit(1);
        }
        byte[] image = FileUtils.readFileToByteArray(imageFile);
        requestBody.put("name", name); // Mandatory
        requestBody.put("width", 1.0); // Mandatory
        requestBody.put("image", Base64.encodeBase64String(image)); // Mandatory
        requestBody.put("active_flag", 1); // Optional
        JSONObject json = new JSONObject();
        json.put("videoUrl", pathVideo);
        requestBody.put("application_metadata", Base64.encodeBase64String(json.toString().getBytes())); // Optional
    }

    private void setHeadersPost(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader(new BasicHeader("Content-Type", "application/json"));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }

    //@Override
    public void OnTargetStatusUpdate(TargetState target_state) {
        if (target_state.hasState) {

            String status = target_state.getStatus();

            System.out.println("Target status is: " + (status != null ? status : "unknown"));

            if (target_state.getActiveFlag() == true && "success".equalsIgnoreCase(status)) {

                targetStatusPoller.stopPolling();

                System.out.println("Target is now in 'success' status");
            }
        }
    }

    public JSONObject updateTarget(TargetManager target) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        HttpPut putRequest = new HttpPut();
        HttpClient client = new DefaultHttpClient();
        putRequest.setURI(new URI(url + "/targets/" + target.getTargetId()));
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", target.getName());
        requestBody.put("active_flag", target.isActive());

        //setRequestBody(requestBody);
        putRequest.setEntity(new StringEntity(requestBody.toString()));
        setHeadersUpdate(putRequest); // Must be done after setting the body

        HttpResponse response = client.execute(putRequest);
        /*System.out.println(EntityUtils.toString(response.getEntity()));

        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);

        JSONObject responseObject = );*/
        return new JSONObject(EntityUtils.toString(response.getEntity()));

    }

    private void setRequestBody(JSONObject requestBody) throws IOException, JSONException {
        requestBody.put("active_flag", true); // Optional
        requestBody.put("application_metadata", Base64.encodeBase64String("Vuforia test metadata".getBytes())); // Optional
    }

    private void setHeadersUpdate(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader(new BasicHeader("Content-Type", "application/json"));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }
}
