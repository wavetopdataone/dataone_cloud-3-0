package com.cn.wavetop.dataone.db;

import com.cn.wavetop.dataone.util.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DBUtil {
    private static int PAGESIZE = 10;
    private static final Log log = LogFactory.getLog(DBUtil.class);

//        public static void update(String sql) throws Exception {
//            Connection con = DBConn.getInstance().getConnection();
//            Statement st = null;
//            try {
//                con.setAutoCommit(false);
//                st = con.createStatement();
//                log.info(sql);
//                st.executeUpdate(sql);
//                con.commit();
//                st.close();
//            } catch (Exception e) {
//                try {
//                    con.rollback();
//                } catch (Exception ex) {
//                }
//                log.error("exception: ", e);
//                // Log.log(e.getMessage());
//            }
//            DBConn.getInstance().closeConnection(con);
//        }

        public static void update(String sql, Connection con) throws Exception {
            Statement st = null;
            st = con.createStatement();
            log.info(sql);
            st.executeUpdate(sql);
            st.close();


        }

    public static String[][] query(String sql, Connection con) throws Exception {
        return query(sql, con, 0);
    }

//        public static String[][] query(String sql) throws Exception {
//            Connection con = DBConn.getInstance().getConnection();
//            String[][] arr = new String[0][0];
//            try {
//                arr = query(sql, con, 0);
//            } catch (Exception e) {
//                log.error("exception: ", e);
//                // Log.log(e.getMessage());
//            } finally {
//                try {
//                    DBConn.getInstance().closeConnection(con);
//                } catch (Exception e) {
//                }
//            }
//
//            return arr;
//        }

//        public static String[][] query(String sql, int dateformat) throws Exception {
//            Connection con = DBConn.getInstance().getConnection();
//            String[][] arr = new String[0][0];
//            try {
//                arr = query(sql, con, dateformat);
//            } catch (Exception e) {
//                log.error("exception: ", e);
//                // Log.log(e.getMessage());
//            } finally {
//                try {
//                    DBConn.getInstance().closeConnection(con);
//                } catch (Exception e) {
//                }
//            }
//
//            return arr;
//        }

    /**
     *
     * @param sql
     * @param con
     * @param dateformat
     *            0����yyyy-MM-dd 1����yyyy-MM-dd hh:mm 2����yyyy-MM-dd hh:mm:ss
     * @return
     * @throws Exception
     */
    public static String[][] query(String sql, Connection con, int dateformat)
            throws Exception {
        Statement st = null;
        ResultSet rs = null;
        String[][] arr = new String[0][0];
        try {
            // st =
            // con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            log.info(sql);
            rs = st.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            rs.last();
            int rows = rs.getRow();
            int columns = meta.getColumnCount();

            // System.out.println("columns = "+columns);

            if (rows <= 0) {
                return new String[0][0];
            }
            arr = new String[rows][columns];
            rs.beforeFirst();
            int row = 0;
            while (rs.next()) {
                for (int i = 0; i < columns; i++) {

                    // System.out.println(rs.getString(i + 1));
                    if (meta.getColumnType(i + 1) == Types.DATE) {

                        arr[row][i] = DateUtils.format(rs.getTimestamp(i + 1));

                        if (dateformat == 0)
                            arr[row][i] = DateUtils.format(rs
                                    .getTimestamp(i + 1));
                        else if (dateformat == 1)
                            arr[row][i] = DateUtils.formatTime(rs
                                    .getTimestamp(i + 1));
                        else if (dateformat == 2) {
                            arr[row][i] = DateUtils.formatTime2(rs
                                    .getTimestamp(i + 1));
                        }
                    } else
                        arr[row][i] = rs.getString(i + 1);
                    if (arr[row][i] == null)
                        arr[row][i] = "";
                    else
                        arr[row][i] = arr[row][i].trim();
                }
                row++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log.log(e.getMessage());
        } finally {
            try {
                rs.close();
            } catch (Exception e) {
            }
            try {
                st.close();
            } catch (Exception e) {
            }
        }

        return arr;
    }

//        public static ResultMap query2(String sql) throws Exception {
//
//            Connection con = DBConn.getInstance().getConnection();
//            ResultMap rm = null;
//            try {
//                rm = query2(sql, con, 0);
//            } catch (SQLException e) {
//                log.error("exception:", e);
//
//            } finally {
//                try {
//                    DBConn.getInstance().closeConnection(con);
//                } catch (Exception e) {
//                }
//            }
//
//            return rm;
//        }

//        public static ResultMap query2(String sql, int dateformat) throws Exception {
//            Connection con = DBConn.getInstance().getConnection();
//            ResultMap rm = null;
//            try {
//                rm = query2(sql, con, dateformat);
//            } catch (Exception e) {
//                log.error("exception:", e);
//
//            } finally {
//                try {
//                    DBConn.getInstance().closeConnection(con);
//                } catch (Exception e) {
//                }
//            }
//
//            return rm;
//        }

    public static ResultMap query2(String sql, Connection con) throws Exception {

        ResultMap rm = null;
        try {
            rm = query2(sql, con, 0);
        } catch (Exception e) {
            log.error("exception:", e);

        }

        return rm;
    }

    public static ResultMap query2(String sql, Connection con, int dateformat)
            throws Exception {
        log.info(sql);
        Statement st = null;
        ResultSet rs = null;
        ResultMap rm = new ResultMap();
        try {
            // st =
            // con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            rs.last();
            int rows = rs.getRow();
            int columns = meta.getColumnCount();
            if (rows <= 0) {
                st.close();
                return rm;
            }

            rs.beforeFirst();
            int row = 0;
            while (rs.next()) {
                Map map = new HashMap();

                for (int i = 0; i < columns; i++) {
                    if (((meta.getColumnType(i + 1) == Types.DATE) || (meta.getColumnType(i + 1) == Types.TIMESTAMP))
                            && rs.getTimestamp(i + 1) != null) {
                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "yyyy-MM-dd hh:mm");

                        map.put(meta.getColumnLabel(i + 1).toUpperCase(),
                                formatter.format(rs.getTimestamp(i + 1)));

                        if (dateformat == 0)
                            map.put(meta.getColumnLabel(i + 1).toUpperCase(),
                                    DateUtils.format(rs.getTimestamp(i + 1)));
                        else if (dateformat == 1)
                            map.put(meta.getColumnLabel(i + 1)
                                            .toUpperCase(), DateUtils
                                            .formatTime(rs.getTimestamp(i + 1)));
                        else if (dateformat == 2) {
                            map.put(meta.getColumnLabel(i + 1)
                                                    .toUpperCase(),
                                            DateUtils.formatTime2(rs
                                                    .getTimestamp(i + 1)));
                        }

                    } else {
                        String s = rs.getString(i + 1);
                        if (s == null)
                            map.put(meta.getColumnLabel(i + 1).toUpperCase(),
                                    "");
                        else
                            map.put(meta.getColumnLabel(i + 1).toUpperCase(), s
                                    .trim());

                    }
                }
                rm.addResult(map);
                row++;
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                rs.close();
            } catch (Exception e) {
            }
            try {
                st.close();
            } catch (Exception e) {
            }
        }

        return rm;
    }

//        public static MyResultSet queryPG(String sql, int page, int pagesize)
//                throws Exception {
//            return queryPG(sql, page, pagesize, 0);
//        }

//        public static MyResultSet queryPG(String sql, int page, int pagesize,
//                                          int dateformat) throws Exception {
//            Connection con = DBConn.getInstance().getConnection();
//            MyResultSet myresults = queryPG(sql,page,pagesize,dateformat,con);
//            DBConn.getInstance().closeConnection(con);
//
//
//            return myresults;
//        }

    public static MyResultSet queryPG(String sql, int page, int pagesize,
                                      int dateformat, Connection con) throws Exception {


        Statement st = null;
        ResultSet rs = null;
        String[][] arr = null;
        MyResultSet myresults = new MyResultSet();
        try {
            int rows = Integer.parseInt(DBUtil.query(getCountSql(sql),con)[0][0]);
            // st =
            // con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            log.info(sql);
            rs = st.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            //rs.last();
            //int rows = rs.getRow();
            int columns = meta.getColumnCount();
            myresults.totalPages = (rows + pagesize - 1) / pagesize;
            if (page > myresults.totalPages)
                page = 1;
            myresults.currentPage = page;
            myresults.totalRows = rows == -1 ? 0 : rows;

            if (rows <= 0) {
                myresults.results = new String[0][0];
                return myresults;
            }
            int beginRow = (page - 1) * pagesize + 1;
            int endRow = page * pagesize;
            if (rows < beginRow) {
                myresults.results = new String[0][0];
                return myresults;
            }
            if (rows < endRow) {
                myresults.results = new String[rows - beginRow + 1][columns];
            } else {
                myresults.results = new String[pagesize][columns];
            }

            rs.beforeFirst();
            // if(rs.next()){
            // rs.relative(beginRow-1);
            // }
            for (int j = 0; j < beginRow && rs.next(); j++) {

            }

            for (int row = 0; row < myresults.results.length; row++) {
                for (int i = 0; i < columns; i++) {
                    // myresults.results[row][i] = rs.getString(i + 1);
                    if (rs.getString(i + 1) == null) {
                        myresults.results[row][i] = "";
                    } else if (meta.getColumnType(i + 1) == Types.DATE) {
                        if (dateformat == 0)
                            myresults.results[row][i] = DateUtils.format(rs
                                    .getTimestamp(i + 1));
                        else if (dateformat == 1)
                            myresults.results[row][i] = DateUtils.formatTime(rs
                                    .getTimestamp(i + 1));
                        else if (dateformat == 2) {
                            myresults.results[row][i] = DateUtils.formatTime2(rs
                                    .getTimestamp(i + 1));
                        }
                    } else {
                        myresults.results[row][i] = rs.getString(i + 1);
                        if (myresults.results[row][i] == null)
                            myresults.results[row][i] = "";
                        else
                            myresults.results[row][i] = myresults.results[row][i]
                                    .trim();
                    }
                }
                // row++;
                rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Log.log(e.getMessage());
        } finally {
            try {
                rs.close();
            } catch (Exception e) {
            }
            try {
                st.close();
            } catch (Exception e) {
            }

        }

        return myresults;
    }

//        public static MyResultSet queryPG(String sql, int page) throws Exception {
//            return queryPG(sql, page, PAGESIZE);
//        }

//        public static void main(String[] args) {
//            try {
//                MyResultSet rs = com.cn.wavetop.dataone.db.DBUtil.queryPG("select id,title,normalcontent,to_char(createdate,'yyyy-MM-dd HH24:mi:ss'),infotype,infokey,infofrom from eq_myarticle where typelist=1",5,20);
//
//
//                System.out.println(rs.totalRows);
//                // Connection con = DBConn.getInstance().getConnection();
//                // Statement st = con.createStatement();
//                // ResultSet rs =
//                // st.executeQuery("select * from view_TeacherBaseInfo");
//                // while(rs.next())
//                // System.err.println(rs.getString(3));
//                //System.currentTimeMillis()
//
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }

    public static String getCountSql(String sql){
        String sql2 = sql.toLowerCase();
        int begin = 0,end = 0;
        String head = "";
        begin = sql2.indexOf("select ") + 7;
        head = sql.substring(0,begin) + " count(*) ";

        begin = sql2.indexOf(" from ");
        head = head + sql.substring(begin);

        return head;

    }
}
