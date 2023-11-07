package io.github.danielthedev.vcd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manifest {

	private static final Pattern RESOLUTION_PATTERN = Pattern.compile("RESOLUTION=([0-9]+x[0-9]+)");
	private static final Pattern AUDIO_PATTERN = Pattern.compile("AUDIO=\"(.*?)\"");
	private static final Pattern AUDIO_URI_PATTERN  = Pattern.compile("URI=\"(.*?)\"");
	private static final Pattern AUDIO_GROUP_PATTERN  = Pattern.compile("GROUP-ID=\"(.*?)\"");
	
	private final List<AudioInfo> audioInfoList = new ArrayList<AudioInfo>(); 
	private final List<VideoInfo> videoInfoList = new ArrayList<VideoInfo>();
	private final String baseURL;
	
	public Manifest(String manifest, String manifestURL) {
		this.baseURL = manifestURL.substring(0, manifestURL.indexOf("/manifest")+1);
		String[] lines = manifest.split("\r\n");
		for(int x = 0; x < lines.length; x++) {
			String line = lines[lines.length - x - 1];
			if(line.startsWith("#EXT-X-STREAM-INF")) {
				Matcher matcher = RESOLUTION_PATTERN.matcher(line); matcher.find();
				String resolution = matcher.group(1);
				matcher = AUDIO_PATTERN.matcher(line); matcher.find();
				String audio = matcher.group(1);
				String manifestRef = lines[lines.length - x];
				AudioInfo audioInfo = audioInfoList.stream().filter(info->info.getGroupID().equals(audio)).findFirst().get();
				this.videoInfoList.add(new VideoInfo(resolution, manifestRef, audioInfo));
			} else if(line.startsWith("#EXT-X-MEDIA:TYPE=AUDIO")) {
				Matcher matcher = AUDIO_URI_PATTERN.matcher(line); matcher.find();
				String uri = matcher.group(1);
				matcher = AUDIO_GROUP_PATTERN.matcher(line); matcher.find();
				String groupID = matcher.group(1);
				this.audioInfoList.add(new AudioInfo(uri, groupID));
			}
		}
		
	}

	public String getBaseURL() {
		return baseURL;
	}

	public List<AudioInfo> getAudioInfoList() {
		return audioInfoList;
	}

	public List<VideoInfo> getVideoInfoList() {
		return videoInfoList;
	}

	public class VideoInfo {
		
		//private final int bandwidth;
		//private final String codec;
		private final String resolution;
		private final String manifest;
		private final AudioInfo audio;
		
		public VideoInfo(String resolution, String manifest, AudioInfo audio) {
			this.resolution = resolution;
			this.manifest = manifest;
			this.audio = audio;
		}

		public String getResolution() {
			return resolution;
		}

		public String getManifest() {
			return manifest;
		}

		public AudioInfo getAudio() {
			return audio;
		}
		
		public String getSegmentsURL() {
			return Manifest.this.baseURL + this.manifest;
		}
	}
	
	public class AudioInfo {
		
		private final String manifest;
		private final String groupID;

		public AudioInfo(String manifest, String groupID) {
			this.manifest = manifest;
			this.groupID = groupID;
		}

		public String getManifest() {
			return manifest;
		}

		public String getGroupID() {
			return groupID;
		}
		
		public String getSegmentsURL() {
			return Manifest.this.baseURL + this.manifest;
		}
	}
}
