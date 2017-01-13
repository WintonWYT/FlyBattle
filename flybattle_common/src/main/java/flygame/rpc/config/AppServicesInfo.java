package flygame.rpc.config;

public class AppServicesInfo<T> {

	String serviceName;
	String serialization;
	String iface;
	String implClzName;

	T serviceDetailConfig;

	public AppServicesInfo(String serviceName, String serialization, String iface, String className) {
		this.serviceName = serviceName;
		this.implClzName = className;
		this.serialization = serialization;
		this.iface = iface;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getIface() {
		return iface;
	}

	public String getImplClzName() {
		return implClzName;
	}

	public String getSerialization() {
		return serialization;
	}

	public T getServiceConfig() {
		return this.serviceDetailConfig;
	}

	public void setServiceConfig(T detail) {
		this.serviceDetailConfig = detail;
	}

}
