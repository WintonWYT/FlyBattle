package com.server.protobuf;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

import java.util.List;

/**
 * Created by wuyingtan on 2016/12/26.
 */
public class PlayerSynInfo {
    @Protobuf(required = true)
    public int uid;
    @Protobuf(required = true)
    public Vec3 pos;
    @Protobuf(required = true)
    public Vec3 dir;
    @Protobuf(fieldType = FieldType.INT32)
    public List<Integer> damage;
//    @Protobuf(fieldType = FieldType.INT32)
//    public Integer speed;
//    @Protobuf(fieldType = FieldType.INT32)
//    public Integer level;
//    @Protobuf(fieldType = FieldType.INT32)
//    public Integer bulletType;
}
