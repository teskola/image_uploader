package com.teskola.imageuploader;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class FiwarePatcher {
    public void test(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://172.16.101.172:1026/ngsi-ld/v1/entities/urn:ngsi-ld:ImageTestEntity:001/attrs/imageTestEntityUrl";
        JSONObject body = new JSONObject();
        try {
            body.put("value", "http://172.16.101.198:9000/test-images/logo.svg");
            body.put("type", "Property");
            body.put("unitCode", "url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) Log.d("Response", response.toString());
            }
        }, error -> Log.e("Error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Link", "<http://context/ngsi-context.jsonld>>; rel='http://www.w3.org/ns/json-ld#context'; type='application/ld+json'");
                headers.put("fiware-service", "openiot");
                headers.put("fiware-servicepath", "/");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    Log.d("Reponse", String.valueOf(response.statusCode));
                    JSONObject result = null;

                    if (jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }
}
