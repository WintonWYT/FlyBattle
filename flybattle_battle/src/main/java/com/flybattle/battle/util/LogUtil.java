package com.flybattle.battle.util;

import com.baitian.mobileserver.config.ConfigData;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;

/**
 * Created by wuyingtan on 2017/1/16.
 */
public class LogUtil {
    public static void initLogger() {
        try {
            System.out.print("init logger ...\n");
            loadLog4jConfig();
            BattleLogger.initLoggers();
            System.out.println("init ok.\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void loadLog4jConfig() throws Exception {

        String log4jXml = "logConfig.xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource(log4jXml);
        String path = url.getPath();
        DOMConfigurator.configure(getLogConfig(path));
    }

    private static Element getLogConfig(String log4jXml) throws Exception {
        // 通过运行参数注入的PATH_SHUTDOWN_LOG来拼接log输出的实际路径
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(log4jXml);
        Element root = dom.getDocumentElement();
        if (ConfigData.PATH_LOG.isEmpty()) {
            return root;
        }

        NodeList appenderList = root.getElementsByTagName("appender");
        for (int i = 0; i < appenderList.getLength(); i++) {
            Element appender = (Element) appenderList.item(i);
            NodeList paramList = appender.getElementsByTagName("param");
            for (int j = 0; j < paramList.getLength(); j++) {
                Element param = (Element) paramList.item(j);
                String name = param.getAttribute("name");
                if ("File".equals(name)) {
                    String filePath = param.getAttribute("value");
                    param.setAttribute("value", String.format("%s/%s", ConfigData.PATH_LOG, filePath));
                }
            }
        }
        return root;
    }
}
