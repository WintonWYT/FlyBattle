package flygame.rpc.config;

import org.w3c.dom.Element;

public interface IAppServiceDetailConfigReader {

	public <T> T parse(Element ele);

}
