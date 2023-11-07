package io.github.danielthedev.vcd.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class PostRequest<K> extends Request<K> {

	private byte[] data;
	
	public PostRequest(String url, PayloadType<K> payloadType, byte[] data) throws MalformedURLException {
		this(url, new HashMap<String, String>(), payloadType, data);
	}
	
	public PostRequest(String url, Map<String, String> headers, PayloadType<K> payloadType, byte[] data) throws MalformedURLException {
		super(url, headers, payloadType);
		this.data = data;
	}

	@Override
	public void initConnection(HttpURLConnection connection) throws IOException {
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
	}

	@Override
	public void readInputStream(byte[] buffer, int offset, int r) throws IOException {
		this.getPayloadType().readInputStream(buffer, offset, r);
	}
	
	@Override
	public void writeOutputStream(OutputStream out) throws IOException {
		out.write(data);
		out.flush();
	}
	
	@Override
	public int getContentLength() {
		return this.data.length;
	}
}
