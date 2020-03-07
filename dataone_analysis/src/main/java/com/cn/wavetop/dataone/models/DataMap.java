package com.cn.wavetop.dataone.models;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/7、14:30
 */
@Data
@Builder
public class DataMap {

    private Map payload;
    private Map message;
}
