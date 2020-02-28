package cn.com.wavetop.dataone_kafka.entity.vo;

import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToData {
   private String status;
   private SysDbinfo data;
   private String message;
}
