package io.github.danielthedev.vcd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import io.github.danielthedev.vcd.awt.DownloadWindow;
import io.github.danielthedev.vcd.browser.Browser;
import io.github.danielthedev.vcd.browser.DownloadListener;

public class VideoCollegeDownloader implements DownloadListener {

	private final Browser browser = new Browser(this);
	private Video video;
	
    public static void main( String[] args ) throws InterruptedException, URISyntaxException, IOException, ExecutionException
    {
    	
    	VideoCollegeDownloader cvd = new VideoCollegeDownloader();     
    	cvd.start();
    }
	
	public void start() throws InterruptedException {
		this.loadLibraries();
		this.browser.openWebsite();
	}

	@Override
	public void downloadVideo(Video video) {
		this.video = video;
		try {
			DownloadWindow downloadWindow = new DownloadWindow();
			downloadWindow.open();
			this.video.loadPlayerOptions();
			downloadWindow.loadVideo(video, videoInfo->{
				new Thread(()->{
					try {
						this.video.download(videoInfo, downloadWindow::loadResult, downloadWindow::printLog);
					} catch (InterruptedException | ExecutionException | IOException e) {
						e.printStackTrace();
					}
				}).start();
			});
		} catch (MalformedURLException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}

	public Browser getBrowser() {
		return browser;
	}
	
	public void loadLibraries() {
		File file = new File("ffmpeg.exe");
		if (!file.exists()) {
			
			try (InputStream in = VideoCollegeDownloader.class.getClassLoader().getResourceAsStream(file.getName())) {
				try (OutputStream out = new FileOutputStream(file)) {
					byte[] buffer = new byte[1024];
					int r;
					while ((r = in.read(buffer, 0, buffer.length)) != -1) {
						out.write(buffer, 0, r);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
