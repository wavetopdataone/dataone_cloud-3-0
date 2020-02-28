package cn.com.wavetop.rureka;

import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class sss {
@Test
public void ss(){
//    start:1579578616146
//    end:1579578620382
    long start=1579598627706l;
    long  end=1579598630414l;
    System.out.println(end-start);
//    start:1579578337545
//    end:1579578344533
    int index=1;
//
//    StringBuffer  stringBuffer=new StringBuffer(" CREATE TABLE `22211`  (  `id` int(11) NOT NULL AUTO_INCREMENT,");
//    String c;
//    for(int i=0;i<1000;i++){
//         index++;
//        StringBuffer  stringBuffers=new StringBuffer("");
//        c="`opt_type"+index+"`";
//        stringBuffers.append(c);
//        stringBuffers.append("int(11) NULL DEFAULT NULL,");
//        stringBuffer.append(stringBuffers);
////        System.out.println(stringBuffers);
////        System.out.println(stringBuffer);
//        new Date();
//    }
//    stringBuffer.append(" PRIMARY KEY (`id`) USING BTREE\n" +
//            ") ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;");
//    System.out.println(stringBuffer);
}
    @Test
    public void show(){
        Set<String> set=new TreeSet<>();
        set.add("c");
        set.add("b");
        set.add("a");
        set.add("2");
        set.add("阿你");
        set.add("1");
        set.add("爱");
        set.add("A");

        char sasas = '爱';
        char sasas2 = '阿';

        int index1= sasas;
        int index2= sasas2;

        System.out.println(index1);
        System.out.println(index2);

        for(String a:set){
            System.out.println(a);
        }
    }
}
