package com.dafy.skye.component;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CreateFlag;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.EnumSet;


/**
 * Created by quanchengyun on 2017/10/25.
 */

/**
 * since it's running on the yarn by map-reduce application
 * so we just directly use the hdfs file system instead of local disk file system
 */

/**
 * BE AWARE: THIS NEED TO BE RUN BY OOZIE
 */
public class FileHelper {
    private static Logger log = LogBuilder.buildLogger(FileHelper.class);

    private static ThreadLocal<StringBuilder> sbLocal = new ThreadLocal<>();

    public static FileWriter getWriter(String file){
        try{
            File f  = new File(file);
            if(f!=null && f.exists()){
                f.delete();
            }
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            return fw;
        }catch(Exception e){
            log.error("创建文件 {} 出错：",file,e);
        }
        return null;
    }

    /**
     * 获取HDFS 文件系统的输出流
     * 确保删除之前的文件，并创建全新的文件，返回输入流
     * @param dir
     * @param fileName
     * @return
     */
    public static FSDataOutputStream getHDFSWriter(String dir , String fileName){
        FSDataOutputStream outStream = null;
        try{
            FileSystem hdfs = null;
            if(StringUtils.isNotBlank(System.getProperty("oozie.action.conf.xml"))){
                Configuration actionConf = new Configuration(false);
                actionConf.addResource(new Path("file:///", System.getProperty("oozie.action.conf.xml")));
                hdfs = FileSystem.get(actionConf);
                Path f = new Path(dir, fileName);
                hdfs.delete(f,true);
                outStream = hdfs.create(f);
            }else{
                Configuration actionConf = new Configuration();
                hdfs = FileSystem.getLocal(actionConf);
                Path f = new Path(dir, fileName);
                hdfs.delete(f,true);
                outStream = hdfs.create(f,(FsPermission)null,EnumSet.of(CreateFlag.CREATE),1024*1024*4,(short)0,1024*1024*16,(Progressable)null);
            }
            return outStream;
        }catch(Exception e){
            log.error("创建HDFS文件 {} 出错：",fileName,e);
        }
        return null;
    }



    /**
     *hive encode NULL  as string '\\N'
     */
    public static void writeLine(DataOutputStream fs , String delimiter, String... columns) throws Exception{
        StringBuilder sb = sbLocal.get();
        if(sb==null){
            sb = new StringBuilder();
            sbLocal.set(sb);
        }
        try{
            for(String column: columns){
                if(column==null || column.equals("null") || column.equals("NULL")){
                    column = "\\N";
                }
                column = column.replaceAll("\n|\r\n|\r|\\s","");
                sb.append(column).append(delimiter);
            }
            //这里会不会占用内存过高
            sb.deleteCharAt(sb.length()-1);
            sb.append("\n");
            fs.write(sb.toString().getBytes("UTF-8"));
            sb.delete(0,sb.length());
        }catch(Exception e){
            log.error("写hdfs文件失败：{}",columns,e);
            throw e;
        }
    }


}
