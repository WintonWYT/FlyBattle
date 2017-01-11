package com.flybattle.battle.task;

import com.flybattle.battle.core.BattleCenter;
import com.flybattle.battle.domain.OpCode;
import com.flybattle.battle.server.ChannelManager;
import com.flybattle.battle.util.CommandData;
import com.flybattle.battle.util.CommandHandler;
import com.flybattle.battle.util.ProtobufCoder;
import com.server.protobuf.response.EnterBattleResp;
import com.server.protobuf.PlayerInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by wuyingtan on 2017/1/6.
 */
public class StartTask implements ITask {
    private int opCode;
    private ByteBuf byteBuf;
    private ChannelHandlerContext ctx;

    public StartTask(int opCode, ByteBuf byteBuf, ChannelHandlerContext ctx) {
        this.byteBuf = byteBuf;
        this.opCode = opCode;
        this.ctx = ctx;
    }


    @Override
    public void run() {
        try {
            PlayerInfo playerInfo = (PlayerInfo) ProtobufCoder.decode(opCode, byteBuf);
            CommandData cd = CommandHandler.INSTANCE.getCommandData(opCode);
            EnterBattleResp resp = (EnterBattleResp) cd.method.invoke(BattleCenter.getInstance(), playerInfo);
            playerInfo = resp.myInfo;
            //TODO 需要一个唯一ID？现在这样耦合性比较高
            ChannelManager.INSTANCE.addChannel(playerInfo.uid, ctx);
            ChannelManager.INSTANCE.sendResponse(playerInfo.uid, OpCode.JOIN_ROOM_RESP, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
