package io.github.danielthedev.vcd.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

public abstract class Request<K> {

	private final PayloadType<K> payloadType;
	private final URL url;
	private final Map<String, String> headers;

	public Request(String url, PayloadType<K> payloadType) throws MalformedURLException {
		this(url, new HashMap<String, String>(), payloadType);
	}
	
	public Request(String url, Map<String, String> headers, PayloadType<K> payloadType) throws MalformedURLException {
		this.url = new URL(url);
		this.headers = headers;
		this.payloadType = payloadType;
	}

	public CompletableFuture<RequestResult<K>> openConnection() {
		return CompletableFuture.supplyAsync(()->{
			HttpURLConnection connection = null;
			
			try {
				connection = (HttpURLConnection) this.url.openConnection();
				for(Entry<String, String> entry : this.headers.entrySet()) connection.setRequestProperty(entry.getKey(), entry.getValue());
				this.initConnection(connection);
				if(connection.getDoOutput()) {
					connection.setRequestProperty("Content-Length", Integer.toString(this.getContentLength()));
				}
				connection.connect();
				if(connection.getDoOutput()) {
					try(OutputStream out = connection.getOutputStream()) {
						this.writeOutputStream(out);
					}
				}
				if(connection.getDoInput()) {
					try(InputStream in = connection.getInputStream()) {
						byte[] buffer = new byte[1024];
						int r;
						while((r = in.read(buffer, 0, buffer.length)) > -1) {
							this.readInputStream(buffer, 0, r);
						}
					}
				}
				this.endConnection();
				return new RequestResult<K>(connection.getResponseCode(), connection.getResponseMessage(), connection.getHeaderFields(), null, this.payloadType.getResult());
			} catch (IOException e) {
				this.endConnection();
				return new RequestResult<K>(-1, null, null, e, this.payloadType.getResult());
			} finally {
				if(connection != null) connection.disconnect();
			}
		});
	}
	
	public PayloadType<K> getPayloadType() {
		return payloadType;
	}

	public URL getUrl() {
		return url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public Request<K> setHeader(String key, String value) {
		this.headers.put(key, value);
		return this;
	}


	public void readInputStream(byte[] buffer, int offset, int r) throws IOException {}

	public void writeOutputStream(OutputStream out) throws IOException {}
	
	public abstract void initConnection(HttpURLConnection connection) throws IOException;
	
	public void endConnection() {}
	
	public int getContentLength() {
		return 0;
	}
	
	public static class RequestResult<T> {
		
		private final int responseCode;
		private final String responseMessage;
		private final Map<String, List<String>> responseHeaders;
		private final Exception exception;
		private final T payload;
		
		public RequestResult(int responseCode, String responseMessage, Map<String, List<String>> responseHeaders,
				Exception exception, T payload) {
			this.responseCode = responseCode;
			this.responseMessage = responseMessage;
			this.responseHeaders = responseHeaders;
			this.exception = exception;
			this.payload = payload;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public String getResponseMessage() {
			return responseMessage;
		}

		public Map<String, List<String>> getResponseHeaders() {
			return responseHeaders;
		}

		public Exception getException() {
			return exception;
		}

		public T getPayload() {
			return payload;
		}

		@Override
		public String toString() {
			return "RequestResult [responseCode=" + responseCode + ", responseMessage=" + responseMessage
					+ ", responseHeaders=" + responseHeaders + ", exception=" + exception + "]";
		}
		
	}
}
