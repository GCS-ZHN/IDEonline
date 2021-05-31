package org.gcszhn.system.service.until;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpRequest {
	public static HttpURLConnection getHttpURLConnection(String url, String method) {
		HttpURLConnection connection = null;
		try {
			URL realUrl = new URL(url);
			connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return connection;
	}
}