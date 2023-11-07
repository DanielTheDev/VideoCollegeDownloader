package io.github.danielthedev.vcd.net;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

public abstract class PayloadType<T> {

	public static class StringPayload extends PayloadType<String> {

		private final StringBuilder builder = new StringBuilder();
		
		@Override
		public void readInputStream(byte[] buffer, int offset, int r) throws IOException {
			this.builder.append(new String(buffer, offset, r));
		}

		@Override
		public String getResult() {
			return builder.toString();
		}
	}
	
	public static class JSONPayload extends PayloadType<JSONObject> {

		private final StringBuilder builder = new StringBuilder();
		
		@Override
		public void readInputStream(byte[] buffer, int offset, int r) throws IOException {
			this.builder.append(new String(buffer, offset, r));
		}

		@Override
		public JSONObject getResult() {
			return new JSONObject(builder.toString());
		}
	}
	
	public static class ByteStream extends PayloadType<Void>{
		
		private final OutputStream out;
		
		public ByteStream(OutputStream out) {
			this.out = out;
		}

		@Override
		public Void getResult() {
			return null;
		}

		@Override
		public void readInputStream(byte[] buffer, int offset, int r) throws IOException {
			out.write(buffer, offset, r);
		}
		
	}
	
	public static JSONPayload createJsonPayload() {
		return new JSONPayload();
	}
	
	public static StringPayload createStringPayload() {
		return new StringPayload();
	}
	
	public abstract T getResult();
	
	public abstract void readInputStream(byte[] buffer, int offset, int r) throws IOException;
}
