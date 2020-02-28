package cn.com.wavetop.dataone_kafka.thread;

import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.utils.FileUtils;
import cn.com.wavetop.dataone_kafka.utils.TestGetFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;

/**
 * @Author yongz
 * @Date 2019/11/18、9:37
 */

public class JobThread02 extends Thread {

    // 任务id
    private Integer jodId;

    // sql路径
    private String sqlPath;

    // 注入KafkaTemplate
    KafkaTemplate kafkaTemplate = (KafkaTemplate) SpringContextUtil.getBean("kafkaTemplate");


    public JobThread02(Integer jodId, String sqlPath) {
        this.jodId = jodId;
        this.sqlPath = sqlPath;
    }

    // 关闭线程的标识
    private boolean stopMe = true;

    @Override
    public void run() {
        ArrayList<String> fileNames;
        while (stopMe) {
            File file = new File(sqlPath + "\\offset"); // 创建文件记录java读取的位置
            // sqlPath下获取所有文件
            fileNames = TestGetFiles.getAllFileName(sqlPath);
            if (fileNames.contains("offset")) {
                String[] offsetContent = FileUtils.readTxtFile(file).split("----");
                System.out.println(offsetContent[0]+"----"+offsetContent[1]);

                BufferedReader br = null;
                StringBuffer content = new StringBuffer(); // 记录文件内容
                try {

                    br = new BufferedReader(new FileReader(offsetContent[0]));

                    for (int i = 0; i < Integer.parseInt(offsetContent[1]); i++) {
                        br.readLine();
                    }


                    String str;
                    int index = 0; // 记录读取的行数
                    while ((str = br.readLine()) != null) {//逐行读取
                        index++ ; //读取一行+1
                        if (str.equals("") || str.equals("WAVETOP_LINE_BREAK")) {
                            continue;
                        }
                        System.out.println(str);
                        kafkaTemplate.send("JodId_" + jodId, str); //发送消息

                        //
                        content.setLength(0); // 清空内容
                        content.append(offsetContent[0]);
                        content.append("----");
                        content.append(index);
                        FileUtils.writeTxtFile(content.toString(), file);
                    }
                    br.close();//别忘记，切记
                    new File(offsetContent[0]).delete(); // 删除掉
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }

            } else {

                if (!file.exists()){
                    try {
                        file.createNewFile();   // 创建文件记录java读取的位置
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for (String fileName : fileNames) {
                    if (!fileName.contains(".sql")) continue; //不是sql文件就不读

                    BufferedReader br = null;
                    StringBuffer content = new StringBuffer(); // 记录文件内容
                    try {

                        br = new BufferedReader(new FileReader(sqlPath + "\\" + fileName));
                        String str;
                        int index = 0; // 记录读取的行数
                        while ((str = br.readLine()) != null) {//逐行读取
                            index++ ; //读取一行+1
                            if (str.equals("") || str.equals("WAVETOP_LINE_BREAK")) {
                                continue;
                            }
                            System.out.println(str);
                            kafkaTemplate.send("JodId_" + jodId, str); //发送消息

                            //
                            content.setLength(0); // 清空内容
//                            content.append("file=");
                            content.append(sqlPath);
                            content.append("\\");
                            content.append(fileName);
                            content.append("----");
//                            content.append("offset=");
                            content.append(index);
                            FileUtils.writeTxtFile(content.toString(), file);
                        }
                        br.close();//别忘记，切记
                        new File(sqlPath + "\\" + fileName).delete(); // 删除掉
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    // 关闭当前线程
    public void stopMe() {
        stopMe = false;
    }


    // setter
    public void setJodId(Integer jodId) {
        this.jodId = jodId;
    }

    public void setSqlPath(String sqlPath) {
        this.sqlPath = sqlPath;
    }
}
