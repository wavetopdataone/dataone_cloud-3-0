package com.cn.wavetop.dataone.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author yongz
 * @Date 2019/10/10、14:22
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ToData {
   private String status;
   private List data;
   private String message;
}
