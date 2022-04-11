package com.dmall;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Http 工具类
 */
public class HttpUtils {

    /**
     * Http Get请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String httpGet(String url) throws IOException {
        HttpURLConnection httpConnection = getHttpConnection(url, "GET");
        httpConnection.connect();
        return getContent(httpConnection);
    }


    /**
     * Http Post请求
     *
     * @param httpUrl
     * @param body
     * @return
     * @throws IOException
     */
    public static String httpPost(String httpUrl, String body) throws IOException {
        HttpURLConnection httpConnection = getHttpConnection(httpUrl, "POST");
        httpConnection.connect();
        write(httpConnection, body);
        return getContent(httpConnection);
    }

    /**
     * 获取Http连接信息
     *
     * @param httpUrl
     * @return
     * @throws IOException
     */
    private static HttpURLConnection getHttpConnection(String httpUrl, String requestMethod) throws IOException {
        URL url = new URL(httpUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setConnectTimeout(60 * 1000);
        httpURLConnection.setReadTimeout(60 * 1000);
        httpURLConnection.setRequestMethod(requestMethod);
        return httpURLConnection;
    }

    /**
     * HttpPost请求，写数据
     *
     * @param httpURLConnection
     * @param body
     * @throws IOException
     */
    private static void write(HttpURLConnection httpURLConnection, String body) throws IOException {
        BufferedOutputStream bos = null;
        try {
            OutputStream outputStream = httpURLConnection.getOutputStream();
            bos = new BufferedOutputStream(outputStream);
            bos.write(body.getBytes(StandardCharsets.UTF_8));
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }

    /**
     * 根据HttpConnection获取返回数据
     *
     * @param httpURLConnection
     * @return
     * @throws IOException
     */
    private static String getContent(HttpURLConnection httpURLConnection) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (httpURLConnection == null) {
            return "";
        }
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(isr);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return builder.toString();
    }

}
