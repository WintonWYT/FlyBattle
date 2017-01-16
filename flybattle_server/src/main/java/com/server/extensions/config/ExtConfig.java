package com.server.extensions.config;

import com.baitian.mobileserver.config.ConfigData;
import com.baitian.mobileserver.logger.ServerLogger;
import com.baitian.mobileserver.util.ServerConfig;
import flygame.common.config.ConfReaderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class ExtConfig {

    private static ExtConfig instance;
    private static String exConfigFile = "exConfig.xml";
    private static String wsConfigFile = "wsConfig.xml";
    
    private ServerConfig serverConfig;
    private int zoneId;

    private ExtConfig() {
        wsConfigFile = new StringBuilder(ConfigData.PATH_CONFIG_XML).append("/").append(wsConfigFile).toString();
        exConfigFile = new StringBuilder(ConfigData.PATH_CONFIG_XML).append("/").append(exConfigFile).toString();
    }

    public static ExtConfig instance() {
        if (instance == null) {
            instance = new ExtConfig();
        }
        return instance;
    }

    public void init() throws Exception {
//        this.zoneId = Integer.parseInt(args[0]);
        Element extElement = loadExConfigElement();
        ConfReaderFactory.createConfigReader(extElement); // init DbManager config
        this.loadServerConfig();
    }


    private void loadServerConfig() {
        Document wsConfig = loadCfgFile(wsConfigFile);
        Element element = (Element) wsConfig.getElementsByTagName("ServerSetup").item(0);
        String ip = getTagValue(element, "ServerIP");
        int port = Integer.parseInt(getTagValue(element, "ServerPort"));
        serverConfig = new ServerConfig(ip, port);
        serverConfig.setMaxUserCount(Integer.parseInt(getTagValue(element, "MaxUserCount")));
        serverConfig.setEnableMsgLengthDebug(Boolean.parseBoolean(getTagValue(element, "EnableMsgLengthDebug")));
        serverConfig.setEnableProfile(Boolean.parseBoolean(getTagValue(element, "EnableProfile")));
        serverConfig.setInMsgWorkerThreads(Integer.parseInt(getTagValue(element, "InMsgWorkerThreads")));
        serverConfig.setOutMsgWorkerThreads(Integer.parseInt(getTagValue(element, "OutMsgWorkerThreads")));
        serverConfig.setScheduledExecutorThreads(Integer.parseInt(getTagValue(element, "ScheduledExecutorThreads")));
    }

    private String getTagValue(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private Element loadExConfigElement() {
        Document rootDoc = loadCfgFile(exConfigFile);
        return rootDoc.getDocumentElement();
    }

    private Document loadCfgFile(String cfgFile) {
        Document dom = null;
        try {
            ServerLogger.info("init config file : " + cfgFile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(cfgFile);
        } catch (ParserConfigurationException | SAXException pce) {
            ServerLogger.error("Parse xml config file error.", pce);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            ServerLogger.error("Can't open configuration file.", ioe);
        }
        return dom;
    }

    private int getInt(String str, int defaultValue) {
        if (str != null && str.length() > 0) {
            return Integer.parseInt(str);
        }
        return defaultValue;
    }

    public String getCommonConfigPath() {
        return ConfigData.PATH_CONFIG_XML;
    }


    public int getZoneId() {
        return zoneId;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

}
