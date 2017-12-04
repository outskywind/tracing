package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.component.DateUtil;
import com.dafy.skye.component.FileHelper;
import com.dafy.skye.component.LogBuilder;
import com.dafy.skye.hadhoop.workflow.basic.RegionHelper;
import com.dafy.skye.mapper.FCustomerMapperStatement;
import com.dafy.skye.mapper.RegionMapperStatement;
import com.dafy.skye.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/23.
 */
/**
 * 导入区域表 regions
 */
public class RegionImport extends  BasicOozieAction{

    private static Logger log = LogBuilder.buildLogger(RegionImport.class);
    private static final String REGION_FILE = "metas/regions";

    //清洗后数据保存的hdfs中间文件名
    private static final String work_fileName = "regions";

    @Override
    protected void postAction() throws Exception{
        super.postAction();
        String date = DateUtil.getDateformat();
        hiveConnector.execute(hiveconn, RegionMapperStatement.backup.replaceAll("\\{date\\}",date));
    }
    @Override
    public Result _doAction(Map configs) {

        DataOutputStream fs = null;
        try{
            InputStreamReader reader  = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(REGION_FILE));
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            Map<String,String> provinceMap = new HashMap<String,String>();

            Map this_config = (Map)configs.get(this.getClass().getName());
            List<String> excludeNames = (List<String>)this_config.get("excludeNames");
            fs = getHDFSWriter();
            if(fs == null){
                return null;
            }
            List<String> specialCityList = new ArrayList<>();
            while((line=br.readLine())!=null){
                line =  StringUtils.strip(line);
                //6位编码
                String code = line.substring(0,6);
                String name = StringUtils.strip(line.substring(6));
                if (code.endsWith("0000")){
                    provinceMap.put(code,name);
                }
                //市,写一行（省直辖的标志也在此包含）
                else if(code.endsWith("00")){
                    //
                    String province = provinceMap.get(code.substring(0,2)+"0000");
                    //直辖市
                    if(name.equals("市辖区")){
                        name = province;
                    }
                    //省直辖县标志，相当于市级
                    else if(RegionHelper.specialCityFlag.contains(name)){
                        //add to the special list
                        //去掉最后2位0
                        specialCityList.add(code.substring(0,code.length()-2));
                        continue;
                    } else if(excludeNames.contains(name)){
                        continue;
                    }
                    FileHelper.writeLine(fs,",",code,province,name);
                } else if(specialCityList.contains(code.substring(0,code.length()-2))){
                    String province = provinceMap.get(code.substring(0,2)+"0000");
                    //省直辖县
                    FileHelper.writeLine(fs,",",code,province,name);
                }
            }
            fs.flush();
            fs.close();
            hiveConnector.execute(hiveconn, RegionMapperStatement.drop_table);
            hiveConnector.execute(hiveconn, RegionMapperStatement.create_table);
            //then laod the data in local file system
            hiveConnector.insertOrUpdate(hiveconn,RegionMapperStatement.load_data, work_dir+"/"+work_fileName);
        }catch(Exception e ){
            log.error("导入区域过程出错：",e);
        }finally {
            try{
                if (fs!=null){
                    fs.close();
                }
            }catch(Exception e){
                log.error("connection close error.",e);
            }
        }
        return null;
    }

    @Override
    String getWork_fileName() {
        return work_fileName;
    }
}
