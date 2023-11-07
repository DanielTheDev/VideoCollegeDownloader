package io.github.danielthedev.vcd.browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.danielthedev.vcd.Video;
import io.github.danielthedev.vcd.VideoCollegeDownloader;

public class Browser {

	private static final String[] ALLOWED_SITES_PREFIX = {"https://login.microsoftonline.com", "https://sts.tue.nl", "https://videocollege.tue.nl", "https://studiegids.tue.nl"};
	private static Pattern URL_PATTERN = Pattern.compile("https:\\/\\/videocollege\\.tue\\.nl\\/mediasite\\/Showcase\\/([a-f0-9]{34})\\/Presentation\\/([a-f0-9]{34})\\/Channel\\/([a-f0-9]{34})");
	private static final String HOME_URL = "https://studiegids.tue.nl/praktische-zaken/it-voorzieningen/online-systemen/videocolleges/overzicht-video-colleges";
	private static String SCRIPT;
	
	private Video selectedVideo;
	private boolean scriptActive = false;
	private ChromeDriver driver;
	private final DownloadListener listener;
	
	public static final Object LOCK = new Object();
	
	static {
		try {
			try(InputStream in = VideoCollegeDownloader.class.getClassLoader().getResourceAsStream("script.js")) {
				SCRIPT = new String(IOUtils.toByteArray(in));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		WebDriverManager.chromedriver().setup();
	}
	
	public Browser(DownloadListener listener) {
		this.listener = listener;
	}
	
	public void openWebsite() throws InterruptedException {
		this.driver = new ChromeDriver();
		this.driver.manage().window().maximize();
		this.driver.get(HOME_URL);

		try {
	        while(hasSiteOpen(this.driver.getCurrentUrl(), ALLOWED_SITES_PREFIX)) {
	        	while(hasSiteOpen(this.driver.getCurrentUrl(), ALLOWED_SITES_PREFIX[2])) {
	        		Matcher matcher = URL_PATTERN.matcher(this.driver.getCurrentUrl());
	        		if(matcher.matches()) {
	        			Cookie cookie = this.driver.manage().getCookieNamed("download-xhr");
	            		if(!(cookie == null)) {
	            			this.selectedVideo = new Video(matcher.group(1), matcher.group(2), matcher.group(3), this.driver.getTitle());
	            			this.selectedVideo.setMediaAuthToken(this.driver.manage().getCookieNamed("MediasiteAuth").getValue());	        
	            			this.removeScript();
	            			this.driver.manage().deleteCookie(cookie);
	            			this.listener.downloadVideo(this.selectedVideo);
	            			synchronized(Browser.LOCK) {
	            				LOCK.wait();
	            			}
	            		}
	            		this.addScript();
	        		} else {
	        			this.removeScript();
	        		}
	        		Thread.sleep(250);
	        	}
	        	Thread.sleep(250);
	        }
		} catch(NoSuchWindowException e) {
			System.exit(0);
		} finally {
			this.closeWebsite();
		}
	}
	
	public void closeWebsite() {
		if(this.driver != null) this.driver.quit();
	}
	
	public void addScript() {
		if(!scriptActive) {
			driver.executeScript("var remove = false;\r\n" + SCRIPT);
			scriptActive = true;
		}
	}
	
	public void removeScript() {
		if(scriptActive) {
			driver.executeScript("var remove = true;\r\n" + SCRIPT);
			scriptActive = false;
		}
	}
	
    public boolean hasSiteOpen(String currentURL, String... list) {
    	if(currentURL.length() == 0) return true;
    	for(String sitePrefix : list) {
    		if(currentURL.startsWith(sitePrefix)) return true;
    	}
    	return false;
    }
	
}
