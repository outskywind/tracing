package com.dafy.skye.conf;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.dafy.base.conf.ConfigWrapper;
import com.dafy.base.conf.DynamicConfig;

import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/9/26.
 */
public class SkyeConfigWrapper extends ConfigWrapper implements Config {

    private String appName ;

    public SkyeConfigWrapper(DynamicConfig delegate, String appName) {
        super(delegate);
        this.appName =appName;
    }

    private String wrapKey(String key){
        return key+"."+appName;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return super.getProperty(key,super.getProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Integer getIntProperty(String key, Integer defaultValue) {
        return super.getIntProperty(key,super.getIntProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Long getLongProperty(String key, Long defaultValue) {
        return super.getLongProperty(key,super.getLongProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Short getShortProperty(String key, Short defaultValue) {
        return super.getShortProperty(key,super.getShortProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Float getFloatProperty(String key, Float defaultValue) {
        return super.getFloatProperty(key,super.getFloatProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Double getDoubleProperty(String key, Double defaultValue) {
        return super.getDoubleProperty(key,super.getDoubleProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Byte getByteProperty(String key, Byte defaultValue) {
        return super.getByteProperty(key,super.getByteProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        return super.getBooleanProperty(key,super.getBooleanProperty(wrapKey(key),defaultValue));
    }

    @Override
    public String[] getArrayProperty(String key, String delimiter, String[] defaultValue) {
        return super.getArrayProperty(key,delimiter,super.getArrayProperty(wrapKey(key),delimiter,defaultValue));
    }

    @Override
    public Date getDateProperty(String key, Date defaultValue) {
        return super.getDateProperty(key,super.getDateProperty(wrapKey(key),defaultValue));
    }

    @Override
    public Date getDateProperty(String key, String format, Date defaultValue) {
        return super.getDateProperty(key,format,super.getDateProperty(wrapKey(key),format,defaultValue));
    }

    @Override
    public Date getDateProperty(String key, String format, Locale locale, Date defaultValue) {
        return super.getDateProperty(key,format,locale,super.getDateProperty(wrapKey(key),format,locale,defaultValue));
    }

    @Override
    public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue) {
        return super.getEnumProperty(key,enumType,super.getEnumProperty(wrapKey(key),enumType,defaultValue));
    }

    @Override
    public long getDurationProperty(String key, long defaultValue) {
        return super.getDurationProperty(key,super.getDurationProperty(wrapKey(key),defaultValue));
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        super.addChangeListener(listener);
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys) {
        super.addChangeListener(listener,interestedKeys);
    }

    @Override
    public Set<String> getPropertyNames() {
        return super.getPropertyNames();
    }








}
