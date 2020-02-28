package com.cn.wavetop.dataone.util;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysFieldrule;
import com.cn.wavetop.dataone.entity.SysTablerule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class DBConns {
    private  final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取Mysql对象
     * @return
     */
    public static Connection getMySQLConn(SysDbinfo sysDbinfo) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String url="jdbc:mysql://"+sysDbinfo.getHost()+":"+sysDbinfo.getPort()+"/"+sysDbinfo.getDbname()+"?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai";
        Class.forName("com.mysql.jdbc.Driver");
        DriverManager.setLoginTimeout(10);
        return DriverManager.getConnection(url, sysDbinfo.getUser(), sysDbinfo.getPassword());
    }
    /**
     * 获取Oracle对象
     * @return
     */
    public static Connection getOracleConn(SysDbinfo sysDbinfo) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String url = "jdbc:oracle:thin:@"+sysDbinfo.getHost()+":"+sysDbinfo.getPort()+":"+sysDbinfo.getDbname();
        Class.forName("oracle.jdbc.driver.OracleDriver");
        DriverManager.setLoginTimeout(10);
        return  DriverManager.getConnection(url, sysDbinfo.getUser(), sysDbinfo.getPassword());

    }
    /**
     * 获取sqlserver对象
     * @return    jdbc:sqlserver://localhost:1433;DatabaseName=tjl
     */
    public static Connection getSqlserverConn(SysDbinfo sysDbinfo) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String url = "jdbc:sqlserver://"+sysDbinfo.getHost()+":"+sysDbinfo.getPort()+";DatabaseName="+sysDbinfo.getDbname();
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DriverManager.setLoginTimeout(10);
        return  DriverManager.getConnection(url, sysDbinfo.getUser(), sysDbinfo.getPassword());
    }

    /**DBConns
     * 获取达梦数据库对象
     */
    public static Connection getDaMengConn(SysDbinfo sysDbinfo) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String url = "jdbc:dm://"+sysDbinfo.getHost()+":"+sysDbinfo.getPort()+"/"+sysDbinfo.getDbname();
        Class.forName("dm.jdbc.driver.DmDriver");
        DriverManager.setLoginTimeout(10);
        return  DriverManager.getConnection(url, sysDbinfo.getUser(), sysDbinfo.getPassword());
    }


    /**
     * 释放资源
     *
     */
    public static void close(Statement stmt, Connection connection, ResultSet rs) {
        if (stmt != null){
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void close(Statement stmt, Connection connection) throws SQLException {
        close(stmt, connection, null);
    }
    public static void close( Connection connection) throws SQLException {
        close(null, connection, null);
    }


    public static  List getConn(SysDbinfo sysDbinfo, SysTablerule sysTablerule, String sql) {
        List<String> list = new ArrayList<String>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Set<String> set=new HashSet<>();
        if(sysTablerule.getSourceTable()!=null) {
            String[] b = sysTablerule.getSourceTable().split(",");
            for (int i = 0; i < b.length; i++) {
                set.add(b[i]);
            }
        }
        //ArrayList<Object> data = new ArrayList<>();

        if (sysDbinfo.getType() == 2) {
            try {
                conn = DBConns.getMySQLConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                String tableName = null;
                while (rs.next()) {
                    tableName = rs.getString(1);
                    list.add(tableName);
                }//显示数据
            } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                System.out.println("連接錯誤");
                return list;

            }finally {
                DBConns.close(stmt,conn,rs);
            }
        } else if (sysDbinfo.getType() == 1) {
            String tableName = "";
            try {
                conn = DBConns.getOracleConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    tableName = rs.getString(1);
                    list.add(tableName);
                }
            } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                System.out.println("連接錯誤");
                return list;
            }finally {
                DBConns.close(stmt,conn,rs);
            }
        } else {
            System.out.println("類型錯誤");
            return list;
        }

        Iterator<String>  iterator=list.iterator();
        while (iterator.hasNext()) {
            String num = iterator.next();
            if (set.contains(num)) {
                iterator.remove();

            }
        }
        return list;
    }

  public  static List<SysFieldrule> getResult(SysDbinfo sysDbinfo, String sql, String list_data){
      List<SysFieldrule> sysFieldruleList = new ArrayList<SysFieldrule>();
      List<SysFieldrule> stringList = new ArrayList<SysFieldrule>();
      SysFieldrule sysFieldrule=new SysFieldrule();
      Connection conn = null;
      Statement stmt = null;
      ResultSet rs = null;
      Set<SysFieldrule> set=new HashSet<SysFieldrule>();
      if(list_data!=null) {
          String[] splits = list_data.replace("$","@").split(",@,");
          for(String s:splits) {
              sysFieldrule=new SysFieldrule();
              String[] b = s.split(",");
              sysFieldrule.setFieldName(b[0]);
              sysFieldrule.setType(b[6]);
              if(!"".equals(b[7])) {
                  sysFieldrule.setScale(b[7]);
              }
              set.add(sysFieldrule);
          }
      }
      if (sysDbinfo.getType() == 2) {
          try {
              conn = DBConns.getMySQLConn(sysDbinfo);
              stmt = conn.prepareStatement(sql);
              rs = stmt.executeQuery(sql);
              while (rs.next()) {
                  sysFieldrule=new SysFieldrule();
                  sysFieldrule.setFieldName(rs.getString("ColumnName"));
                  sysFieldrule.setType(rs.getString("TypeName"));
                sysFieldrule.setScale(rs.getString("length"));
//                  sysFieldrule.setAccuracy(rs.getString("Scale"));
//                  sysFieldrule.setNotNull(rs.getLong("CanNull"));
                  stringList.add(sysFieldrule);
              }
          } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
              e.printStackTrace();
              return stringList;
          }finally {
              DBConns.close(stmt,conn,rs);
          }
      }else if(sysDbinfo.getType() == 1){
          try {
              conn = DBConns.getOracleConn(sysDbinfo);
              stmt = conn.prepareStatement(sql);
              rs = stmt.executeQuery(sql);
              while (rs.next()) {
                  sysFieldrule=new SysFieldrule();
                  sysFieldrule.setFieldName(rs.getString("COLUMN_NAME"));
                  sysFieldrule.setType(rs.getString("DATA_TYPE"));
                sysFieldrule.setScale(rs.getString("NVL(DATA_LENGTH,0)"));
                 // sysFieldrule.setAccuracy(rs.getString("NVL(DATA_SCALE,0)"));
                //  sysFieldrule.setNotNull(rs.getLong("NULLABLE"));
                  stringList.add(sysFieldrule);
                  //stringList.add(rs.getString("COLUMN_NAME"));
//                  stringList.add(rs.getString("DATA_TYPE"));
//                  stringList.add(rs.getString("NVL(DATA_LENGTH,0)"));
//                  stringList.add(rs.getString("NVL(DATA_PRECISION,0)"));
//                  stringList.add(rs.getString("NVL(DATA_SCALE,0)"));
//                  stringList.add(rs.getString("NULLABLE"));
//                  stringList.add(rs.getString("COLUMN_ID"));
//                  stringList.add(rs.getString("DATA_TYPE_OWNER"));
              }
          } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
              e.printStackTrace();
              return stringList;
          }finally {
              DBConns.close(stmt,conn,rs);
          }
      } else {
          System.out.println("類型錯誤");
          return stringList;
      }
      Iterator<SysFieldrule>  iterator=stringList.iterator();
      while (iterator.hasNext()) {
          SysFieldrule num = iterator.next();
          if (set.contains(num)) {
              iterator.remove();
          }
      }
        return stringList;
  }

    /**
     * 查询目的端表名是否存在表名
     * @param
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public static List<String> existsTableName(SysDbinfo sysDbinfo, String sql,String sourceName, String destName) {
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement ps=null;
        ResultSet rs = null;
        String tableName = null;
        List<String> list=new ArrayList<>();
        try {
            if (sysDbinfo.getType() == 1) {
                conn = DBConns.getOracleConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                while (rs.next()){
                    tableName = rs.getString(1);
                    if(tableName.equals(destName)){
                        if(sysDbinfo.getSourDest()==0){
                             if(!tableName.equals(sourceName)){
                                 list.add(tableName);
                             }
                        }else {
                            list.add(tableName);
                        }
                    }
                }
            } else if (sysDbinfo.getType() == 2) {
                conn = DBConns.getMySQLConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                while (rs.next()){
                    tableName = rs.getString(1);
                    if(tableName.equals(destName)){
                        if(sysDbinfo.getSourDest()==0){
                            if(!tableName.equals(sourceName)){
                                list.add(tableName);
                            }
                        }else {
                            list.add(tableName);
                        }
                    }
                }
            } else if (sysDbinfo.getType() == 3) {
                conn = DBConns.getSqlserverConn(sysDbinfo);
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()){
                    tableName = rs.getString(1);
                    if(sysDbinfo.getSourDest()==0){
                        if(!tableName.equals(sourceName)){
                            list.add(tableName);
                        }
                    }else {
                        list.add(tableName);
                    }
                }
            }else if (sysDbinfo.getType() == 4){
                conn = DBConns.getDaMengConn(sysDbinfo);
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()){
                    tableName = rs.getString(1);
                    if(tableName.equals(destName)){
                        list.add(tableName);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        } finally {
            if(ps!=null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DBConns.close(stmt, conn, rs);
        }
        return list;
    }

    public static void main(String[] args) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
//        SysDbinfo mysql = SysDbinfo.builder().host("192.168.1.226").port(Long.valueOf(3306)).dbname("dataone").user("root").password("888888").build();
//        Connection mySQLConn = getMySQLConn(mysql);
//        System.out.println(mySQLConn);
        SysDbinfo oracle = SysDbinfo.builder().host("192.168.103.238").port(Long.valueOf(1521)).dbname("ORCL").user("test").password("test").build();
        Connection oracleConn = getOracleConn(oracle);
        System.out.println(oracleConn);
        SysDbinfo sqlserver = SysDbinfo.builder().host("192.168.10.176").port(Long.valueOf(1433)).dbname("TEST1").user("sa").password("wavetop_888888").build();
        Connection sqlserverConn = getSqlserverConn(sqlserver);
        System.out.println(sqlserverConn);

    }
}
