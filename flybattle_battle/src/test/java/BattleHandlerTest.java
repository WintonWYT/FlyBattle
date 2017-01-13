import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.flybattle.battle.domain.OpCode;
import com.server.protobuf.PlayerSynInfo;
import com.server.protobuf.response.EnterBattleResp;
import com.server.protobuf.response.SyncResp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * Created by wuyingtan on 2017/1/9.
 */
public class BattleHandlerTest {
    public static void handleActive(ChannelHandlerContext ctx) {
        System.out.println("welcome" + ctx.channel().localAddress());
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(4);
        buf.writeInt(301);
        try {
            //ProtobufCoder.encode(info, buf);
            ctx.writeAndFlush(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void handleRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        int opcode = buf.readInt();
        if (OpCode.JOIN_ROOM_RESP == opcode) {
            Codec code = ProtobufProxy.create(EnterBattleResp.class);
            int size = buf.readInt();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            try {
                EnterBattleResp resp = (EnterBattleResp) code.decode(bytes);
                System.out.println(resp.myInfo.roomId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("ready to start battle");
        } else if (OpCode.SYNC_BATTLE_RESP == opcode) {
            Codec code = ProtobufProxy.create(SyncResp.class);
            int size = buf.readInt();
            if (size != 0) {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                try {
                    SyncResp resp = (SyncResp) code.decode(bytes);
                    for (PlayerSynInfo playerSynInfo : resp.playerSynInfos) {
                        // System.out.println("uid:" + playerSynInfo.uid + " ,speed:" + playerSynInfo.speed);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new Thread(() -> {

            BattleServerTest test = new BattleServerTest();
            try {
                test.connect(9090, "10.18.20.56");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }
}
