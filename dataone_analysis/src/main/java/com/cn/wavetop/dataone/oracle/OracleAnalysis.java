package com.cn.wavetop.dataone.oracle;

/**
 * oracle的解析
 */
public class OracleAnalysis extends Thread{
    private Integer jobId;
    private String tableName;
    private boolean flag=true;
    public OracleAnalysis(Integer jobId,String tableName) {
        this.jobId=jobId;
        this.tableName=tableName;
    }
    @Override
    public void run() {
       while(flag){
           System.out.println("开始解析咯"+jobId+tableName);
//           stopMe();
       }
    }


    public  boolean stopMe(){
        flag=false;
        return flag;
    }
}
