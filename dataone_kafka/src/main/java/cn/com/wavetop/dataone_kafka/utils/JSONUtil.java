package cn.com.wavetop.dataone_kafka.utils;

import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.*;
import java.util.List;
/**
 * @Author yongz
 * @Date 2019/10/27、19:36
 */
public class JSONUtil {

    // 定义jackson对象
    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * 将对象转换成json字符串
     * @param data
     * @return
     */
    public static String toJSONString(Object data) {
        try {
            String string = mapper.writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json结果集转化为对象
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T> T parseObject(String jsonData, Class<T> beanType) {
        try {
            T t = mapper.readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json数据转换成list
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T> List<T> parseArray(String jsonData, Class<T> beanType) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = mapper.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取json文件
     */
    public static String readJsonFile(String path){
        String laststrJson = "";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                laststrJson = laststrJson + tempString;
                line++;
            }
            reader.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return laststrJson;
    }

    /**
     * 写出json文件
     */
    public static void writeJsonFile(String newJsonString, String path){
        try {
            FileWriter fw = new FileWriter(path);
            PrintWriter out = new PrintWriter(fw);
            out.write(newJsonString);
            out.println();
            fw.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static SysDbinfo getSysDbinfo(String jsonString) {
        SysDbinfo sysDbinfo = null;
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonString);
            org.json.JSONObject sysDbinfoObject = jsonObject.getJSONObject("data");
            sysDbinfo = SysDbinfo.builder()
                    .host(sysDbinfoObject.getString("host"))
                    .dbname(sysDbinfoObject.getString("dbname"))
                    .user(sysDbinfoObject.getString("user"))
                    .password(sysDbinfoObject.getString("password"))
                    .port(sysDbinfoObject.getLong("port"))
                    .type(sysDbinfoObject.getLong("type"))
                    .build();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return sysDbinfo;
    }
}