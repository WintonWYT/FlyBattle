package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.protobuf.EnergyBlockInfo;
import com.server.protobuf.PlayerInfo;

import java.util.List;

/**
 * Created by wuyingtan on 2016/12/9.
 */
public class EnterBattleResp {
    @Protobuf(required = true)
    public PlayerInfo myInfo;
    @Protobuf//(required = true)
    public List<PlayerInfo> otherInfo;
    @Protobuf//(required = true)
    public List<EnergyBlockInfo> energyBlockInfo;
}
