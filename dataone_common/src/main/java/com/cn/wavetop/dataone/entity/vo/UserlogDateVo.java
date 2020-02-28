package com.cn.wavetop.dataone.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserlogDateVo {
    private String date;
    private List<UserlogVo> userlogVos;
}
