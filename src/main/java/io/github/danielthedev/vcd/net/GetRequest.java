package io.github.danielthedev.vcd.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Map;

public class GetRequest<K> extends Request<K> {
	
	public GetRequest(String url, PayloadType<K> payloadType) throws MalformedURLException {
		super(url, payloadType);
	}
	
	public GetRequest(String url, Map<String, String> headers, PayloadType<K> payloadType) throws MalformedURLException {
		super(url, headers, payloadType);
	}

	@Override
	public void initConnection(HttpURLConnection connection) throws IOException {
		connection.setRequestMethod("GET");
	}

	@Override
	public void readInputStream(byte[] buffer, int offset, int r) throws IOException {
		this.getPayloadType().readInputStream(buffer, offset, r);
	}

}
