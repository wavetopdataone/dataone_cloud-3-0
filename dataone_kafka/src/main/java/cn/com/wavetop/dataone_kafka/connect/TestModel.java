package cn.com.wavetop.dataone_kafka.connect;

import cn.com.wavetop.dataone_kafka.connect.model.Schema;
import cn.com.wavetop.dataone_kafka.utils.JSONUtil;
import cn.com.wavetop.dataone_kafka.utils.PrassingUtil;
import jdk.nashorn.internal.ir.annotations.Ignore;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.kafka.common.protocol.types.Field;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author yongz
 * @Date 2019/12/4、14:31
 */
public class TestModel {

    public static void main(String[] args) throws JSQLParserException {
//        Schema schema = mysqlToSchema("IF NOT EXISTS (SELECT TAB.NAME FROM TEST1.SYS.TABLES AS TAB LEFT JOIN TEST1.SYS.SCHEMAS AS SC ON TAB.SCHEMA_ID = SC.SCHEMA_ID WHERE TAB.NAME='sys_menu' AND SC.NAME='dbo') CREATE TABLE TEST1.dbo.sys_menu (id bigint ,icon NVARCHAR(255) NULL,menu_id bigint NULL,menu_name NVARCHAR(255) NULL,menu_type NVARCHAR(255) NULL,order_num NVARCHAR(255) NULL,parent_id bigint NULL,parent_name NVARCHAR(255) NULL,perms NVARCHAR(255) NULL,target NVARCHAR(255) NULL,url NVARCHAR(255) NULL,visible NVARCHAR(255) NULL,create_time datetime NULL,create_user NVARCHAR(255) NULL,update_time datetime NULL,update_user NVARCHAR(255) NULL,PRIMARY KEY (id ))", 2);
////        getStringTime(1575455606000L);
//        System.out.println(schema);
//        HashMap<String, Schema> schemas = new HashMap<>();
//        schemas.put(schema.getName(), schema);
//        String data = toJsonString2("INSERT INTO \"test\".\"file1\"(\"date\",\"time\") VALUES (TO_DATE('2019-12-04 00:00:00','YYYY-MM-DD HH24:MI:SS'),'00:00:00')", schemas, 2);
//        System.out.println(data);

        System.out.println(getTimestamp("2020-01-14", "yyyy-MM-dd"));
        System.out.println(getStringTime(1578931200000L, "yyyy-MM-dd HH:mm:ss"));
//        System.out.println(getTimestamp("2019-10-09 00:00:00","yyyy"));

    }

    /**
     * @param time timestamp
     * @desc 时间戳转字符串
     * @example timestamp=1558322327000
     **/
    public static String getStringTime(Long time, String type) {
        return new SimpleDateFormat(type).format(new Date(time));
    }

    /**
     * @param time timestamp
     * @return
     * @desc 字符串转时间戳
     * @example timestamp=1558322327000
     */
    public static Long getTimestamp(String time, String type) {
        try {
            return new SimpleDateFormat(type).parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("报错了！" + time + type);
        }
        return null;
    }

    /**
     * 解析mysql的create语句
     *
     * @param creat_table
     * @return
     */
    public static Schema mysqlToSchema(String creat_table, int dbType) {
        ArrayList fileds = new ArrayList<>();
        if (creat_table.contains("IF NOT EXISTS (SELECT")) {
            creat_table = creat_table.substring(creat_table.indexOf("CREATE TABLE"));
        }

//        System.out.println(creat_table);
        String tableName = "";
        // todo 表名获取待优化
        String[] s1 = creat_table.split(" ");
        for (String s : s1) {
            if (s.contains(".")) {
                String[] s2 = s.split("\\.");
                s = s2[s2.length - 1];
                if (s.contains("'") || s.contains("\"")) {
                    s = s.substring(1, s.length() - 1);
                }
                tableName = s;
            }
//            System.out.println(s);
        }
        String[] strings = creat_table.substring(creat_table.indexOf("(") + 1, creat_table.lastIndexOf(")")).split(",");
//        System.out.println(Arrays.toString(strings));

        String filedType;
        String field; // 字段
        HashMap map;
        for (int i = 0; i < strings.length; i++) {
            map = new HashMap<>();
            if (strings[i].contains("PRIMARY KEY")) continue; //这个不是字段的调过
            if (strings[i].split(" ").length < 2) continue; // 精度

            filedType = strings[i].split(" ")[1];
            if (filedType.equalsIgnoreCase("TINYINT") || filedType.contains("bit") || filedType.contains("BINARY")) {
                filedType = "int8";
            } else if (filedType.equalsIgnoreCase("SMALLINT")) {
                filedType = "int16";
            } else if (filedType.equalsIgnoreCase("INT")) {
                filedType = "int32";
            } else if (filedType.equalsIgnoreCase("BIGINT") || filedType.contains("bigint") || filedType.contains("int") || filedType.contains("INT") || filedType.contains("NUMBER") || filedType.contains("DECIMAL") || filedType.contains("decimal")|| filedType.contains("NUMERIC")) {
                filedType = "int64";
            } else if (filedType.equalsIgnoreCase("FLOAT")) {
                filedType = "float32";
            } else if (filedType.equalsIgnoreCase("DOUBLE")) {
                filedType = "float64";
            } else if (filedType.contains("VARCHAR") || filedType.contains("varchar")) {
                filedType = "string";
            } else if (filedType.contains("VARBINARY")||filedType.contains("text")||filedType.contains("TEXT")||filedType.contains("varbinary")||filedType.contains("BLOB")||filedType.contains("blob")||filedType.contains("BYTE")) {
                filedType = "bytes";
            } else if (filedType.contains("DATE") || filedType.contains("date") || filedType.contains("TIME") || filedType.contains("time")) {
                filedType = "int64";
                map.put("name", "org.apache.kafka.connect.data.Timestamp");
                map.put("version", 1);
            }

            field = strings[i].split(" ")[0];
            if (field.contains("'") || field.contains("\"")) {
                field = field.substring(1, field.length() - 1);
            }
            map.put("type", filedType);
            map.put("optional", true);
            if (dbType == 1) {
                field = field.toUpperCase();
            }
            map.put("field", field);

            fileds.add(map);
            map = null;
        }

        if (dbType == 1) {
            tableName = tableName.toUpperCase();
        }
        return Schema.builder().type("struct").fields(fileds).name(tableName).build();
    }

    public static String toJsonString2(String insertSql, HashMap<String, Schema> schemas, int dbType) throws JSQLParserException {

        if (insertSql.contains("IF NOT EXISTS") && insertSql.contains("ELSE")) {
            insertSql = insertSql.substring(insertSql.indexOf("INSERT INTO"), insertSql.indexOf("ELSE UPDATE"));
        }
//        System.out.println(insertSql);


        HashMap<Object, Object> map = new HashMap<>();
        List<String> insert_columns = null;
        List<String> insert_values = null;
        try {
//            System.out.println(insertSql);
            insert_columns = PrassingUtil.get_insert_column(insertSql);
            insert_values = PrassingUtil.get_insert_values(insertSql);

        } catch (
                Exception e) {
//            System.err.println(insertSql);
            e.printStackTrace();
        }

        String insert_table = PrassingUtil.get_insert_table(insertSql);

        if (insert_table.contains("'") || insert_table.contains("\"")) {
            insert_table = insert_table.substring(1, insert_table.length() - 1);
        }

        if (dbType == 1l) {//  todo Oacle表名转大写的问题
            insert_table = insert_table.toUpperCase();
        }
//        System.out.println(insert_table);
        Schema schema = schemas.get(insert_table);
        String column;
        String value;
        for (
                int i = 0; i < insert_columns.size(); i++) {
            column = insert_columns.get(i);
            value = insert_values.get(i);
            if (column.contains("'") || column.contains("\"")) {
                column = column.substring(1, column.length() - 1);
            }
            if (dbType == 1) {
                column = column.toUpperCase();
            }
            // 解析ORACLE数据库的TO_DATE函数中的值
            if ((value.contains("DATE") || value.contains("data")) && value.contains("(")) {
                String[] split = value.substring(value.indexOf("("), value.lastIndexOf(")")).split(",");
                split[0] = split[0].substring(2, split[0].length() - 1); // 获取时间
                if (split[0].length() == 4) {
                    map.put(column, getTimestamp(split[0], "yyyy"));
                } else if (split[0].length() == 8) {
                    map.put(column, getTimestamp(split[0], "HH:mm:ss"));
                } else if (split[0].length() == 10) {
                    map.put(column, getTimestamp(split[0], "yyyy-MM-dd"));
                } else if (split[0].length() == 19) {
                    map.put(column, getTimestamp(split[0], "yyyy-MM-dd HH:mm:ss"));
                } else if (split[0].length() == 23) {
                    map.put(column, getTimestamp(split[0], "yyyy-MM-dd HH:mm:ss.SSS"));
                }

            } else {
                if (value.contains("'") || value.contains("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
//                System.out.println(value);

                List<Map> schemaFields = schema.getFields();
                for (Map schemaField : schemaFields) {
                    String field = (String) schemaField.get("field");
                    if (field.equalsIgnoreCase(column)) {

                        // 首先她两必须相等
//                        System.out.println(field);
                        String type = (String) schemaField.get("type");

                        String name = (String) schemaField.get("name");
                        if ("NULL".equalsIgnoreCase(value)) {
                            map.put(column, null);
                        } else {
                            if ("org.apache.kafka.connect.data.Timestamp".equals(name)) {
                                if (value.length() == 4) {
                                    map.put(column, getTimestamp(value, "yyyy"));
                                } else if (value.length() == 8) {
                                    map.put(column, getTimestamp(value, "HH:mm:ss"));
                                } else if (value.length() == 10) {
                                    map.put(column, getTimestamp(value, "yyyy-MM-dd"));
                                } else if (value.length() == 19) {
                                    map.put(column, getTimestamp(value, "yyyy-MM-dd HH:mm:ss"));
                                } else if (value.length() == 23) {
                                    map.put(column, getTimestamp(value, "yyyy-MM-dd HH:mm:ss.SSS"));
                                }
                            } else {

                                if (type.contains("int")) {
                                    value = value.replaceAll("[\']", "");
                                    map.put(column, Long.parseLong(value));
                                } else if (type.contains("float")) {
                                    value = value.replaceAll("[\']", "");
                                    map.put(column, Double.parseDouble(value));
                                } else if (type.contains("boolean")) {
                                    value = value.replaceAll("[\']", "");
                                    map.put(column, Boolean.parseBoolean(value));
                                } else {
                                    map.put(column, value);
                                }
                            }
                        }
                    }
                }

            }


        }

//        System.out.println(insertSql);
//        String[] insert = insertSql.split("VALUES");
//        String[] fields = insert[0].substring(insert[0].indexOf("(") + 1, insert[0].lastIndexOf(")")).split(",");
//        String[] values = insert[1].substring(insert[1].indexOf("(") + 1, insert[1].lastIndexOf(")")).split(",");
//        System.out.println(fields.length);
//        System.out.println(values.length);
//        for (int i = 0; i < fields.length; i++) {
////
//            if (values[i].contains("'") || values[i].contains("\"")) {
//                values[i] = values[i].substring(1, values[i].length() - 1);
//            }
//            if (fields[i].contains("'") || fields[i].contains("\"")) {
//                fields[i] = fields[i].substring(1, fields[i].length() - 1);
//            }
//            System.out.println(fields[i]+values[i]);
//            map.put(fields[i], values[i]);
////            List<Map> schemaFields = schema.getFields();
////            for (Map map1 : schemaFields) {
////                String field = (String) (map1.get("field"));
////                if (field.equalsIgnoreCase(fields[i])) {
////                    if (map1.get("name") == null || "".equals(map1.get("name"))) {
////
////                        String type = (String) map1.get("type");
////
////                        if (type.contains("int")) {
////                            map.put(fields[i], Long.parseLong(values[i]));
////                        } else if (type.contains("float")) {
////                            map.put(fields[i], Double.parseDouble(values[i]));
////                        } else if (type.contains("bytes")) {
////                            // 待定
////                            map.put(fields[i], values[i]);
////                        }else {
////                            map.put(fields[i], values[i]);
////                        }
////                    }else {
////                        String name = (String) map1.get("name");
////
////                        if (name.contains("Timestamp")){
////                            // 待定
////                            map.put(fields[i], values[i]);
////                        }
////                    }
////                }
////
////            }
//
//        }


        HashMap<Object, Object> map2 = new HashMap<>();
        map2.put("schema", schema);
        map2.put("payload", map);

        //        System.out.println(map2);
        String s = JSONUtil.toJSONString(map2);
//        System.out.println(s);
        map.clear();
        map = null;
        map2.clear();
        map2 = null;
        return s;
    }


}
