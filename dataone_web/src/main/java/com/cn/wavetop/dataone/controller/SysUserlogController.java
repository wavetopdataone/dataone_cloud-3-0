package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysUserlog;
import com.cn.wavetop.dataone.service.SysUserlogService;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/sys_userlog")
public class SysUserlogController  {
    @Autowired
    private SysUserlogService sysUserlogService;

    @ApiOperation(value = "查询所有管理日志",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "查询管理日志")
    @PostMapping("/findSysUserlog")
    public   Object findAllSysUserlog() {
        return sysUserlogService.findAllSysUserlog();
    }

    @ApiOperation(value = "条件查询管理日志",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "条件查询管理日志")
    @PostMapping("/findLog")
    public Object findLog(Long deptId,Long userId,String operation,String startTime,String endTime){

        return sysUserlogService.findLog(deptId,userId,operation,startTime,endTime);
    }

    /**
     * 导出管理日志信息
     * @param deptId
     * @param operation
     * @return
     */
    /**
     * 导出管理日志信息
     * @param deptId
     * @param operation
     * @return
     */
    @ApiOperation(value = "导出管理日志表", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "导出管理日志表")
    @GetMapping("/OutPutUserExcel")
    public void outPutExcel(HttpServletRequest request,HttpServletResponse response, @RequestParam Long deptId, @RequestParam Long userId, @RequestParam String operation, @RequestParam String startTime, @RequestParam String endTime, @RequestParam String loginName, @RequestParam String roleKey, @RequestParam Long dept) throws UnsupportedEncodingException {

        DateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH：mm：ss");//设置日期格式

        //给出导出的文件名为"测试导出的数据表_时间.xls
        List<SysUserlog> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();

        //一、从数据库拿数据
        map = (HashMap<String, Object>) sysUserlogService.OutSysUserlogByOperation(deptId,userId,operation,startTime,endTime,loginName,roleKey,dept);
        list = (List) map.get("data");

        //设置表格
        if (list != null){
            //定义excel的文件的名字
            String fileName = "管理日志" + df.format(new Date()) + ".xlsx";
            /*fileName = URLEncoder.encode(fileName, "UTF-8");*/

            // 定义一个新的工作簿
            XSSFWorkbook wb = new XSSFWorkbook();
            // 创建一个Sheet页
            XSSFSheet sheet = wb.createSheet("UserLog");
            sheet.setDefaultRowHeight((short) (2 * 256));//设置行高
            sheet.setColumnWidth(0, 4000);//设置列宽
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 4000);
            sheet.setColumnWidth(5, 8000);

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
            cell.setCellValue("操作人");

            cell = row.createCell(3);
            cell.setCellValue("操作");

            cell = row.createCell(4);
            cell.setCellValue("对象");

            cell = row.createCell(5);
            cell.setCellValue("详情");


            XSSFRow rows;
            XSSFCell cells;
            //循环后台拿到的数据给所有行每一列设置对应的值
            for (int i = 0; i < list.size(); i++) {
                // 在这个sheet页里创建一行
                rows = sheet.createRow(i + 1);
                // 该行创建一个单元格,在该单元格里设置值
                cells = rows.createCell(0);
                cells.setCellValue(i+1);
                cells = rows.createCell(1);
                cells.setCellValue(ft.format(list.get(i).getCreateDate()));
                cells = rows.createCell(2);
                cells.setCellValue(list.get(i).getUsername());
                cells = rows.createCell(3);
                cells.setCellValue(list.get(i).getOperation());
                cells = rows.createCell(4);
                cells.setCellValue(list.get(i).getUserdept());
                cells = rows.createCell(5);
                cells.setCellValue(list.get(i).getDetail());

            }
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
    }
}
