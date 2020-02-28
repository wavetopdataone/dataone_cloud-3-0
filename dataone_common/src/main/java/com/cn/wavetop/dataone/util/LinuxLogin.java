package com.cn.wavetop.dataone.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Vector;

@Component
public class LinuxLogin {

	public static Connection conn = null;
	public static Connection login(String ip){
		//创建远程连接，默认连接端口为22，如果不使用默认，可以使用方法
		//new Connection(ip, port)创建对象
		Connection conn = new Connection(ip);
		try {
			//连接远程服务器
			conn.connect();
			//使用用户名和密码登录
			conn.authenticateWithPassword("root", "wavetop_888888");

		} catch (IOException e) {
			System.err.printf("用户%s密码%s登录服务器%s失败！", "root", "wavetop_888888", "192.168.1.156");
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 复制到JAVA所在服务器
	 * @param conn
	 * @param fileName
	 * @param localPath
	 */
	public static void copyFile(Connection conn, String fileName, String localPath){
		SCPClient sc = new SCPClient(conn);
		try {
			sc.get(fileName, localPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 流式输出，用于浏览器下载
	 * @param conn
	 * @param fileName
	 * @param outputStream
	 */
	public static void copyFile(Connection conn, String fileName, ServletOutputStream outputStream) throws IOException {
		SCPClient sc = new SCPClient(conn);
			sc.get(fileName, outputStream);
	}

	public static void copyFile(Connection conn, String fileName, PrintWriter out1) throws IOException {
		SCPClientUtil sc = new SCPClientUtil(conn);
		sc.get(fileName, out1);
	}

	/**
	 * 在远程LINUX服务器上，在指定目录下，获取文件各个属性
	 * @param[in] conn Conncetion对象
	 * @param[in] remotePath 远程主机的指定目录
	 */
	public void getFileProperties(Connection conn, String remotePath){
		try {
			SFTPv3Client sft = new SFTPv3Client(conn);

			Vector<?> v = sft.ls(remotePath);

			for(int i=0;i<v.size();i++){
				SFTPv3DirectoryEntry s = new SFTPv3DirectoryEntry();
				s = (SFTPv3DirectoryEntry) v.get(i);
				//文件名
				String filename = s.filename;
				//文件的大小
				Long fileSize = s.attributes.size;
			}

			sft.close();

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}


	public static void main(String args[]){
		URL resource = LinuxLogin.class.getClassLoader().getResource("dataoneinfo-2019-12-26.0.log");


		Connection conn = login("192.168.1.156");
		copyFile( conn, "/opt/kafka/connect-logs/kafka-connect.log.2019-12-01","F:");
	}

}
