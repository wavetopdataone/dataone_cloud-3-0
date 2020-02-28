package com.cn.wavetop.dataone;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.entity.SysFieldrule;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.util.DateUtil;
import com.cn.wavetop.dataone.util.LinuxLogin;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class test {


    @Test
    public void show(){
        HashMap<String,String> map=new HashMap<>();
        HashMap<Object, Object> map2 = new HashMap<>();
        HashMap<Object, Object> map3 = new HashMap<>();
        ArrayList list=new ArrayList();
        map.put("filed","icon");
        map.put("optional","true");
        map.put("type","string");
        list.add(map);
        map=new HashMap<>();
        map.put("filed","id");
        map.put("optional","false");
        map.put("type","Integer");
        list.add(map);
        map2.put("type","struct");
        map2.put("fields",list);
        map3.put("schema", map2);
        map3.put("payload", "hah");
//        Map map = new HashMap();
//        map.put("name", "json");
//        map.put("bool", Boolean.TRUE);
//        map.put("int", new Integer(1));
//        map.put("arr", new String[] { "a", "b" });
//        map.put("func", "function(i){ return this.arr[i]; }");
        String json =  JSON.toJSONString(list);
        System.out.println(json);
    }

@Test
public  void ssa(){
    String a="NUBERE";
    System.out.println(a.toUpperCase());
}
@Test
public void showww(){
String a="'IMAGE','TEXT','UNIQUEIDENTIFIER','DATE','TIME','DATETIME2','DATETIMEOFFSET','TINYINT','SMALLINT','INT','SMALLDATETIME','MONEY','DATETIME','FLOAT','SQL_VARIANT','NTEXT','BIT','DECIMAL','NUMERIC','SMALLMONEY','BIGINT','HIERARCHYID','GEOMETRY','GEOGRAPHY','VARBINARY','VARCHAR','BINARY','CHAR','TIMESTAMP','NVARCHAR','NCHAR','XML','SYSNAME'";
    String []b=a.split(",");
    StringBuffer stringBuffer=new StringBuffer("[");
    for(int i=0;i<b.length;i++){

        stringBuffer.append(b[i].toLowerCase());
        stringBuffer.append(",");
    }
    stringBuffer.append("]");
    System.out.println(stringBuffer);

}


    @Test
    public void ssswq(){
        ArrayList<SysFieldrule> data = new ArrayList<>();
        List<SysFieldrule> sysFieldruleList =  new ArrayList<>();
        SysFieldrule sysFieldrule=new SysFieldrule();
        sysFieldrule.setFieldName("test1");
        data.add(sysFieldrule);
        SysFieldrule sysFieldrule1=new SysFieldrule();
        sysFieldrule1.setFieldName("test2");
        data.add(sysFieldrule1);
        SysFieldrule sysFieldrule2=new SysFieldrule();
        sysFieldrule2.setFieldName("test3");
        data.add(sysFieldrule2);
        SysFieldrule sysFieldrule3=new SysFieldrule();
        sysFieldrule3.setFieldName("test4");
        data.add(sysFieldrule3);
        SysFieldrule sysFieldrule4=new SysFieldrule();
        sysFieldrule4.setFieldName("test5");
        data.add(sysFieldrule4);



        for (int i = 0; i < data.size(); i++) {
            if(data.get(i).getFieldName().equals("test3")){
                data.add(0,data.get(i));
                data.remove(data.get(i+1));
            }
        }
        for(SysFieldrule s:data){
            System.out.println(s+"*------1");
        }
    }
    @Test
    public void shows() throws FileNotFoundException {
        Connection conn = LinuxLogin.login("192.168.1.156");
        FileOutputStream out = new FileOutputStream("H:\\aa.txt");
        SCPClient sc = new SCPClient(conn);
        try {
            sc.get("/opt/kafka/connect-logs/kafka-connect.log", out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void asq(){


        Map<Object, Object> map = new HashMap<>();
        map.put("status", "404");
        map.put("message", "系统找不到指定文件！");
        String vv = String.valueOf(JSON.toJSON(map));
        System.out.println(vv);
        List<String> list=new ArrayList<>();
        List<String> list2=new ArrayList<>();
        list.add("sys_dept");
        list.add("sys_menu");
        list.add("test1");
        list.add("test2");
        for(String a:list){
            if(a.contains("test1")){
                list2.add(a);
            }
        }
        System.out.println(list2);
//        String d="dsadsadsa技术支持";
//        if(d.contains("技术支持")){
//
//        }
//
//      double a=0.5;
//        double b=100;
//       double c= a/b;
//        BigDecimal bg = new BigDecimal(c);
//        double num1 = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
//        if(0.5>c){
//            System.out.println("hha");
//        }
//        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
//        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
//
//////
//        String nowDate = dfs.format(new Date());//几号
//        String nowDates = df.format(new Date());//几号
//
//        Set<String> set = new HashSet<>();
////        Comparator<String> comp = new DescSort();
//        set.add("2019-12-11");
//        set.add("2019-12-13");
//        set.add("2019-12-12");
//
//        set.add("2019-12-12");
//        set.add("2019-12-12");
//
//        set.add("2019-12-10");
//        Set<String> sest =getOrderByDate(set);
//        System.out.println(sest);
//////        String minue = nowDate.substring(3, 5);
////        String yesterDay = DateUtil.dateAdd(nowDate, -1);//昨天
////        System.out.println(nowDate);
////        System.out.println(yesterDay);
////       Date a=new Date("1575529091543");
//        List<SysJobrela> list=new ArrayList<>();
//        SysJobrela sysUserJobrela=new SysJobrela();
//        sysUserJobrela.setId(Long.valueOf(95));
//        sysUserJobrela.setJobName("test1");
//        SysJobrela sysUserJobrela1=new SysJobrela();
//        sysUserJobrela1.setId(Long.valueOf(96));
//        sysUserJobrela1.setJobName("test2");
//        SysJobrela sysUserJobrela4=new SysJobrela();
//        sysUserJobrela4.setId(Long.valueOf(97));
//        sysUserJobrela4.setJobName("test2");
//        list.add(sysUserJobrela);
//        list.add(sysUserJobrela1);
//        list.add(sysUserJobrela4);
//        List<SysJobrela> list1=new ArrayList<>();
//        SysJobrela sysUserJobrela2=new SysJobrela();
//        sysUserJobrela2.setId(Long.valueOf(96));
//        sysUserJobrela2.setJobName("test3");
//        list1.add(sysUserJobrela2);
//        System.out.println(list);
//        System.out.println(list1);
//       for(SysJobrela sysJobrela:list1){
//           for(int i =0;i<list.size();i++ ){
//               if(sysJobrela.getId()==list.get(i).getId()){
//                   list.get(i).setJobName("dsadasdas");
//               }
//           }
//       }
//        System.out.println(list);
//        System.out.println(list1);
    }
    @Test
public void s(){

    List<SysJobrela> list=new ArrayList<>();
        SysJobrela sysUserJobrela=new SysJobrela();
    sysUserJobrela.setId(Long.valueOf(95));
    sysUserJobrela.setJobName("test1");
        SysJobrela sysUserJobrela1=new SysJobrela();
        sysUserJobrela1.setId(Long.valueOf(96));
        sysUserJobrela1.setJobName("test12");
    list.add(sysUserJobrela);
    list.add(sysUserJobrela1);
    SysJobrela sysUserJobrela2=new SysJobrela();
        sysUserJobrela2.setId(Long.valueOf(95));
        sysUserJobrela2.setJobName("test1");
        SysJobrela sysUserJobrela3=new SysJobrela();
        sysUserJobrela3.setId(Long.valueOf(95));
        sysUserJobrela3.setJobName("test1");
    list.add(sysUserJobrela2);
        list.add(sysUserJobrela3);
      for(int i=0;i<list.size();i++){
          if(list.get(i).getJobName().equals("test1")){
              list.remove(i);
              i--;
//              System.out.println(list.get(i)+"-------1");
          }
//          System.out.println(list.get(i)+"--------2");
      }
    for (SysJobrela s:list){
        System.out.println(s+"----");
    }
}
    /**
     * 日期转星期
     *
     * @param datetime
     * @return
     */
    public static String dateToWeek(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        Calendar cal = Calendar.getInstance(); // 获得一个日历
        Date datet = null;
        try {
            datet = f.parse(datetime);
            cal.setTime(datet);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

     @Test
    public void sqa(){
//         SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
//
//         String nowDate = dfs.format(new Date());//几号
         Date date=new Date();
         SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
         String ab=dfs.format(date);
//         if(num.equals("7")) {
             DateUtil.dateAdd(ab, -6);
//         }
//         System.out.println(d-a);
     }

     @Autowired
     private RedisTemplate<String,Object> redisTemplate;
     @Test
    public void sawq(){
         List<String> list=new ArrayList<>();
         list.add("1");
      redisTemplate.opsForValue().set("a",list);
     }
     @Test
    public void encryptPassword() {
        String salt = new SecureRandomNumberGenerator().nextBytes().toHex(); //生成盐值
        String ciphertext = new Md5Hash("Aa111111", salt, 3).toString(); //生成的密文
        String[] strings = new String[]{salt, ciphertext};
    }

    //驗證密碼
//        String ciphertext = new Md5Hash("88888","0ae975a6aeb859797f60e98c575ee12c",3).toString(); //生成的密文
//       String password="a3be8ba9c44a062345e2a210661df3b8";
//        System.out.println(ciphertext);
//                if(ciphertext.equals(password)){
//                    System.out.println(true);
//                }
//        String splits = "id,id,NUMBER,22,$,name,name,VARCHAR2,255,$";
//        String[]  splitss= splits.replace("$","@").split(",@,");
//     for(String s:splitss){
//         System.out.println(s);
//     }
//        List<SysFieldrule> list = new ArrayList<>();
////        String[] a="aa,bb,cc,".split(",");
////        for(int b=0;b<a.length;b++){
////            System.out.println(a[b]);
////        }
//        SysFieldrule s=new SysFieldrule();
//         s.setSourceName("123");
//         s.setDestName("abc");
//        SysFieldrule s1=new SysFieldrule();
//        s1.setSourceName("456");
//        s1.setDestName("abc");
//        SysFieldrule s3=new SysFieldrule();
//        s3.setSourceName("789");
//        s3.setDestName("abc");
//        SysFieldrule s2=new SysFieldrule();
//        s2.setSourceName("123");
//        s2.setDestName("abc");
//        SysFieldrule s4=new SysFieldrule();
//        s4.setSourceName("456");
//        s4.setDestName("abc");
//        list.add(s);
//        list.add(s3);
//    list.add(s1);
//    Set<SysFieldrule> sysFieldrules=new HashSet<SysFieldrule>();
//        sysFieldrules.add(s2);
//        sysFieldrules.add(s4);
//        Iterator<SysFieldrule> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            SysFieldrule num = iterator.next();
//            if (sysFieldrules.contains(num)) {
//                iterator.remove();
//            }
//        }
//        for (SysFieldrule b : list) {
//            System.out.println(b);
//        }
        //        Long a = Long.valueOf(5);
//        System.out.println(a.longValue() + a);
//        List<String> list = new ArrayList<>();
//        list.add("aa");
//        list.add("aa");
//        list.add("aa");
//        list.add("bb");
//        List<String> newList = new ArrayList();
//        newList.add("aa");
//        Iterator<String> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            String num = iterator.next();
//            if (newList.contains(num)) {
//                iterator.remove();
//            }
//        }
//        for (String b : list) {
//            System.out.println(b);
//        }
    @Test
    public void safd(){

    }
}
