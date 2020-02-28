package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.ErrorQueueSettings;

/**
 * @Author yongz
 * @Date 2019/10/11、10:30
 */
public interface ErrorQueueSettingsService {


    Object getErrorQueueAll();

    Object getCheckErrorQueueByjobid(long job_id);

    Object addErrorQueue(ErrorQueueSettings errorQueueSettings);

    Object editErrorQueue(ErrorQueueSettings errorQueueSettings);

    Object deleteErrorQueue(long job_id);
}
