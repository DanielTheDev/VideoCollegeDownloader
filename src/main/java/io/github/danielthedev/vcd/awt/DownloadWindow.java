package io.github.danielthedev.vcd.awt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import com.formdev.flatlaf.FlatLightLaf;

import io.github.danielthedev.vcd.Video;
import io.github.danielthedev.vcd.browser.Browser;
import io.github.danielthedev.vcd.Manifest.VideoInfo;

@SuppressWarnings("serial")
public class DownloadWindow extends JFrame {

	public static final int LOG_AUDIO = 0;
	public static final int LOG_VIDEO = 1;
	public static final int LOG_PRESENTATION = 2;
	public static final int LOG_EDITOR = 3;
	
	
	private final JPanel bottomPanel = new JPanel();
	private final JPanel mainPanel = new JPanel();
	private final JPanel topPanel = new JPanel();
	private final JPanel downloadOptionsPanel = new JPanel();
	private final JPanel qualityListPanel = new JPanel();
	
	private final JTextPane[] textPanes = new JTextPane[4];
	private final int[] logLines = new int[4];
	
	private final JButton downloadButton = new JButton();
	private final JPanel videoDetailsPanel = new JPanel();
	private final JLabel videoNameLabel = new JLabel();
	private final JLabel videoIDLabel = new JLabel();
	private final JLabel channelIDLabel = new JLabel();
	private final JLabel departmentIDLabel = new JLabel();
	private final JPanel spacePanel = new JPanel();
	
	private JRadioButton[] radioButtonList;
	private Map<String, Map.Entry<VideoInfo, VideoInfo>> downloadQualities;
	
	static {
    	try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

    public DownloadWindow() {
    	super("VideoCollege Downloader");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            	synchronized(Browser.LOCK) {
            		Browser.LOCK.notify();
        		}
            }
        });
        this.setBackground(new Color(255, 255, 255));
        this.setMinimumSize(new Dimension(1280, 720));
        this.setPreferredSize(new Dimension(1280, 720));
        this.setLocationRelativeTo(null);

        this.requestFocus();
        this.addComponents();
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pack();
    }
    
    public void loadVideo(Video video, Consumer<Entry<VideoInfo, VideoInfo>> callback) {
    	this.channelIDLabel.setText("ChannelID: " + video.getChannelID());
    	this.departmentIDLabel.setText("DepartmentID: " + video.getDepartmentID());
    	this.videoIDLabel.setText("VideoID: " + video.getVideoID());
    	this.videoNameLabel.setText("Name: " + video.getTitle());
    	this.downloadQualities = video.getPlayerOptions().getQualities();
    	this.radioButtonList = this.createQualityList(new ArrayList<String>(this.downloadQualities.keySet()));
    	Arrays.sort(this.radioButtonList, (a,b)->Integer.compare(Integer.parseInt(b.getText().split("x")[0]), Integer.parseInt(a.getText().split("x")[0])));
    	this.radioButtonList[0].setSelected(true);
    	for(JRadioButton btn : this.radioButtonList) qualityListPanel.add(btn);
    	downloadButton.setEnabled(true);
        downloadButton.addActionListener((e)->{
        	this.downloadButton.setEnabled(false);
        	
        	for(JRadioButton btn : this.radioButtonList) {
        		if(btn.isSelected()) { 
        			callback.accept(this.downloadQualities.get(btn.getText()));
        			break;
        		}
        	}
        });

    	this.revalidate();
    	this.repaint();
    }
         
    private void addComponents() {
    	mainPanel.setBackground(new Color(255, 255, 255));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        topPanel.setBackground(new Color(255, 255, 255));
        topPanel.setMaximumSize(new Dimension(10000, 10000));
        topPanel.setMinimumSize(new Dimension(135, 100));
        topPanel.setPreferredSize(new Dimension(900, 100));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        downloadOptionsPanel.setBackground(new Color(255, 255, 255));
        downloadOptionsPanel.setBorder(this.createTitleBorder("Download Options", 5));
        downloadOptionsPanel.setMaximumSize(new Dimension(300, 10000));
        downloadOptionsPanel.setMinimumSize(new Dimension(300, 200));
        downloadOptionsPanel.setPreferredSize(new Dimension(300, 200));
        downloadOptionsPanel.setLayout(new BoxLayout(downloadOptionsPanel, BoxLayout.Y_AXIS));

        qualityListPanel.setBackground(new Color(255, 255, 255));
        qualityListPanel.setMaximumSize(new Dimension(10000, 100000));
        qualityListPanel.setMinimumSize(new Dimension(200, 100));
        qualityListPanel.setLayout(new BoxLayout(qualityListPanel, BoxLayout.Y_AXIS));
        

        downloadOptionsPanel.add(qualityListPanel);

        downloadButton.setText("Download");
        downloadButton.setHorizontalAlignment(SwingConstants.LEFT);
        downloadButton.setHorizontalTextPosition(SwingConstants.LEFT);
        downloadButton.setVerticalAlignment(SwingConstants.BOTTOM);
        downloadButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        downloadButton.setEnabled(false);

        downloadOptionsPanel.add(downloadButton);

        topPanel.add(downloadOptionsPanel);

        videoDetailsPanel.setBackground(new Color(255, 255, 255));
        videoDetailsPanel.setBorder(this.createTitleBorder("Video details", 5));
        videoDetailsPanel.setMaximumSize(new Dimension(500, 10000));
        videoDetailsPanel.setMinimumSize(new Dimension(500, 200));
        videoDetailsPanel.setPreferredSize(new Dimension(500, 200));
        videoDetailsPanel.setLayout(new BoxLayout(videoDetailsPanel, BoxLayout.Y_AXIS));

        videoNameLabel.setText("Name: loading...");
        videoDetailsPanel.add(videoNameLabel);

        videoIDLabel.setText("VideoID: loading...");
        videoDetailsPanel.add(videoIDLabel);

        channelIDLabel.setText("ChannelID: loading...");
        videoDetailsPanel.add(channelIDLabel);

        departmentIDLabel.setText("DepartmentID: loading...");
        videoDetailsPanel.add(departmentIDLabel);

        topPanel.add(videoDetailsPanel);

        mainPanel.add(topPanel);

        spacePanel.setBackground(new Color(255, 255, 255));
        spacePanel.setMaximumSize(new Dimension(20, 20));
        spacePanel.setMinimumSize(new Dimension(20, 20));
        spacePanel.setPreferredSize(new Dimension(20, 20));

        GroupLayout spacePanelLayout = new GroupLayout(spacePanel);
        spacePanel.setLayout(spacePanelLayout);
        spacePanelLayout.setHorizontalGroup(
            spacePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        spacePanelLayout.setVerticalGroup(
            spacePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        mainPanel.add(spacePanel);
        bottomPanel.setBackground(new Color(255, 255, 255));
        bottomPanel.setPreferredSize(new Dimension(779, 200));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        bottomPanel.add(createLogPanel("Video Log", 50, LOG_VIDEO));
        bottomPanel.add(createLogPanel("Presentation Log", 50, LOG_PRESENTATION));
        bottomPanel.add(createLogPanel("Audio Log", 50, LOG_AUDIO));
        bottomPanel.add(createLogPanel("Editor Log", 500, LOG_EDITOR));
        mainPanel.add(bottomPanel);
    }
    
    public void loadResult(File location) {
    	Toolkit.getDefaultToolkit().beep();
    	JOptionPane.showMessageDialog(this, "Finished downloading: " + location.getName(), "VideoCollege Downloader", JOptionPane.INFORMATION_MESSAGE);
    	this.dispose();
    	synchronized(Browser.LOCK) {
    		Browser.LOCK.notify();
		}
    }
    
    public void printLog(int log, String text) {
		SwingUtilities.invokeLater(()->{
			JTextPane pane = DownloadWindow.this.textPanes[log];
			if(text.equals("clear")) {
				pane.setText("");
				return;
			}
			if(pane.getText().length() == 0) {
				pane.setText(text);
			} else {
				pane.setText(pane.getText() + "\r\n" + text );
			}
			logLines[log]++;
			if(logLines[log] > 60) {
				int lineCount = 0;
				String buffer = pane.getText();
				for(int x = 0; x < buffer.length(); x++) {
					if(buffer.charAt(x) == '\r' && buffer.charAt(x + 1) == '\n') {
						lineCount++;
						if(lineCount == 11) {				
							pane.setText(buffer.substring(x+2));
							break;
						}
					}
				}
				logLines[log] = 50;
			}
		});
    }
    
    private JRadioButton[] createQualityList(List<String> set) {
    	ButtonGroup buttonGroup = new ButtonGroup();
    	JRadioButton[] buttons = new JRadioButton[set.size()];
    	for(int x = 0; x < buttons.length; x++) {
    		JRadioButton radioButton = new JRadioButton(set.get(x));
            radioButton.setBackground(new Color(255, 255, 255));
            buttonGroup.add(radioButton);
            radioButton.setHorizontalAlignment(SwingConstants.LEFT);
            radioButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            buttons[x] = radioButton;
    	}
		return buttons;
    }
    
    private JScrollPane createLogPanel(String title, int width, int textPaneID) {
    	JTextPane textPane = new JTextPane();
    	this.textPanes[textPaneID] = textPane;
    	JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setBackground(new Color(255, 255, 255));
        
        scrollPanel.setBorder(this.createTitleBorder(title, 1));
        scrollPanel.setMinimumSize(new Dimension(width, 100));
        scrollPanel.setPreferredSize(new Dimension(width, 100));
        scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPanel.getVerticalScrollBar().setUnitIncrement(5);
        textPane.setFont(new Font("Consolas", Font.PLAIN, 11));
        textPane.setBackground(new Color(0, 0, 0));
        textPane.setForeground(new Color(0, 200, 0));
        textPane.setMinimumSize(new Dimension(width, 100));
        textPane.setPreferredSize(new Dimension(width, 100));
        textPane.setEditable(false);
        scrollPanel.setViewportView(textPane);
        return scrollPanel;
    }
    
    private Border createTitleBorder(String title, int margin) {
    	return BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(margin, margin, margin ,margin), BorderFactory.createTitledBorder(title));
    }

	public void open() {
		this.setAlwaysOnTop(true);
		this.setVisible(true);
		this.requestFocus();
		this.setAlwaysOnTop(false);
	}
                 
}
