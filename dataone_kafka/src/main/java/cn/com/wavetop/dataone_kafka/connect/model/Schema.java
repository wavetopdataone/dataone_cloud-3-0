package cn.com.wavetop.dataone_kafka.connect.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/12/4、14:20
 */
@Data
@Builder
public class Schema {

    private String type = "struct"; // 类型
    private List<Map> fields; // 字段类型
    private boolean optional = false;
    private String name; // 源端表名

}
