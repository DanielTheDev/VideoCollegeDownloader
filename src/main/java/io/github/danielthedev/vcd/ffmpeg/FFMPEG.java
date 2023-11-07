package io.github.danielthedev.vcd.ffmpeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;


public class FFMPEG {
	
	public static void combineAudioAndVideo(File ffmpeg, File video, File audio, File output, BiConsumer<Integer, String> logger, int logType) throws IOException, InterruptedException {
		if(output.exists()) output.delete();
		if(!(video.exists() || audio.exists())) throw new IOException("video or audio not exists");
		String[] commands = {ffmpeg.getAbsolutePath(), "-i", video.getAbsolutePath(), "-i", audio.getAbsolutePath(), "-c", "copy", output.getAbsolutePath()};
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
		    logger.accept(logType, line);
		}
		process.waitFor();
		video.delete();
		audio.delete();
		logger.accept(logType, "clear");
		logger.accept(logType, "Finished merging");
	}

	
	public static void concatenateVideos(File ffmpeg, File video1, File video2, File output, BiConsumer<Integer, String> logger, int type) throws IOException, InterruptedException {
		if(output.exists()) output.delete();
		if(!(video1.exists() || video2.exists())) throw new IOException("video1 or video2 not exists");
		String[] commands = {ffmpeg.getAbsolutePath(), "-i", video1.getAbsolutePath(), "-i", video2.getAbsolutePath(), "-filter_complex", "[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]", "-map", "[vid]", "-c:v", "libx264", "-crf", "23", "-preset", "ultrafast", output.getAbsolutePath()};
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			logger.accept(type, line);
		}
		process.waitFor();
		video1.delete();
		video2.delete();
	}
	

}
