package flygame.common.config;

import flygame.common.ApplicationLocal;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Properties;

public class ConfReaderFactory {

	private static ConfigReader configReader = null;

	public static ConfigReader getConfigReader() {
		if (configReader == null) {
			throw new IllegalStateException("call ConfReaderFactory.createConfigReader first !");
		}
		return configReader;
	}

	public static ConfigReader createConfigReader(Object data) {

		ConfigReader reader = null;
		String workDbConnectionString = "";
		String driver = "";
		boolean usePlainText = false;

		// 配置文件为properites类型
		if (data instanceof Properties) {
			Properties configProperties = (Properties) data;
			String applicationName = getAppName(configProperties);
			workDbConnectionString = getWorkDbConnectionString(configProperties);
			driver = getDriver(configProperties);
			usePlainText = getUsePlainText(configProperties);
			reader = new DbConfigReader(applicationName, workDbConnectionString, driver, usePlainText);
		} else if (data instanceof Element) {
			Element docEle = (Element) data;
			NodeList nl = docEle.getElementsByTagName("applicationInfo");
			Element appNode = (Element) nl.item(0);
			String applicationName = getAppName(appNode);
			workDbConnectionString = getWorkDbConnectionString(appNode);
			driver = getDriver(appNode);
			usePlainText = getUsePlainText(appNode);
			reader = new DbConfigReader(applicationName, workDbConnectionString, driver, usePlainText);
		}

		configReader = reader;

		return reader;
	}

	private static String getAppName(Properties configProperties) {
		return configProperties.getProperty("as_app_name");
	}

	private static String getAppName(Element appNode) {
		return getNodeAttributeValue(appNode, "appName", "value");
	}

	private static String getWorkDbConnectionString(Properties configProperties) {
		return configProperties.getProperty("as_main_connection_string");
	}

	private static String getWorkDbConnectionString(Element appNode) {
		return getNodeAttributeValue(appNode, "mainConnectionString", "value");
	}

	private static String getDriver(Properties configProperties) {
		return configProperties.getProperty("as_db_driver");
	}

	private static String getDriver(Element appNode) {
		return getNodeAttributeValue(appNode, "dbDriver", "value");
	}

	private static boolean getUsePlainText(Properties configProperties) {
		String plainText = configProperties.getProperty("as_use_plaintext");
		return StringUtils.isEmpty(plainText) ? false : plainText.toLowerCase().equals("true");
	}

	private static boolean getUsePlainText(Element appNode) {
		String plainText = getNodeAttributeValue(appNode, "usePlainText", "value");
		return StringUtils.isEmpty(plainText) ? false : plainText.toLowerCase().equals("true");
	}

	/**
	 * 在指定的节点下，获取指定的子节点指定的属性的值。如果没找到节点或属性，返回null。
	 *
	 * @param elem
	 * @param nodeName
	 * @param attrName
	 * @return
	 */
	private static String getNodeAttributeValue(Element elem, String nodeName, String attrName) {
		NodeList nl = elem.getElementsByTagName(nodeName);
		if (nl != null && nl.getLength() > 0) {
			Element node = (Element) nl.item(0);
			return node.getAttribute(attrName);
		} else {
			ApplicationLocal.instance().error(String.format("%s / %s not found", nodeName, attrName));
			return null;
		}
	}
}
