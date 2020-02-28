package com.cn.wavetop.dataone.util;


import com.cn.wavetop.dataone.entity.vo.DatabaseVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.*;


public  class DataBaseUtil extends Thread{
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    private DatabaseVo databaseVo = (DatabaseVo) SpringContextUtil.getBean("databaseVo");
//    private StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) SpringContextUtil.getBean("stringRedisTemplate");
//
//    @Override
//    public  void run() {
//        System.out.println("mysql -h"+databaseVo.getUrl()+" -u"+databaseVo.getUser()+" -p"+databaseVo.getPassword()+" --default-character-set=utf8 "
//                + databaseVo.getDatabaseName());
//        try {
//
//            Runtime runtime = Runtime.getRuntime();
//            Process process = runtime
//                    .exec("mysql -h"+databaseVo.getUrl()+" -u"+databaseVo.getUser()+" -p"+databaseVo.getPassword()+" --default-character-set=utf8 "
//                            + databaseVo.getDatabaseName());
//            OutputStream outputStream = process.getOutputStream();
//            FileInputStream fis = new FileInputStream(databaseVo.getPath());
//            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
//            BufferedReader br = new BufferedReader(isr);
//            String str = null;
//            StringBuffer sb = new StringBuffer();
//            while ((str = br.readLine()) != null) {
//                sb.append(str + "\r\n");
//            }
//            str = sb.toString();
//            OutputStreamWriter writer = new OutputStreamWriter(outputStream,"utf-8");
//            writer.write(str);
//            writer.flush();
//            if(writer!=null){
//                writer.close();
//            }
//
//            if(br!=null){
//                br.close();
//            }
//            if(isr!=null){
//                isr.close();
//            }
//            if(fis!=null){
//                fis.close();
//            }
//            if(outputStream!=null){
//                outputStream.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//         logger.error("数据库sql导入失败");
//        }
//        logger.info("数据库sql导入成功");
//    }
//
//
//
//
//
////    /**
////     * Java代码实现MySQL数据库导出
////     *
////     * @author GaoHuanjie
////     * @param hostIP MySQL数据库所在服务器地址IP
////     * @param userName 进入数据库所需要的用户名
////     * @param password 进入数据库所需要的密码
////     * @param savePath 数据库导出文件保存路径
////     * @param fileName 数据库导出文件文件名
////     * @param databaseName 要导出的数据库名
////     * @return 返回true表示导出成功，否则返回false。
////     */
////    public static boolean exportDatabaseTool(String hostIP, String userName, String password, String savePath, String fileName, String databaseName) throws InterruptedException {
////        File saveFile = new File(savePath);
////        if (!saveFile.exists()) {// 如果目录不存在
////            saveFile.mkdirs();// 创建文件夹
////        }
////        if(!savePath.endsWith(File.separator)){
////            savePath = savePath + File.separator;
////        }
////
////        PrintWriter pw = null;
////        BufferedReader bufferedReader = null;
////        try {
////            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(savePath + fileName), "utf8"));
////            Process process = Runtime.getRuntime().exec(" mysqldump -h" + hostIP + " -u" + userName + " -p" + password + " --set-charset=UTF8 " + databaseName);
////            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), "utf8");
////            bufferedReader = new BufferedReader(inputStreamReader);
////            String line;
////            while((line = bufferedReader.readLine())!= null){
////                pw.println(line);
////            }
////            pw.flush();
////            if(process.waitFor() == 0){//0 表示线程正常终止。
////                return true;
////            }
////        }catch (IOException e) {
////            e.printStackTrace();
////        } finally {
////            try {
////                if (bufferedReader != null) {
////                    bufferedReader.close();
////                }
////                if (pw != null) {
////                    pw.close();
////                }
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        }
////        return false;
////    }
////
////
////    @Test
////    public void test() throws IOException{
//////        try {
//////            //beifen
//////            exportDatabaseTool("localhost","root","admin","H:\\","haha","ry.sql");
//////        } catch (InterruptedException e) {
//////            e.printStackTrace();
//////        }
////        //huifu
//////        System.out.println(restore("localhost", "root", "admin", "shop", "C:\\Users\\admin\\Desktop\\dataone1.sql"));;
////    }
//
//    /**
//     * 恢复
//     * @param url
//     * @param user
//     * @param password
//     * @param databaseName
//     * @param path
//     * @return
//     */

}
