package cn.com.wavetop.dataone_kafka.entity.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysDbinfo {

    private Long id;
    private String host;
    private String user;
    private String password;
    private String name;
    private String dbname;
    private String schema;
    private Long port;
    private Long sourDest;
    private Long type;

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

        }
        return sysDbinfo;
    }
}
