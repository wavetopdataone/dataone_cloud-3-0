package com.cn.wavetop.dataone.entity.vo;

import lombok.Data;

@Data
public class EmailDescriptionVo {
    private String path;//发送附件的路径
    private String Description;//文件说明
    private String name;//文件名字
}
