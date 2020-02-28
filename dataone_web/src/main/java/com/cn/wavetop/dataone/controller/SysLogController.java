package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysLog;
import com.cn.wavetop.dataone.entity.SysLoginlog;
import com.cn.wavetop.dataone.service.SysLogService;
import com.cn.wavetop.dataone.service.SysUserlogService;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/sys_log")
public class SysLogController  {
    @Autowired
    private SysLogService sysLogService;

    @ApiOperation(value = "查询所有操作日志",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "查询操作日志")
    @PostMapping("/findSyslog")
    public   Object findSyslog() {
        return sysLogService.findAll();
    }

    @ApiOperation(value = "条件查询操作日志",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "条件查询操作日志")
    @PostMapping("/findLogByCondition")
    public Object findLogByCondition(Long deptId,Long userId,String operation,String startTime,String endTime){

        return sysLogService.findLogByCondition(deptId,userId,operation,startTime,endTime);
    }

    @ApiOperation(value = "导出操作日志表", httpMethod = "GET", protocols = "HTTP",produces="application/octet-stream", notes = "导出操作日志表")
    @GetMapping("/OutPutExcel")
    public void outPutExcel(HttpServletRequest request,HttpServletResponse response, @RequestParam Long deptId, @RequestParam Long userId, @RequestParam String operation, @RequestParam String startTime, @RequestParam String endTime, @RequestParam String loginName, @RequestParam String roleKey, @RequestParam Long dept) throws UnsupportedEncodingException {
        response.setCharacterEncoding("utf-8");
        DateFormat ft = new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH：mm：ss");//设置日期格式
        //给出导出的文件名为"测试导出的数据表_时间.xls
        List<SysLog> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();

        //一、从数据库拿数据
        map = (HashMap<String, Object>) sysLogService.OutSyslogByOperation(deptId,userId,operation,startTime,endTime,loginName,roleKey,dept);
        list = (List) map.get("data");

        //设置表格

            //定义excel的文件的名字
            String fileName = "操作日志" + ft.format(new Date()) + ".xlsx";
            /*fileName = URLEncoder.encode(fileName, "UTF-8");*/

            // 定义一个新的工作簿
            XSSFWorkbook wb = new XSSFWorkbook();
            // 创建一个Sheet页
            XSSFSheet sheet = wb.createSheet("操作日志");
            sheet.setDefaultRowHeight((short) (2 * 256));//设置行高
            sheet.setColumnWidth(0, 6000);//设置列宽
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 6000);
            sheet.setColumnWidth(3, 6000);
            sheet.setColumnWidth(4, 6000);
            sheet.setColumnWidth(5, 6000);
            CellStyle style = wb.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER); // 创建一个居中格式
            XSSFFont font = wb.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 16);
            //获得表格第一行
            XSSFRow row = sheet.createRow(0);
            //根据需要给第一行每一列设置标题
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("序列号");

            cell = row.createCell(1);
            cell.setCellValue("时间");

            cell = row.createCell(2);
            cell.setCellValue("用户名");

            cell = row.createCell(3);
            cell.setCellValue("操作");
            cell = row.createCell(4);
            cell.setCellValue("任务名称");

            cell = row.createCell(5);
            cell.setCellValue("IP");


            XSSFRow rows;
            XSSFCell cells;
            //循环后台拿到的数据给所有行每一列设置对应的值
        if (list != null&&list.size()>0) {
            for (int i = 0; i < list.size(); i++) {
                // 在这个sheet页里创建一行
                rows = sheet.createRow(i + 1);
                // 该行创建一个单元格,在该单元格里设置值
                cells = rows.createCell(0);
                cells.setCellValue(i + 1);
                cells = rows.createCell(1);
                cells.setCellValue(ft.format(list.get(i).getCreateDate()));
                cells = rows.createCell(2);
                cells.setCellValue(list.get(i).getUsername());
                cells = rows.createCell(3);
                cells.setCellValue(list.get(i).getOperation());
                cells = rows.createCell(4);
                cells.setCellValue(list.get(i).getJobName());
                cells = rows.createCell(5);
                cells.setCellValue(list.get(i).getIp());
            }
        }

            try {
                String fileNames = "操作日志"+df.format(new Date())+".xlsx";
                setFileDownloadHeader(wb,request,response,fileNames);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    wb.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    }


        public void setFileDownloadHeader( XSSFWorkbook wb,HttpServletRequest request, HttpServletResponse response, String fileName) throws UnsupportedEncodingException {

            final String userAgent = request.getHeader("USER-AGENT");
                        String finalFileName = null;

                      if ("Edge".equals(userAgent)) {
                              finalFileName = URLEncoder.encode(fileName, "UTF8");
                           } else if ("MSIE".equals(userAgent)) {// IE浏览器
                               finalFileName = URLEncoder.encode(fileName, "UTF8");
                           } else if ("Mozilla".equals(userAgent)) {// google,火狐浏览器
                                finalFileName = new String(fileName.getBytes(), "ISO8859-1");
                            } else {
                                 finalFileName = URLEncoder.encode(fileName, "UTF8");// 其他浏览器
                           }

//            response.setHeader("Content-Disposition", "attachment;filename="+new String(fileName.getBytes("utf-8"), "iso8859-1"));
//
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition",
                                        "attachment;fileName=" + finalFileName);
            ServletOutputStream out = null;

            try {

                out = response.getOutputStream();
                wb.write(out);
                out.flush();
                out.close();
                wb.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }



        }

    /**
     * 定时任务每周一早上六点删除日志,把超过十万条的都删了
     * @return 0 0 21 * * ?    0 0 6 ? * MON
     */
    //@ApiOperation(value = "每周一早上六点删除日志",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "条件查询操作日志")
    @Scheduled(cron = "0 0 6 ? * MON")
    public void deleteLog() {
        sysLogService.deleteLog();
    }

}
