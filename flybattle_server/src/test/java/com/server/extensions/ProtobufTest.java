package com.server.extensions;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.server.util.TestInfo;

import java.io.IOException;

/**
 * Created by wuyingtan on 2017/1/10.
 */
public class ProtobufTest {
    public static void main(String[] args) throws IOException {
        TestInfo info = new TestInfo();
        Codec codec = ProtobufProxy.create(TestInfo.class);
        byte[] bytes = codec.encode(info);

        TestInfo playerSynInfo1 = (TestInfo) codec.decode(bytes);
        System.out.println("" + playerSynInfo1.yes);
    }
}
