package com.cn.wavetop.dataone.controller;
import com.cn.wavetop.dataone.dao.ErrorLogRespository;
import com.cn.wavetop.dataone.dao.SysErrorRepository;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.dao.UserlogRespository;
import com.cn.wavetop.dataone.entity.ErrorLog;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.SysLoginlog;
import com.cn.wavetop.dataone.entity.Userlog;
import com.cn.wavetop.dataone.service.ErrorLogService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import io.swagger.annotations.ApiImplicitParam;
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
import java.util.*;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@RestController
@RequestMapping("/errorlog")
public class ErrorLogController {

    @Autowired
    private ErrorLogService service;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private UserlogRespository userlogRespository;
    @Autowired
    private ErrorLogRespository errorLogRespository;

    @ApiOperation(value = "查看全部", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @GetMapping("/errorlog_all")
    public Object errorlog_all() {
        return service.getErrorlogAll();
    }

    @ApiOperation(value = "根据表名，时间查询错误队列", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据表名，时间查询错误队列")
    @PostMapping("/check_errorlog")
    public Object check_errorlog(Long jobId,String tableName,String type,String startTime,String endTime,String content,Integer current,Integer size) {
        return service.getCheckError(jobId,tableName,type,startTime,endTime,content,current,size);
    }
    @ApiOperation(value = "查询错误队列类型", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询错误队列类型")
    @PostMapping("/selType")
    public Object selType() {
        return service.selType();
    }
    @ApiImplicitParam
    @PostMapping("/add_errorlog")
    public Object add_errorlog( @RequestBody ErrorLog errorLog) {

        return service.addErrorlog(errorLog);
    }

    @ApiImplicitParam
    @PostMapping("/edit_errorlog")
    public Object edit_errorlog(@RequestBody ErrorLog errorLog) {
        return service.editErrorlog(errorLog);
    }


    @PostMapping("/delete_errorlog")
    public Object delete_errorlog(Long jobId,String ids) {

        return service.deleteErrorlog( jobId,ids);
    }

    @PostMapping("/reset_errorlog")
    public Object reset_errorlog(Long jobId,String ids) {
        System.out.println(ids);
        return service.resetErrorlog( jobId,ids);
    }
    @ApiOperation(value = "根据任务ID查询错误队列", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据任务ID查询错误队列，表名，错误量")
    @PostMapping("/query_errorlog")
    public Object query_errorlog(Long jobId,Integer current,Integer size) {
        return service.queryErrorlog(jobId,current,size);
    }
    @ApiOperation(value = "根据ID查询具体错误队列", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据ID查询具体错误队列")
    @PostMapping("/selErrorlog")
    public Object selErrorlog(Long id) {
        return service.selErrorlogById(id);
    }

    @ApiOperation(value = "导出错误队列量", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "导出错误队列量")
    @GetMapping("/outErrorlog")
    public void outErrorlog(Long jobId,String ids,String loginName, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{

       String []id=ids.split(",");
        //消息列表
       Optional<SysJobrela> sysJobrela= sysJobrelaRespository.findById(jobId);
       Userlog build2 = Userlog.builder().time(new Date()).user(loginName).jobName(sysJobrela.get().getJobName()).operate(loginName+"导出了错误队列"+sysJobrela.get().getJobName()+"的数据").jobId(jobId).build();
       userlogRespository.save(build2);
        DateFormat ft = new SimpleDateFormat("yyyy-MM-dd ");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH：mm：ss");//设置日期格式

        //给出导出的文件名为"测试导出的数据表_时间.xls
        List<ErrorLog> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();

        //一、从数据库拿数据
//        map = (HashMap<String, Object>) service.getCheckError(jobId,tableName,startTime,endTime);
//        list = (List) map.get("data");
        ErrorLog errorLog=null;
        if(id!=null) {
            for (String idss : id) {
                errorLog = errorLogRespository.findById(Long.valueOf(idss).longValue());
                list.add(errorLog);
            }
        }

        //设置表格
        if (list != null) {
            //定义excel的文件的名字
            String fileName = "错误队列" + df.format(new Date()) + ".xlsx";
            /*fileName = URLEncoder.encode(fileName, "UTF-8");*/

            // 定义一个新的工作簿
            XSSFWorkbook wb = new XSSFWorkbook();
            // 创建一个Sheet页
            XSSFSheet sheet = wb.createSheet("errorLog");
            sheet.setDefaultRowHeight((short) (2 * 256));//设置行高
            sheet.setColumnWidth(0, 4000);//设置列宽
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 6000);

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
            cell.setCellValue("表名");

            cell = row.createCell(3);
            cell.setCellValue("错误类型");

            cell = row.createCell(4);
            cell.setCellValue("原始数据");


            XSSFRow rows;
            XSSFCell cells;
            //循环后台拿到的数据给所有行每一列设置对应的值
            for (int i = 0; i < list.size(); i++) {
                // 在这个sheet页里创建一行
                rows = sheet.createRow(i + 1);
                // 该行创建一个单元格,在该单元格里设置值
                cells = rows.createCell(0);
                cells.setCellValue(i + 1);
                cells = rows.createCell(1);
                cells.setCellValue(ft.format(list.get(i).getOptTime()));
                cells = rows.createCell(2);
                cells.setCellValue(list.get(i).getSourceName());
                cells = rows.createCell(3);
                cells.setCellValue(list.get(i).getOptType());
                cells = rows.createCell(4);
                cells.setCellValue(list.get(i).getContent());

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
