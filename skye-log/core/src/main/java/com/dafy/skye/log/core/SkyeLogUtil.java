package com.dafy.skye.log.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SkyeLogUtil {
    private static final Logger log = LoggerFactory.getLogger(SkyeLogUtil.class);
    private volatile static String privateIp;
    private volatile static String publicIp;
    private volatile static String pid;

    public static String getPublicIp() {
        if(publicIp != null){
            return publicIp;
        }

        for (InetAddress inetAddress : getAllIpv4Address()) {
            if(!inetAddress.isSiteLocalAddress()) {
                publicIp = inetAddress.getHostAddress();
                return publicIp;
            }
        }

        return null;
    }

    public static String getPrivateIp() {
        if(privateIp != null){
            return privateIp;
        }

        for (InetAddress inetAddress : getAllIpv4Address()) {
            if(inetAddress.isSiteLocalAddress()) {
                privateIp = inetAddress.getHostAddress();
                return privateIp;
            }
        }

        return null;
    }

    private static List<InetAddress> getAllIpv4Address() {
        List<InetAddress> ipList = new ArrayList<>(2);
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()
                        || !networkInterface.isUp()
                        || networkInterface.isVirtual()
                        || networkInterface.getDisplayName().contains("Host-Only")) {
                    continue;
                }
                Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses();
                while (enumeration.hasMoreElements()) {
                    InetAddress inetAddress = enumeration.nextElement();
                    if(inetAddress instanceof Inet4Address){
                        ipList.add(inetAddress);
                    }
                }
            }
        } catch (Exception e) {
            log.error("getAllIpv4Address error!", e);
        }

        return ipList;
    }

    public static String getPid(){
        if(pid==null){
            String name = ManagementFactory.getRuntimeMXBean().getName();
            pid = name.split("@")[0];
        }
        return pid;
    }
}
