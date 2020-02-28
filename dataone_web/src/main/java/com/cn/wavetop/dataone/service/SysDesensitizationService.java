package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysDesensitization;

public interface SysDesensitizationService {
   Object addDesensitization(SysDesensitization sysDesensitization);

   Object delDesensitization(SysDesensitization sysDesensitization);
   Object delJobrelaRelated(Long jobId);
}
