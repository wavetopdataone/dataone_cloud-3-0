package com.cn.wavetop.dataone.utils;

import com.cn.wavetop.dataone.compilerutil.CustomStringJavaCompiler;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * Create by andy on 2018-12-06 15:21
 */
@Data
public class Test2 {

    public static void main(String[] args) throws Exception {
        String code = "import java.io.IOException;\n" +
                "\n" +
                "/**\n" +
                " * @Author yongz\n" +
                " * @Date 2020/3/9、14:23\n" +
                " */\n" +
                "public class Test {\n" +
                "    public static void test(String args,String a) throws IOException {\n" +
                "        System.out.println(args+\"-------\"+a);\n" +
                "        System.out.println(args+\"-------\"+a);\n" +
                "        System.out.println(args+\"-------\"+a);\n" +
                "\n" +
                "    }\n" +
                "}";
        System.out.println(
                code
        );
        CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(code);
        boolean res = compiler.compiler();
        if (res) {
            System.out.println("编译成功");
            // 通过类名 加载class
//            ClassLoader classLoader = new ClassClassLoader(getClass().getClassLoader());
            Class cls = compiler.getScriptClass();


            // java反射机制 method.setAcessible 设置允许访问
            Method method = cls.getMethod("test",String.class,String.class);

            method.setAccessible(true);
            Object o = cls.newInstance();
//String [] a={};
            method.invoke(o,"my name is ","zhengyong!");

        } else {
            System.out.println("编译失败");
            System.out.println(compiler.getCompilerMessage());
        }

    }


}