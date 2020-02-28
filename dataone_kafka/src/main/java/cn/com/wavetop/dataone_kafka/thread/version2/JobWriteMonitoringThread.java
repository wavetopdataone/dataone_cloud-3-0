package cn.com.wavetop.dataone_kafka.thread.version2;

import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * @Author yongz
 * @Date 2019/12/19、16:41
 */
public class JobWriteMonitoringThread extends Thread {


    private long jobid;
    private String destTable;
    private JdbcTemplate jdbcTemplate;
    private Long fristCount;
    private int sync_range;

    public JobWriteMonitoringThread(long jobid, String destTable, JdbcTemplate jdbcTemplate) {
        this.jobid = jobid;
        this.jdbcTemplate = jdbcTemplate;
        this.destTable = destTable;
    }

    @Override
    public void run() {
        fristCount = jdbcTemplate.queryForObject("select count(*) from " + destTable, Long.class);
        if (fristCount == null){
            fristCount = 0l;
        }
        boolean flag = true;
        while (flag) {

        }
    }
}
