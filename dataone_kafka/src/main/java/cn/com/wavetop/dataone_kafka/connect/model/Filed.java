package cn.com.wavetop.dataone_kafka.connect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yongz
 * @Date 2019/12/4、17:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filed {
    private String type;
    private boolean optional;
    private String field;
}
