package com.dafy.skye.hadhoop.workflow;

/**
 * Created by quanchengyun on 2017/10/23.
 */

import com.dafy.skye.component.HiveConnector;
import com.dafy.skye.component.LogBuilder;
import com.dafy.skye.component.YamlParser;
import com.dafy.skye.hadhoop.workflow.action.OozieAction;
import org.slf4j.Logger;

import java.sql.Connection;
import java.util.HashMap;

/**
 * since the each action in an Oozie workflow is loaded by the specified main class
 * so we need to construct a architecture which load each action by the args
 * //whereis the log file path?
 */
public class ActionLoader {
    private static Logger log = null;
    public static final String CONFIG="config.yml";
    public static final String CONFIG_ACTION="action";
    public static final String CONFIG_HIVE_WORK_DIR = "hdfs.dir";

    public static void main(String[] args)throws Exception{
        if (args==null||args.length==0) throw new Exception("args null ,no actionName specified");
        String actionName = args[0];
        HashMap configs = YamlParser.parse(CONFIG,HashMap.class);
        initLogger(configs);
        //解析参数，获取对应的action
        HashMap actionClazzMap = (HashMap)configs.get(CONFIG_ACTION);
        String actionClazz = (String)actionClazzMap.get(actionName);
        OozieAction action = loadAction(actionClazz);
        //translate the configs to the actual running action
        //we change to  the ${hdfs_dir} which is specified in command option
        if(args.length>=2){
            configs.put(CONFIG_HIVE_WORK_DIR,args[1]);
        }
        action.doAction(configs);

    }


    /**
     * 加载正确的action 并执行
     * do we need a distributed lock here?
     * @param actionName
     * @return
     */
    public static OozieAction loadAction(String actionName) throws Exception {
        return (OozieAction) Class.forName(actionName).newInstance();
    }

    public static void initLogger(HashMap configs){
        HashMap<String,String> logConfig=(HashMap<String,String>)configs.get("log");
        if(logConfig!=null){
            String logfile = checkNull(System.getProperty("log.file"),(String)logConfig.get("file"));
            String pattern =(String)logConfig.get("pattern");
            String fileNamePattern = (String)logConfig.get("fileNamePattern");
            log = LogBuilder.buildLogger(ActionLoader.class,logfile,fileNamePattern,pattern);
        }
    }


    public static <T> T checkNull(T checkValue, T defaultValue){
        return (checkValue==null||checkValue=="")?defaultValue:checkValue;
    }
}
