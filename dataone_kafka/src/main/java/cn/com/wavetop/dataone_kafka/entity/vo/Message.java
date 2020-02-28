package cn.com.wavetop.dataone_kafka.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/11/4、19:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    private Map schema;
    private String payload;

}
