package io.github.danielthedev.vcd;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.danielthedev.vcd.Manifest.VideoInfo;
import io.github.danielthedev.vcd.net.GetRequest;
import io.github.danielthedev.vcd.net.PayloadType;
public class PlayerOptions {
	
	private final Manifest videoManifest;
	private final Manifest presentationManifest;
	
	public PlayerOptions(JSONObject payload) throws MalformedURLException, InterruptedException, ExecutionException {
		Manifest videoManifest = null;
		Manifest presentationManifest = null;
		JSONArray streams = payload.getJSONObject("d").getJSONObject("Presentation").getJSONArray("Streams");
		for(int x = 0; x < streams.length(); x++) {
			JSONObject stream = ((JSONObject)streams.get(x));
			int streamType = stream.getInt("StreamType");
			if(streamType == 4 || streamType == 0) {
				JSONArray videoURLS = stream.getJSONArray("VideoUrls");
				
				for(int y = 0; y < videoURLS.length(); y++) {
					JSONObject videoURL = ((JSONObject)videoURLS.get(y));
					if(videoURL.getString("MediaType").equals("MP4")) {
						String manifestURL = videoURL.getString("Location");
						GetRequest<String> request = new GetRequest<String>(manifestURL, PayloadType.createStringPayload());
						Manifest manifest = new Manifest(request.openConnection().get().getPayload(), manifestURL);
						if(streamType == 0) videoManifest = manifest;
						else if(streamType == 4) presentationManifest = manifest;
					}
				}
			}
		}
		this.videoManifest = videoManifest;
		this.presentationManifest = presentationManifest;
	}
	
	public Map<String, Map.Entry<VideoInfo, VideoInfo>> getQualities() {
		Map<String, Entry<VideoInfo, VideoInfo>> map = new HashMap<String, Map.Entry<VideoInfo, VideoInfo>>();
		for(VideoInfo videoInfo : this.videoManifest.getVideoInfoList()) {
			
			for(VideoInfo presentationInfo : this.presentationManifest.getVideoInfoList()) {
				if(videoInfo.getResolution().equals(presentationInfo.getResolution())) {
					map.put(videoInfo.getResolution(), Map.entry(videoInfo, presentationInfo));
					break;
				}
			}
		}
		return map;
	}

	public Manifest getVideoManifest() {
		return videoManifest;
	}

	public Manifest getPresentationManifest() {
		return presentationManifest;
	}
}
