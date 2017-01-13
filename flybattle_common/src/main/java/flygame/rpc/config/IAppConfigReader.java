package flygame.rpc.config;

import java.util.List;

public interface IAppConfigReader {
	
	public String getRegistry();

	public int getPort();

	public String getProtocol();

	public List<AppServicesInfo> getServices();
}
