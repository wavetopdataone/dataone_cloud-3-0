package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.MailnotifySettings;

/**
 * @Author yongz
 * @Date 2019/10/11、13:24
 */
public interface MailnotifySettingsService {
    Object getMailnotifyAll();

    Object getCheckMailnotifyByJobId(long job_id);

    Object addMailnotify(MailnotifySettings mailnotifySettings);

    Object editMailnotify(MailnotifySettings mailnotifySettings);

    Object deleteErrorlog(long job_id);
}
