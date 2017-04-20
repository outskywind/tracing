package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;

/**
 * Created by Caedmon on 2017/4/11.
 */
public class DubboBraveHelper {
    public static String getCurrentSpanName(){
        RpcContext rpcContext=RpcContext.getContext();
        return getSpanName(rpcContext);
    }
    public static String getSpanName(RpcContext rpcContext){
        String className = rpcContext.getUrl().getPath();
        String simpleName = className.substring(className.lastIndexOf(".")+1);
        System.out.println("get span name");
        return simpleName+"."+rpcContext.getMethodName();
    }
    public  static  int convertToInt(String address){
        String[] p4 = address.split("\\.");
        int ipInt = 0;
        int part = Integer.valueOf(p4[0]);
        ipInt = ipInt | (part << 24);
        part = Integer.valueOf(p4[1]);
        ipInt = ipInt | (part << 16);
        part = Integer.valueOf(p4[2]);
        ipInt = ipInt | (part << 8);
        part = Integer.valueOf(p4[3]);
        ipInt = ipInt | (part);
        return ipInt;
    }
}
