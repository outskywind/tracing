package com.dafy.skye.klog.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Administrator on 2016/4/1.
 */
public class KLogUtil {
    private static final Logger log= LoggerFactory.getLogger(KLogUtil.class);
    private volatile static String localhost=null;
    private volatile static String pid;
    public static String getLocalHost() throws SocketException {
        if(localhost!=null){
            return localhost;
        }
        for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback()|| !networkInterface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses();
            while (enumeration.hasMoreElements()) {
                InetAddress inetAddress=enumeration.nextElement();
                if(inetAddress instanceof Inet4Address){
                    String hostAddress=inetAddress.getHostAddress();
                    if(isValidAddress(hostAddress)){
                        log.debug("Get local host {}",hostAddress);
                        localhost=hostAddress;
                        return localhost;
                    }
                }
            }
        }
        return null;
    }
    public static boolean isValidAddress(String address){
        return !address.equals("0.0.0.0")
                &&!address.contains("localhost")
                &&!address.contains("127.0.0.1");
    }
    public static String getPid(){
        if(pid==null){
            String name = ManagementFactory.getRuntimeMXBean().getName();
            pid = name.split("@")[0];
        }
        return pid;
    }
}
