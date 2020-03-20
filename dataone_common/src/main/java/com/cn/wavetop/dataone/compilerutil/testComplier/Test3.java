package com.cn.wavetop.dataone.compilerutil.testComplier;

/**
 * @Author yongz
 * @Date 2020/03/20、16:53
 */
public class Test3 {

    public static void main(String[] args) throws InterruptedException {
        String loop = "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        while(true){\n" +
                "            System.out.println(\"Hello World!\");\n" +
                "        }\n" +
                "       \n" +
                "    }\n" +
                "}";

        String sleep_loop = "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "    try {\n" +
                "            Thread.sleep(6000);\n" +
                "        } catch (InterruptedException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "       System.out.println(\"Hello World!\");\n" +
                "        while(true){\n" +
                //"            System.out.println(\"Hello World!\");\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String ok = "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "       System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}";

        TestRun t = new TestRun(ok, "thread:ok");
        t.start();

        TestRun t1 = new TestRun(loop, "thread:loop:");
        t1.start();
        //
        TestRun t2 = new TestRun(sleep_loop, "thread:sleep_loop:");
        t2.start();


    }


}

class TestRun extends Thread {
    String code;

    TestRun(String code, String name) {
        this.code = code;
        super.setName(name);
    }

    @Override
    public void run() {
        System.out.println(CompilerUtil.getRunInfo(code));
    }
}

