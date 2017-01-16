package flygame.rpc.client;

import com.baitian.rpc.client.Option;
import com.baitian.rpc.client.RpcClientBuilder;
import flygame.rpc.config.RpcConfig;

/**
 * Created by wuyingtan on 2017/1/12.
 */
public class RpcClientFactory {
    public static <T> T newRpcClient(Class<T> clientType) {
        T result = RpcClientBuilder.newClient(RpcConfig.ZOOKEEPER_SERVER, clientType);
        return result;
    }

    public static <T> T newRpcClientWithOptin(Class<T> clientType, Option<?, ?>... options) {
        T result = RpcClientBuilder.newClient(RpcConfig.ZOOKEEPER_SERVER, clientType,
                options);
        return result;
    }
}
