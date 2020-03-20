package com.cn.wavetop.dataone.utils;

import com.cn.wavetop.dataone.compilerutil.CustomStringJavaCompiler;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/19、14:07
 */
public class Test {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, UnsupportedEncodingException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String code = "import java.io.IOException;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "/**\n" +
                " * @Author yongz\n" +
                " * @Date 2020/3/9、14:23\n" +
                " */\n" +
                "public class Test {\n" +
                "    public static Map test(String args, String a, Map map) throws IOException {\n" +
                "        System.out.println(args+\"-------\"+a+\"------\"+map);\n" +
                "        System.out.println(args+\"-------\"+a+\"------\"+map);\n" +
                "        map.put(\"xuezihao2\", \"909\");\n" +
                "        map.put(\"xuezihao\", map.get(\"xuezihao\")+\"xiugai489564\");\n" +
                "        return map;\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap<Object, Object> map = new HashMap<>();\n" +
                "        map.put(\"xuezihao\", \"18\");\n" +
                "        try {\n" +
                "            Map xuezihao = test(\"xuezihao\", \"28\", map);\n" +
                "            System.out.println(xuezihao);\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}";

        CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(code);
        boolean compiler1 = compiler.compiler();
        System.out.println(compiler1);
        Class cls = compiler.getScriptClass();
        String fullClassName = compiler.getFullClassName();
        System.out.println(fullClassName);


        // 反射的基础
        Object o = cls.newInstance();
        Method test = cls.getMethod("test", String.class, String.class, Map.class);
        test.setAccessible(true);// 暴力反射
        HashMap<Object, Object> map = new HashMap<>();
        map.put("xuezihao", "18");
        Map invoke = (Map) test.invoke(o, "xuezihao", "28", map);

        System.out.println(invoke);
    }
}
