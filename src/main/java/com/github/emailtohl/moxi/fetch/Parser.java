package com.github.emailtohl.moxi.fetch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 抓数据
 * 
 * @author HeLei
 * @date 2017.03.09
 */
public class Parser implements NotificationBroadcaster {
	private static final long serialVersionUID = 2941852498029796582L;
	private String username = "zt";
	private String password = "4da64b5779c9d82140c450b33124ccc3";
	private String proxyHost;
	private String proxyPort;
	private Set<NotificationListener> listeners = new CopyOnWriteArraySet<>();

	public Connection getConnection(String url) {
		Connection conn = Jsoup.connect(url)
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.header("Accept-Encoding", "gzip, deflate, sdch").header("Accept-Language", "zh-CN,zh;q=0.8")
				.header("Host", "epub.cqvip.com").header("Proxy-Connection", "keep-alive")
				.header("Referer", "http://epub.cqvip.com/manage/_main_left.aspx")
				.header("Upgrade-Insecure-Requests", "1")
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
				.cookie("cookie_admin_username", username).cookie("cookie_admin_password", password);
		if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
			conn.proxy(proxyHost, Integer.valueOf(proxyPort));
		}
		return conn;
	}

	public void parse(File f) {
		try (PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)))) {
			Connection conn = getConnection("http://epub.cqvip.com/manage/Article/NumList.aspx");
			Document doc;
			try {
				doc = conn.get();
			} catch (IOException e) {
				publish(e.getMessage());
				return;
			}
			int total = getTotalPages(doc);
			for (int i = 1; i <= total; i++) {// 遍历期刊列表
				Connection perConn = getConnection("http://epub.cqvip.com/manage/Article/NumList.aspx?page=" + i);
				Document perDoc;
				try {
					perDoc = perConn.get();
				} catch (IOException e) {
					publish(e.getMessage());
					continue;
				}
				for (Element a : perDoc.select("a")) {
					String href = a.attr("href");
					if (href == null || !href.startsWith("ArticleList.aspx?"))
						continue;
					String articleUrl = "http://epub.cqvip.com/manage/Article/" + href;
					Connection sconn = getConnection(articleUrl);
					Document sdoc;
					try {
						sdoc = sconn.get();
					} catch (IOException e) {
						publish(e.getMessage());
						continue;
					}
					for (Element sa : sdoc.select("a")) {
						String shref = sa.attr("href");
						if (shref == null || !shref.startsWith("ArticlePost.aspx?"))
							continue;
						String detailUrl = "http://epub.cqvip.com/manage/Article/" + shref;
						Connection dconn = getConnection(detailUrl);
						Document ddoc;
						try {
							ddoc = dconn.get();
						} catch (IOException e) {
							publish(e.getMessage());
							continue;
						}
						String author = ddoc.getElementById("txtFirstWriter").val();
						String agent = ddoc.getElementById("txtFirstOrgan").val();
						String summary = ddoc.getElementById("txtRemark_c").val();
						out.println("作者：" + author);
						out.println("机构：" + agent);
//						out.println("摘要：" + summary);
						String result = new StringBuilder().append("作者：").append(author).append("\n").append("机构：")
								.append(agent).append("\n").append("摘要：").append(summary).toString();
						publish(result);
					}
				}
			}
			
		} catch (FileNotFoundException e1) {
			publish(e1.getMessage());
		} finally {
			end();
		}
	}
	
	private Pattern pagePattern = Pattern.compile("page=(\\d+)");
	private int getTotalPages(Document doc) {
		int total = 1;// 默认总页数为1页
		String lastHref = doc.select("#pagelist a").last().attr("href");
		if (lastHref != null) {
			Matcher m = pagePattern.matcher(lastHref);
			if (m.find()) {
				total = Integer.valueOf(m.group(1));
			}
		}
		return total;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	@Override
	public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
			throws IllegalArgumentException {
		this.listeners.add(listener);
	}

	@Override
	public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
		this.listeners.remove(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return null;
	}

	private void publish(String msg) {
		listeners.forEach(listener -> {
			Notification notification = new Notification("message", this, serialVersionUID, System.currentTimeMillis(),
					msg);
			listener.handleNotification(notification, null);
		});
	}

	private void end() {
		listeners.forEach(listener -> {
			Notification notification = new Notification("end", this, serialVersionUID, System.currentTimeMillis(),
					"执行结束");
			listener.handleNotification(notification, null);
		});
	}

}
