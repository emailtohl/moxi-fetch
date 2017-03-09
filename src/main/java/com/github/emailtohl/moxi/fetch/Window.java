package com.github.emailtohl.moxi.fetch;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.time.LocalDate;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
/**
 * main启动
 * @author HeLei
 * @date 2017.03.09
 */
public class Window implements NotificationListener {
	private JFrame frame;
	private JTextField outDirField;
	private JTextField usernameField;
	private JTextField passwordField;
	private File outDir;
	private File outFile;
	private JTextArea textArea;
	private JLabel label_3;
	private JLabel label_4;
	private JTextField proxyHostField;
	private JTextField proxyPortField;
	private JButton execBtn;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window() {
		outDir = new File("D:\\数据输出路径");
		if (!outDir.exists() || !outDir.isDirectory()) {
			outDir.mkdirs();
		}
		outFile = new File(outDir, "articleSummary-" + LocalDate.now().toString() + ".txt");
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("数据下载");
		frame.setBounds(100, 100, 514, 434);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		
		JLabel label = new JLabel("数据输出目录");
		label.setBounds(34, 29, 127, 15);
		frame.getContentPane().add(label);
		
		JButton button = new JButton("浏览输出目录");
		button.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser("/");// 从本程序的根目录开始浏览
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setFileFilter(new FileFilter() {// 过滤文件，在文件选择框中只显示目录和文件
				@Override
				public boolean accept(File file) {
					if (file.isDirectory())
						return true;
					else
						return false;
				}
				@Override
				public String getDescription() {
					return "文件夹";
				}
			});
			fileChooser.setDialogTitle("选择文件存放目录");
			
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {// APPROVE_OPTION是一个常量，值为0，代表已经打开的文件，如果在对话框点取消则result = 1
				outDir = fileChooser.getSelectedFile();
				outDirField.setText(outDir.getAbsolutePath());
			}
		});
		button.setBounds(344, 25, 127, 23);
		frame.getContentPane().add(button);
		
		outDirField = new JTextField();
		outDirField.setBounds(34, 58, 437, 21);
		frame.getContentPane().add(outDirField);
		outDirField.setColumns(10);
		outDirField.setText(outDir.getAbsolutePath());
		
		usernameField = new JTextField();
		usernameField.setBounds(34, 131, 267, 21);
		frame.getContentPane().add(usernameField);
		usernameField.setColumns(10);
		usernameField.setText("zt");
		
		passwordField = new JTextField();
		passwordField.setBounds(34, 201, 267, 21);
		frame.getContentPane().add(passwordField);
		passwordField.setColumns(10);
		passwordField.setText("");
		
		execBtn = new JButton("执行");
		execBtn.addActionListener(e -> {
			execBtn.setEnabled(false);
			new Thread(() -> {
				Parser parser = new Parser();
				parser.addNotificationListener(this, null, null);
				parser.setUsername(usernameField.getText());
				parser.setPassword(passwordField.getText());
				parser.setProxyHost(proxyHostField.getText());
				parser.setProxyPort(proxyPortField.getText());
				parser.parse(outFile);
			}).start();
		});
		execBtn.setBounds(344, 241, 127, 23);
		frame.getContentPane().add(execBtn);
		
		JLabel label_1 = new JLabel("用户名");
		label_1.setBounds(34, 106, 127, 15);
		frame.getContentPane().add(label_1);
		
		JLabel label_2 = new JLabel("密码");
		label_2.setBounds(34, 176, 127, 15);
		frame.getContentPane().add(label_2);
		
		textArea = new JTextArea();
		textArea.setBounds(34, 274, 437, 115);
		textArea.setEditable(false);
		textArea.setSelectedTextColor(Color.RED);
		textArea.setLineWrap(true); //激活自动换行功能 
		textArea.setWrapStyleWord(true); // 激活断行不断字功能
		frame.getContentPane().add(textArea);
		
		label_3 = new JLabel("代理host");
		label_3.setBounds(344, 106, 127, 15);
		frame.getContentPane().add(label_3);
		
		label_4 = new JLabel("代理port");
		label_4.setBounds(344, 176, 127, 15);
		frame.getContentPane().add(label_4);
		
		proxyHostField = new JTextField();
		proxyHostField.setText("zt");
		proxyHostField.setColumns(10);
		proxyHostField.setBounds(344, 131, 127, 21);
		proxyHostField.setText("");
		frame.getContentPane().add(proxyHostField);
		
		proxyPortField = new JTextField();
		proxyPortField.setText("");
		proxyPortField.setColumns(10);
		proxyPortField.setBounds(344, 201, 127, 21);
		proxyPortField.setText("");
		frame.getContentPane().add(proxyPortField);
	}
	
	@Override
	public void handleNotification(Notification notification, Object handback) {
		EventQueue.invokeLater(() -> {
			textArea.setText(notification.getMessage());
			if ("end".equals(notification.getType()))
				execBtn.setEnabled(true);
		});
	}
	
}
