package com.start;

import com.baidu.bjf.remoting.protobuf.ProtobufIDLGenerator;
import com.server.protobuf.PlayerSynInfo;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class ProtobufGenerator {
    public static String getProtoFormClass(Class clazz) {
        return ProtobufIDLGenerator.getIDL(clazz);
    }

    public static void main(String[] args) {
        System.out.println(getProtoFormClass(PlayerSynInfo.class));
//        System.out.println(getProtoFormClass(BattleReq.class));
//        System.out.println(getProtoFormClass(SignUpReq.class));
//        System.out.println(getProtoFormClass(LoginResp.class));
//        System.out.println(getProtoFormClass(SignUpResp.class));
//        System.out.println(getProtoFormClass(EnterBattleResp.class));
    }
}
