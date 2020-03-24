package com.cn.wavetop.dataone.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/24、15:29
 *
 * 在线编译返回值
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptMessage {
    /**
     *  status 0表示执行成功，
     *  非0表示失败： 1表示编译失败，
     *                2表示获取class失败，
     *                3表示构造实例对象失败
     *                4表示获取脚本方法失败
     *                5表示执行脚本方法失败
     */
    private int status;

    /**
     * 状态描述
     */
    private String message;

    /**
     * 错误信息
     */
    private String errorMessage;

    // 执行结果集
    private Map result;

}
