package com.cn.wavetop.dataone.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yongz
 * @Date 2019/10/10、14:22
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ToDataMessage{
   private String status;
   private String message;
}
