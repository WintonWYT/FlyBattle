package flygame.rpc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class XmlConfigReader implements IAppConfigReader {

	private static final Logger LOG = LoggerFactory.getLogger(XmlConfigReader.class);

	private static final String v_registry = "registry";
	
	private static final String v_services = "services";
	private static final String v_port = "port";
	private static final String v_protocol = "protocol";

	private static final String v_service = "service";
	private static final String v_serviceName = "serviceName";
	private static final String v_iface = "iface";
	private static final String v_serialization = "serialization";
	private static final String v_implName = "implName";

	private static final String v_serviceDetailConfig = "serviceDetailConfig";
	private static final String v_parseClz = "parseClz";

	public XmlConfigReader(Element ele) {
		this.ele = ele;
	}

	private Element ele;

	public List<AppServicesInfo> getServices() {
		return readServicesConfig(ele);
	}

	private List<AppServicesInfo> readServicesConfig(Element ele) {
		List<AppServicesInfo> servicesList = new ArrayList<AppServicesInfo>();

		Element servicesConfigNode = getElement(ele, v_services);
		NodeList nl = servicesConfigNode.getElementsByTagName(v_service);
		for (int i = 0; i < nl.getLength(); i++) {
			AppServicesInfo info = null;
			Element selem = (Element) nl.item(i);
			String serviceName = selem.getAttribute(v_serviceName);
			String iface = selem.getAttribute(v_iface);
			String serialization = selem.getAttribute(v_serialization);
			String implClassName = selem.getAttribute(v_implName);

			if (implClassName == null || serialization == null) {
				LOG.info(String.format("services config not complete![iface=%s, serviceName=%s, className=%s, serialization=%s]", iface, serviceName,
						implClassName, serialization));
			} else {
				info = new AppServicesInfo(serviceName, serialization, iface, implClassName);
				servicesList.add(info);
			}
			Element detailConfig = getElement(selem, v_serviceDetailConfig);
			if (detailConfig != null) {
				String parseClz = detailConfig.getAttribute(v_parseClz);
				if (parseClz == null) {
					LOG.error("no parseClz for detail config: " + v_serviceName);
					continue;
				}
				IAppServiceDetailConfigReader detailReader;
				try {
					detailReader = (IAppServiceDetailConfigReader) Class.forName(parseClz).newInstance();
					info.setServiceConfig(detailReader.parse(detailConfig));
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					LOG.error("", e);
				}
			}
		}
		return servicesList;
	}

	private Element getElement(Element ele, String nodeName) {
		NodeList nl = ele.getElementsByTagName(nodeName);
		if (nl != null && nl.getLength() > 0) {
			return (Element) nl.item(0);
		} else {
			LOG.info(String.format("config %s not found", nodeName));
			return null;
		}
	}

	@Override
	public int getPort() {
		Element services_ele = getElement(ele, v_services);
		String port = services_ele.getAttribute(v_port);
		return Integer.valueOf(port);
	}

	@Override
	public String getProtocol() {
		Element services_ele = getElement(ele, v_services);
		String protocol = services_ele.getAttribute(v_protocol);
		return protocol;
	}

	@Override
	public String getRegistry() {
		Element registry_ele = getElement(ele, v_registry);
		return registry_ele.getTextContent();
	}

}
