package cn.com.wavetop.service.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * @Author yongz
 * @Date 2019/11/5、15:50
 */

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User {

    private Long id;

    private String username;

    private String password;

    private Double balance;


}
