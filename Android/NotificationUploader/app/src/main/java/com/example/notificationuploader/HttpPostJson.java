package com.example.notificationuploader;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

public class HttpPostJson {
    public static JSONObject doPost_withApikey(String requestUrl, JSONObject input, String apikey, int connTimeout) throws Exception{
        BufferedReader reader = null;
        OutputStream os = null;
        HttpURLConnection urlCon = null;

        try {
            URL url = new URL(requestUrl);

            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setConnectTimeout(connTimeout);
//            urlCon.setReadTimeout(10000);
            urlCon.setRequestMethod("POST");

            urlCon.setDoInput( true );
            urlCon.setDoOutput(input != null);
            urlCon.setRequestProperty("Content-Type", "application/json");
            if( apikey != null )
                urlCon.setRequestProperty("X-API-KEY", apikey);
            urlCon.setUseCaches(false);

            urlCon.connect();

            if( input != null ) {
                PrintStream ps = new PrintStream(urlCon.getOutputStream());
                ps.print(input.toString());
                ps.close();
            }

            int status = urlCon.getResponseCode();
            if( status != HttpURLConnection.HTTP_OK)
                throw new Exception("HTTP Status Error");

            InputStream is = urlCon.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            StringBuffer buffer = new StringBuffer();
            String str;
            while (null != (str = reader.readLine())) {
                buffer.append(str);
            }
            is.close();

            return new JSONObject(buffer.toString());
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (os != null)
                    os.close();
                if (urlCon != null)
                    urlCon.disconnect();
            } catch (IOException e) {
            }
        }
    }
}