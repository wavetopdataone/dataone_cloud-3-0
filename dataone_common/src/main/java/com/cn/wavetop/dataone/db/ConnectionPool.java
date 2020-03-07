package com.cn.wavetop.dataone.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class ConnectionPool {
    private final Log log = LogFactory.getLog(getClass());

    private String _driverName = null;// "com.inet.tds.TdsDriver";

    private String _username = null; // "sa";

    private String _password = null;// "";

    // private String _password = "consultation2000";
    private String _url = null;// "jdbc:inetdae:192.168.7.188:1433";

    // private String _url = "jdbc:inetdae:203.207.144.100:1433";
    private Properties _pt = new Properties();

    private static ConnectionPool _instance = null;

    private ArrayList _list = new ArrayList();

    int _nCount = 0;

    private ConnectionPool() {
        try {

            // File f = new File("c:\\sjw_config\\sjw.properties");
            InputStream in = ConnectionPool.class
                    .getResourceAsStream("db.properties");
            // System.err.println(f.getAbsolutePath());
            // PrintStream ps = new PrintStream(new
            // FileOutputStream("c:\\a.txt"));
            // ps.println(f.getAbsolutePath());
            // ps.close();
            // InputStream in = new FileInputStream(f);
            Properties pro = new Properties();
            pro.load(in);
            _driverName = pro.getProperty("driverName");
            _username = pro.getProperty("username");
            _password = pro.getProperty("password");
            _url = pro.getProperty("url");
            String _database = pro.getProperty("database");
            String _charset = pro.getProperty("charset");

            // _pt.setProperty("database", "webconsultation");
            if (_database != null)
                _pt.setProperty("database", _database);
            if (_charset != null)
                _pt.setProperty("charset", _charset);
            _pt.setProperty("user", _username);
            _pt.setProperty("password", _password);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public static ConnectionPool getInstance() {
        if (_instance == null)
            _instance = new ConnectionPool();
        return _instance;
    }

    public void add(Connection con) {
        _list.add(con);
    }

    public Connection getConnection() throws Exception {
        return getConnection(30000);
        //return newConnection();
    }

    public void closeConnection(Connection con){
        _list.add(con);
    }

    // 962777
    private synchronized Connection getConnection(long time)
            throws SQLException {
        // System.err.println("getConnection()");
        if (time <= 0)
            throw new SQLException("�ȴ���ʱ");
        try {
            if (_list.size() > 0) {
                Connection con = (Connection) _list.remove(0);
                return con;
            } else {

                Connection con = newConnection();
                return con;

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(e.getMessage());
        }
    }

    private synchronized Connection getConn() throws SQLException {
        // System.err.println("getConn()" + System.currentTimeMillis());
        if (_list.size() > 0) {
            Connection con = (Connection) _list.remove(0);
            return con;
        }
        throw new SQLException("�ȴ���ʱ.");

    }

    private Connection newConnection() throws Exception {
        // System.err.println("newConnection()");
        log.info("Create a new Connection _url=" + _url + "  ;  _pt=" + _pt);
        Driver driver = (Driver) Class.forName(_driverName).newInstance();
        Connection con = DriverManager.getConnection(_url, _pt);
        //con = new WrapConnection(con, this);
        _nCount++;
        // System.err.println("������һ���µ�����,��ǰ������:" + _nCount);
        return con;
    }

    public int size() {
        return _list.size();
    }
}
