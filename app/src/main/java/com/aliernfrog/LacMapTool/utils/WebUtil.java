package com.aliernfrog.LacMapTool.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtil {

    public static String getContentFromURL(String urlString) throws Exception {
        URL url = new URL(urlString);
        String _line;
        String _full = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        while ((_line = reader.readLine()) != null) {
            _full += "\n"+_line;
        }
        reader.close();
        if (_full.startsWith("\n")) _full.replace("\n", "");
        return _full;
    }

    public static String doGetRequest(String Url) throws Exception {
        URL url = new URL(Url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        String res = getResFromConnection(connection);
        connection.disconnect();
        return res;
    }

    public static String doPostRequest(String Url, JSONObject obj) throws Exception {
        URL url = new URL(Url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = obj.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        String res = getResFromConnection(connection);
        connection.disconnect();
        return res;
    }

    public static String getResFromConnection(HttpURLConnection connection) throws Exception {
        StringBuilder res = new StringBuilder();
        String resLine;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            while ((resLine = br.readLine()) != null) {
                if (res.length() > 0) resLine = "<br />"+resLine;
            res.append(resLine.trim());
            }
        }
        return res.toString();
    }
}
