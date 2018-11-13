package com.dafy.skye.alertmanager.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TemplateUtil {

    private static final String TEMPLATE_DIR = "templates";
    private static final String ENCODING = "UTF-8";

    private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_28);

    private static final Map<String, Template> TEMPLATE_MAPPING = new ConcurrentHashMap<>();

    static {
        CFG.setDefaultEncoding(ENCODING);
        CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        CFG.setLogTemplateExceptions(false);
        CFG.setClassLoaderForTemplateLoading(TemplateUtil.class.getClassLoader(), TEMPLATE_DIR);
    }

    private static Template loadTemplate(String templateFileName) {
        try {
            Template template = CFG.getTemplate(templateFileName);
            TEMPLATE_MAPPING.put(templateFileName, template);
            return template;
        } catch (Exception e) {
            log.error("loadTemplate error! templateFileName={}", templateFileName, e);
            return null;
        }
    }

    /**
     * 渲染模板
     *
     * @param templateFileName
     * @param data
     * @return
     */
    public static String renderTemplate(String templateFileName, Object data) {
        Template template = TEMPLATE_MAPPING.get(templateFileName);
        if(template == null) {
            if((template = loadTemplate(templateFileName)) == null) {
                return null;
            }
        }

        StringWriter writer = new StringWriter();
        try {
            template.process(data, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("renderTemplate error! templateFileName={}", templateFileName, e);
            return null;
        }
    }
}
