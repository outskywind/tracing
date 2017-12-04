package com.dafy.skye;

import com.dafy.skye.component.HiveConnector;
import com.dafy.skye.component.YamlParser;
import com.dafy.skye.hadhoop.workflow.ActionLoader;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;

/**
 * Created by quanchengyun on 2017/10/30.
 */
public class TestHive {


    @Test
    public void test(){


        HashMap configs = YamlParser.parse(ActionLoader.CONFIG,HashMap.class);
        Connection conn = HiveConnector.getInstance().getConnection(configs);

        try{
            PreparedStatement ps = conn.prepareStatement("update test set name='your are coward' where id = 1");
            ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }



    }

}
