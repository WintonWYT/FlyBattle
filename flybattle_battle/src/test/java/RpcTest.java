import flygame.rpc.Iface.ITest;
import flygame.rpc.client.RpcClientFactory;
import org.apache.thrift.TException;

/**
 * Created by wuyingtan on 2017/1/13.
 */
public class RpcTest {
    public static void main(String[] args) {
        ITest.Iface test = RpcClientFactory.newRpcClient(ITest.Iface.class);
        try {
            String fuck = test.test("Fuck");
            System.out.println(fuck);
        } catch (TException e) {
            e.printStackTrace();
        }

    }
}
