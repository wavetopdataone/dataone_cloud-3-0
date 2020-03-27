package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysScriptRepository;
import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.dao.SysUserScriptRepository;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysScriptService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import com.cn.wavetop.dataone.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SysScriptServiceImpl implements SysScriptService {
    @Autowired
    private SysScriptRepository sysScriptRepository;
    @Autowired
    private SysUserScriptRepository sysUserScriptRepository;
    @Autowired
    private SysUserRepository sysUserRepository;

    @Override
    public Object findByScriptName(Integer scriptFlag, String scriptName) {
        List<SysScript> scriptNameList = null;
        //*1代表默认模板，*2代表脚本库
        if (scriptFlag == 1) {
            //查询默认模板
            scriptNameList = sysScriptRepository.findByScriptNameContainingAndScriptFlag("%" + scriptName + "%", scriptFlag);
        } else {
            scriptNameList = sysScriptRepository.findByScriptNameAndScriptFlag(PermissionUtils.getSysUser().getId(), scriptName);
        }
        return ToData.builder().status("1").data(scriptNameList).build();
    }

    @Override
    public Object findAll(Integer scriptFlag) {
        List<SysScript> scriptNameList = null;
        //*1代表默认模板，*2代表脚本库
        if (scriptFlag == 1) {
            scriptNameList = sysScriptRepository.findByScriptFlag(scriptFlag);
        } else {
            scriptNameList = sysScriptRepository.findByScriptFlag(PermissionUtils.getSysUser().getId());
        }

        return ToData.builder().status("1").data(scriptNameList).build();
    }

    @Override
    public Object findById(Long id) {
        Optional<SysScript> sysScript = sysScriptRepository.findById(id);
        return sysScript.get();
    }

    @Transactional
    @Override
    public Object deleteById(Long id) {
        sysUserScriptRepository.deleteByScript(id);
        sysScriptRepository.deleteById(id);
        return ToDataMessage.builder().status("1").message("删除成功").build();
    }

    /**
     * 保存
     *
     * @param
     * @return
     */
    @Transactional
    @Override
    public Object save(SysScript sysScript) {
        List<SysScript> sysScriptList = sysScriptRepository.findByScriptName(PermissionUtils.getSysUser().getId(), sysScript.getScriptName());
        if (sysScriptList != null && sysScriptList.size() > 0) {
            return ToDataMessage.builder().status("0").message("该用户已经存在名称为"+sysScriptList.get(0).getScriptName()+"的脚本名称了").build();
        } else {
            SysScript sys = sysScriptRepository.save(sysScript);
            String prems = null;
            List<SysRole> sysRoles = sysUserRepository.findUserById(PermissionUtils.getSysUser().getId());
            if (sysRoles != null && sysRoles.size() > 0) {
                prems = sysRoles.get(0).getRoleKey();
            }
            SysUserScript sysUserScript = SysUserScript.builder().
                    scriptId(sys.getId()).
                    userId(PermissionUtils.getSysUser().getId()).
                    deptId(PermissionUtils.getSysUser().getDeptId()).
                    prems(Long.valueOf(prems)).build();
            sysUserScriptRepository.save(sysUserScript);
        }
        return ToDataMessage.builder().status("1").message("添加成功").build();
    }
    /**
     * 修改
     *
     * @param
     * @return
     */
    @Transactional
    @Override
    public Object update(SysScript sysScript) {
        System.out.println("sysScript----"+sysScript);
        Optional<SysScript> sysScriptList = sysScriptRepository.findById(sysScript.getId());
        if (sysScriptList != null ) {
            SysScript sysScript1 =  sysScriptRepository.findByUserIdAndScriptName(PermissionUtils.getSysUser().getId(),sysScriptList.get().getId(),sysScriptList.get().getScriptName());
              if(sysScript1!=null){
                  sysScript1.setScriptContent(sysScript.getScriptContent());
                  sysScript1.setScriptName(sysScript.getScriptName());
                  sysScriptRepository.save(sysScript1);
              }else{
                  return ToDataMessage.builder().status("0").message("脚本名称重复").build();
              }
        } else {
            return ToDataMessage.builder().status("0").message("不存在此脚本").build();
        }
        return ToDataMessage.builder().status("1").message("修改成功").build();
    }

    @Transactional
    @Override
    public Object updateScriptName(Long id, String scriptName) {
        SysScript sysScript1 = sysScriptRepository.findByUserIdAndScriptName(PermissionUtils.getSysUser().getId(), id, scriptName);
        if (sysScript1 != null) {
            sysScriptRepository.updateScriptName(id, scriptName);
            return ToDataMessage.builder().status("1").message("修改成功").build();
        }else{
            return ToDataMessage.builder().status("0").message("该用户下已存在此名称的脚本了").build();
        }
    }

    @Override
    public Object copyScript(Long id) {
        Optional<SysScript> sysScript = sysScriptRepository.findById(id);
        String content = sysScript.get().getScriptContent();
//        String[] copy = content.split("process(Map record) \\{");
        Map map = new HashMap<>();

        String copyScript = content.substring(StringUtils.getCharacterPosition(content), content.indexOf("return"));
        map.put("status",1);
        map.put("copyScript",copyScript);
        return map;
    }


    public static void main(String[] args) {
        String a = "import java.time.LocalDateTime;\n" +
                "import java.time.format.DateTimeFormatter;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "public  class TimestampProcess {\n" +
                "    private  static  final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(\"yyyy-MM-dd\");\n" +
                "    private  static  final DateTimeFormatter TIMESTAMP_FORMATTER =\n" +
                "        DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\");\n" +
                "\n" +
                "    public Map process(Map payload) {\n" +
                "    // timestamp_type 类型为字符串，格式为：2019-07-24 17:06:54.000\n" +
                "    final String timestampStr = (String) record.get(\"timestamp_type\");\n" +
                "    if (timestampStr != null) {\n" +
                "        try {\n" +
                "            // 将字符串转换为日期对象\n" +
                "            final LocalDateTime localDateTime = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);\n" +
                "            // 将时间对象转换为 DATE_FORMATTER 格式的字符串，转化之后的字符串为：2019-07-24\n" +
                "            record.put(\"formatedTime\", DATE_FORMATTER.format(localDateTime));\n" +
                "        } catch (Exception e) {\n" +
                "        e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "    return record;\n" +
                "    }\n" +
                "}";
        System.out.println("这是啥"+ StringUtils.getCharacterPosition(a));

//        String[] b = a.split("process(Map payload) \\{");
        System.out.println(a.substring(StringUtils.getCharacterPosition(a)+1, a.indexOf("return")));
//        for(int i=0;i<b.length;i++){
//            System.out.println(b[i]+"------------------");
//        }

    }


}
