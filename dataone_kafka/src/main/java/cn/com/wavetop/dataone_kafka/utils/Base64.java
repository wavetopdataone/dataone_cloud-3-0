package cn.com.wavetop.dataone_kafka.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;

/**
 * @Author yongz
 * @Date 2019/12/6、17:00
 */
public class Base64 {
    public static void main(String[] args) throws Exception {
        final BASE64Encoder encoder = new BASE64Encoder();
        final BASE64Decoder decoder = new BASE64Decoder();
        final String text = "1";
        final byte[] textByte = text.getBytes("UTF-8");
//编码
        final String encodedText = encoder.encode(textByte);
        System.out.println(encodedText);
//解码
        System.out.println(new String(decoder.decodeBuffer(encodedText), "gbk"));

    }
}
