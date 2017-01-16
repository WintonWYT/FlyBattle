package flygame.rpc.config;

import flygame.common.config.ConfReaderFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    private static String CONFIG_FILE = "RpcConfig.xml";

    private String registry;

    private int port;
    private String protocol;
    private int numWorkers;
    private int requestTimeoutMillis;
    private List<AppServicesInfo> appServicesInfoList;
    private HashMap<String, AppServicesInfo> AppServicesInfoMap = new HashMap<>();

    private static AppConfig _instance = null;

    private AppConfig() {
    }

    public synchronized static AppConfig instance(String configFile, ClassLoader classLoader) {
        if (_instance == null) {
            _instance = new AppConfig();
            _instance.readConfig(configFile, classLoader);
        }
        return _instance;
    }

    public String getRegistry() {
        return registry;
    }

    public List<AppServicesInfo> getAppServicesInfo() {
        return appServicesInfoList;
    }

    public AppServicesInfo getAppServiceInfo(String serviceName) {
        return AppServicesInfoMap.get(serviceName);
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public int getNumWorkers() {
        return numWorkers;
    }

    public int getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    private void readConfig(String cfgFile, ClassLoader classLoader) {
        Document doc = loadCfgFile(cfgFile, classLoader);
        Element root = doc.getDocumentElement();
        ConfReaderFactory.createConfigReader(root);
        IAppConfigReader appReader = new XmlConfigReader(root);
        port = appReader.getPort();
        protocol = appReader.getProtocol();
        registry = appReader.getRegistry();
        appServicesInfoList = appReader.getServices();
        if (appServicesInfoList != null && appServicesInfoList.size() != 0) {
            appServicesInfoList.forEach(service -> {
                if (StringUtils.isEmpty(service.iface)) {
                    try {
                        service.iface = Class.forName(service.implClzName).getInterfaces()[0].getName();
                    } catch (Exception e) {
                        LOG.error("", e);
                    }
                }
                if (StringUtils.isEmpty(service.serviceName)) {
                    service.serviceName = service.iface;
                }
                AppServicesInfoMap.put(service.serviceName, service);
            });
        }
    }

    private Document loadCfgFile(String cfgFile, ClassLoader classLoader) {
        Document dom = null;

        File f;
        try {
            f = new File(classLoader.getResource(cfgFile).toURI());
            LOG.info(f.getAbsolutePath());
            LOG.info("load config file : " + cfgFile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            dom = db.parse(f);
        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
            LOG.error("Parse xml config file error!", e);
        }
        return dom;
    }

}