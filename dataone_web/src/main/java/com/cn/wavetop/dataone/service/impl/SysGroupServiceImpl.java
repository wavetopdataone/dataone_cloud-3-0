package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysGroupRespository;
import com.cn.wavetop.dataone.entity.SysFieldrule;
import com.cn.wavetop.dataone.entity.SysGroup;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.service.SysGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Author yongz
 * @Date 2019/10/12、9:10
 */
@Service
public class SysGroupServiceImpl implements SysGroupService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysGroupRespository repository;

    @Override
    public Object getGroupAll() {
        return ToData.builder().status("1").data(repository.findAll()).build();
    }

    @Override
    public Object checkGroupById(long id) {
        if (repository.existsById(id)) {
            Optional<SysGroup> data = repository.findById(Long.valueOf(id));
            Map<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("data", data);
            return map;
        } else {
            return ToData.builder().status("0").message("没有找到").build();

        }
    }
    @Transactional
    @Override
    public Object addGroup(SysGroup sysGroup) {
        if (repository.existsById(sysGroup.getId())) {
            return ToData.builder().status("0").message("已存在").build();
        } else {
            SysGroup save = repository.save(sysGroup);
            HashMap<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("message", "添加成功");
            map.put("data", save);
            return map;
        }
    }
    @Transactional
    @Override
    public Object editGroup(SysGroup sysGroup) {
        HashMap<Object, Object> map = new HashMap();

        long id = sysGroup.getId();

        // 查看该任务是否存在，存在修改更新任务，不存在新建任务
        if (repository.existsById(id )) {
            SysGroup data = repository.findById(id);
            map.put("status", 1);
            map.put("message", "修改成功");
            map.put("data", data);
        } else {
            SysGroup data = repository.save(sysGroup);
            map.put("status", 2);
            map.put("message", "添加成功");
            map.put("data", data);
        }
        return map;
    }
    @Transactional
    @Override
    public Object deleteGroup(Long id) {
        HashMap<Object, Object> map = new HashMap();
        // 查看该任务是否存在，存在删除任务，返回数据给前端
        if (repository.existsById(id)) {
            repository.deleteById(id);
            map.put("status", 1);
            map.put("message", "删除成功");
        } else {
            map.put("status", 0);
            map.put("message", "任务不存在");
        }
        return map;
    }


}
