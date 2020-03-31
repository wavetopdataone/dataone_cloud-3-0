package com.cn.wavetop.dataone.oracleLogminer;

import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.util.DBConns;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import io.swagger.models.auth.In;

import java.util.Date;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncTask {
    public static Connection getOracleConn() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String url = "jdbc:oracle:thin:@192.168.103.238:1521:orcl";
//                String url = "jdbc:oracle:thin:@192.168.1.25:1521:orcl";

        Class.forName("oracle.jdbc.driver.OracleDriver");

//        DriverManager.setLoginTimeout(10);
        // System.out.println("22211111111");
        return  DriverManager.getConnection(url, "test2", "test2");

    }
    /**

     *该方法是获取oracle日志地址,封装到一个list中

     */




    /**
     * 生成執行的sql文件
     */
    private static List<String> doLogAddress(Connection conn)  {
//        SysDbinfo sysDbinfo=findSourcesDbinfoById(jobId);
        //数据库连接

        String sql=" select group#, member from v$logfile order by group#";
        ResultMap resultMap=null;
        List<String> logList=new ArrayList<>();
        try {
            resultMap=DBUtil.query2(sql,conn);
            for(int i=0;i<resultMap.size();i++){
                logList.add(resultMap.get(i,"member").replaceAll("\\\\","\\\\\\\\"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logList;
    }



    /**
     * <p>方法名称: createDictionary|描述: 调用logminer生成数据字典文件</p>
     * @param sourceConn 源数据库连接
     * @throws Exception 异常信息
     */
    public static void createDictionary(Connection sourceConn) throws Exception{
        String createDictSql = "BEGIN dbms_logmnr_d.build(dictionary_filename => 'dictionary.ora', dictionary_location =>'"+Constants.DATA_DICTIONARY+"'); END;";
        CallableStatement callableStatement = sourceConn.prepareCall(createDictSql);
        callableStatement.execute();
        // System.out.println("生成字典成功");
    }

    /**
     * <p>方法名称: startLogmur|描述:启动logminer分析 </p>
     * @throws Exception
     */
    public static void startLogmur(Integer fangshi,String date,Integer index) throws Exception {

        CallableStatement callableStatement=null;
        SysDbinfo sysDbinfo=SysDbinfo.builder().dbname("ORCL").host("192.168.103.238").user("test2").password("test2").port(1521L).type(1l).schema("TEST").build();
        Connection conn=null;
        try {
            conn= DBConns.getConn(sysDbinfo);
          List<String> logList=  doLogAddress(conn);
            StringBuffer sbSQL = new StringBuffer();
            sbSQL.append(" BEGIN ");
            sbSQL.append("dbms_logmnr.add_logfile(logfilename=>'"+logList.get(0)+"',options=>dbms_logmnr.NEW);");
            for (int i = 1; i < logList.size(); i++) {
                if (logList.get(i).length() > 0 && !"".equals(logList.get(i))) {
              sbSQL.append("dbms_logmnr.add_logfile(logfilename=>'"+logList.get(i) +"',options=>dbms_logmnr.addfile);");
                }
            }
            sbSQL.append(" END;");
            // System.out.println(sbSQL);
            executeCallable(sbSQL.toString(),callableStatement,conn);

//             callableStatement = conn.prepareCall(sbSQL + "");
//            callableStatement.execute();

// 打印获分析日志文件信息
//            ResultMap resultMap = DBUtil.query2("SELECT db_name, thread_sqn, filename FROM v$logmnr_logs",conn);
//            for (int i = 0; i < resultMap.size(); i++) {
//                // System.out.println("已添加日志文件==>" + resultMap.get(i,"filename"));
//            }
            // System.out.println("开始分析日志文件,起始scn号:" + Constants.LAST_SCN);
//            callableStatement = conn.prepareCall("BEGIN dbms_logmnr.start_logmnr(startScn=>'" + Constants.LAST_SCN + "',dictfilename=>'" + Constants.DATA_DICTIONARY + "\\dictionary.ora',OPTIONS =>DBMS_LOGMNR.COMMITTED_DATA_ONLY+dbms_logmnr.NO_ROWID_IN_STMT);END;");
//            callableStatement = conn.prepareCall(" BEGIN dbms_logmnr.start_logmnr(startScn=>'" + Constants.LAST_SCN + "',OPTIONS=>DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG);END;");

            if(fangshi==1&&index==0) {
                callableStatement = conn.prepareCall(" BEGIN dbms_logmnr.start_logmnr(StartTime =>to_date('" + date + "','YYYY-MM-DD HH24:MI:SS'),OPTIONS=>DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG);END;");
            }else{
                callableStatement = conn.prepareCall(" BEGIN dbms_logmnr.start_logmnr(startScn=>'" + Constants.LAST_SCN + "',OPTIONS=>DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG);END;");
            }
            callableStatement.execute();
            // System.out.println("完成分析日志文件");
             // 查询获取分析结果
            // System.out.println("查询分析结果");
//            resultSet = statement.executeQuery("SELECT scn,operation,timestamp,status,sql_redo FROM v$logmnr_contents WHERE  seg_type_name='TABLE' AND operation !='SELECT_FOR_UPDATE' and seg_owner='SCOTT'");
            ResultMap resultMap =DBUtil.query2("SELECT scn,operation,timestamp,status,sql_redo FROM v$logmnr_contents where seg_owner='TEST2'",conn);
            String lastScn = Constants.LAST_SCN;
            String operation = null;
            String sql = null;
            boolean isCreateDictionary = false;
            for (int i = 0; i < resultMap.size(); i++) {
                lastScn = resultMap.get(i,"scn") + "";
                if (lastScn.equals(Constants.LAST_SCN)) {
                    continue;
                }
//                operation = resultSet.getObject(2) + "";
//                if ("DDL".equalsIgnoreCase(operation)) {
//                    isCreateDictionary = true;
//                }
                sql = resultMap.get(i,"sql_redo") + "";
// 替换用户
                sql = sql.replace("\"" + Constants.SOURCE_CLIENT_USERNAME + "\".", "");
                // System.out.println("scn=" + lastScn +"日期的time："+resultMap.get(i,"timestamp") + ""+",自动执行sql==" + sql + "");
            }

// 更新scn
            Constants.LAST_SCN = (Integer.parseInt(lastScn)) + "";

// DDL发生变化，更新数据字典
//            if (isCreateDictionary) {
//                // System.out.println("DDL发生变化，更新数据字典");
//                createDictionary(sourceConn);
//                // System.out.println("完成更新数据字典");
//                isCreateDictionary = false;
//            }

            // System.out.println("完成一个工作单元");
// 分析完成后,释放内存
            String endLogSQL = "BEGIN dbms_logmnr.end_logmnr;END;";
            executeCallable(endLogSQL, callableStatement, conn);


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(callableStatement!=null) {
                callableStatement.close();
            }
            if(conn!=null) {
                conn.close();
            }
            conn=null;
            callableStatement=null;
        }
    }

    /**
      *
      *  执行过程语句
     */
    private static void executeCallable(String _sql, CallableStatement _call,
                                 Connection _con) throws SQLException {
        _call = _con.prepareCall(_sql);
        _call.execute();
    }



}
