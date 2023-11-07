package io.github.danielthedev.vcd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import org.json.JSONObject;

import io.github.danielthedev.vcd.Manifest.VideoInfo;
import io.github.danielthedev.vcd.awt.DownloadWindow;
import io.github.danielthedev.vcd.ffmpeg.FFMPEG;
import io.github.danielthedev.vcd.net.GetRequest;
import io.github.danielthedev.vcd.net.PayloadType;
import io.github.danielthedev.vcd.net.PostRequest;
import io.opentelemetry.internal.shaded.jctools.queues.MessagePassingQueue.Consumer;

public class Video {
	
	private static final String PLAYER_OPTIONS_URL = "https://videocollege.tue.nl/Mediasite/PlayerService/PlayerService.svc/json/GetPlayerOptions";
	private static final String PLAYER_OPTIONS_REFERAL_URL = "https://videocollege.tue.nl/mediasite/Showcase/%s/Presentation/%s/Channel/%s";

	private PlayerOptions playerOptions;
	
	private String title;
	private String departmentID;
	private String channelID;
	private String videoID;
	
	private String mediaAuthToken;
	
	public Video(String departmentID, String channelID, String videoID, String title) {
		this.departmentID = departmentID;
		this.channelID = channelID;
		this.videoID = videoID;
		this.title = title;
	}

	public String getDepartmentID() {
		return departmentID;
	}

	public void setDepartmentID(String departmentID) {
		this.departmentID = departmentID;
	}

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getVideoID() {
		return videoID;
	}

	public void setVideoID(String videoID) {
		this.videoID = videoID;
	}

	public String getMediaAuthToken() {
		return mediaAuthToken;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMediaAuthToken(String mediaAuthToken) {
		this.mediaAuthToken = mediaAuthToken;
	}
	
	public void loadPlayerOptions() throws MalformedURLException, InterruptedException, ExecutionException {
		JSONObject data = new JSONObject();
		JSONObject options = new JSONObject();
		data.put("getPlayerOptionsRequest", options);
		options.put("QueryString", String.format("?player=%s&playfrom=0&covertitle=false", departmentID));
		options.put("ResourceId", channelID);
		options.put("UrlReferrer", PLAYER_OPTIONS_REFERAL_URL.formatted(departmentID, channelID, videoID));
		PostRequest<JSONObject> request = new PostRequest<JSONObject>(PLAYER_OPTIONS_URL, PayloadType.createJsonPayload(), data.toString().getBytes());
		request
		.setHeader("Cookie", String.format("MediasiteAuth=%s;", this.getMediaAuthToken()))
		.setHeader("Content-Type", "application/json; charset=utf-8");
		this.playerOptions = new PlayerOptions(request.openConnection().get().getPayload());
	}
	
	public Callable<Void> createSegmentDownloadThread(String segmentsURL, String baseURL, File file, BiConsumer<Integer, String> logger, int logType) {
		return ()->{
			List<String> segments = getSegmentList(segmentsURL);
			this.downloadSegments(baseURL, segments, file, logger, logType);
			logger.accept(logType, "clear");
			logger.accept(logType, "Finished downloading");
			return null;
		};
	}

	public void download(Entry<VideoInfo, VideoInfo> videoInfo, Consumer<File> callback, BiConsumer<Integer, String> logger) throws InterruptedException, ExecutionException, FileNotFoundException, IOException {
		videoInfo.getKey().getAudio().getSegmentsURL();
		videoInfo.getValue().getSegmentsURL();
		
		File videoFolder = new File("Videos");
		if(!videoFolder.exists()) videoFolder.mkdirs();
		File ffmpegFile =  new File("ffmpeg.exe");
		File videoFile = new File(videoFolder, "video.mp4");
		File audioFile = new File(videoFolder, "audio.mp4");
		File presentationFile = new File(videoFolder, "presentation.mp4");
		File combinedFile = new File(videoFolder, "combinedVideo.mp4");
		File outputFile = new File(videoFolder, "finalVideo.mp4");
		File titleFile = new File(videoFolder, this.formatVideoTitle() + ".mp4");
		if(titleFile.exists()) titleFile.delete();
		
		Callable<Void> downloadVideo = this.createSegmentDownloadThread(videoInfo.getKey().getSegmentsURL(), this.playerOptions.getVideoManifest().getBaseURL(), videoFile, logger, DownloadWindow.LOG_VIDEO);
		Callable<Void> downloadAudio = this.createSegmentDownloadThread(videoInfo.getKey().getAudio().getSegmentsURL(), this.playerOptions.getVideoManifest().getBaseURL(), audioFile, logger, DownloadWindow.LOG_AUDIO);
		Callable<Void> downloadPresentation = this.createSegmentDownloadThread(videoInfo.getValue().getSegmentsURL(), this.playerOptions.getPresentationManifest().getBaseURL(), presentationFile, logger, DownloadWindow.LOG_PRESENTATION);
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		
		executorService.invokeAll(Arrays.asList(downloadVideo, downloadAudio, downloadPresentation));
		FFMPEG.concatenateVideos(ffmpegFile, videoFile, presentationFile, combinedFile, logger, DownloadWindow.LOG_EDITOR);
		FFMPEG.combineAudioAndVideo(ffmpegFile, audioFile, combinedFile, outputFile, logger, DownloadWindow.LOG_EDITOR);
		outputFile.renameTo(titleFile);
		callback.accept(titleFile);
	}
	
	private String formatVideoTitle() {
		char[] forbiddenChars = new char[] {'\\','/',':','"','?','*','<','>','|'};
		String newTitle = this.title;
		for(char c : forbiddenChars) {
			newTitle = newTitle.replace(c+"", "");
		}
		return newTitle;
	}
	
	public PlayerOptions getPlayerOptions() {
		return this.playerOptions;
	}

	public void downloadSegments(String endpoint, List<String> segments, File file, BiConsumer<Integer, String> logger, int type) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		try(FileOutputStream out = new FileOutputStream(file)) {
			for(int i = 0; i < segments.size(); i++) {	
				GetRequest<Void> request = new GetRequest<Void>(endpoint + segments.get(i), new PayloadType.ByteStream(out)); 
				if(request.openConnection().get().getResponseCode() != 200) return;
				logger.accept(type, "fetched segment ("+ i + "/" + segments.size() + ")");
			}
		}
	}
	
	public List<String> getSegmentList(String url) throws InterruptedException, ExecutionException, MalformedURLException {
		List<String> list = new ArrayList<String>();
		GetRequest<String> request = new GetRequest<String>(url, PayloadType.createStringPayload()); 
		String payload = request.openConnection().get().getPayload();
		String[] lines = payload.split("\r\n");
		for(String line : lines) {
			if(line.startsWith("#EXT-X-MAP:URI=\"")) {
				list.add(line.substring(16, line.length()-1));
			}
			if(!line.startsWith("#")) list.add(line);
		}	
		return list;
	}
	
}

