package flygame.common.config;

import flygame.extensions.db.DbGroupConfig;

import java.util.Map;

public interface ConfigReader {
	
	void setConfigElement(Object configElement);
	
	void setPrefixDbNames(String[] names);
	
	Map<String, DbGroupConfig> loadDbConfig();

	/*Map<String, String> loadMcConfig();*/
	
/*	Map<String, String> loadTtConfig();*/

	Map<String,String> loadRedisConfig();
	
	Map<String, String> loadOtherConfig();
	
	Map<String, Integer> loadTablePartUnit();
	
	String loadUcConfig();
	
	String loadDbConnectionString();

	Map<Integer, int[]> loadZoneDbIndexs();
}
