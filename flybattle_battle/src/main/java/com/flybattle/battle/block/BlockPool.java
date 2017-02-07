package com.flybattle.battle.block;

import com.flybattle.battle.util.BattlefieldConfig;
import com.server.protobuf.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyingtan on 2017/1/9.
 */
public class BlockPool {
    private Map<Integer, Block> eBlockList = new ConcurrentHashMap<>();
    private Random random = new Random();
    private int length;
    private int weight;
    private int height;
    private int roomId;

    public BlockPool(int roomId) {
        this.roomId = roomId;
        this.length = BattlefieldConfig.BLOCK_LENGTH;
        this.weight = BattlefieldConfig.BLOCK_WEIGHT;
        this.height = BattlefieldConfig.BLOCK_HEIGHT;
        int expNum = BattlefieldConfig.BLOCK_EXP_NUM;
        int hpNum = BattlefieldConfig.BLOCK_HP_NUM;
        int num = BattlefieldConfig.BLOCK_NUM;
        for (int i = 0; i < num; i++) {
            Vec3 pos = nextBlockPosition();
            Block block = new Block(i, pos);
            if (i < expNum) {
                block.setType(BattlefieldConfig.BLOCK_EXP_CODE);
            } else if (i < expNum + hpNum) {
                block.setType(BattlefieldConfig.BLOCK_HP_CODE);
            } else {
                block.setType(BattlefieldConfig.BLOCK_TOOL_CODE);
            }
            eBlockList.put(i, block);
        }
    }

    private Vec3 nextBlockPosition() {
        float x = random.nextInt(length);
        float y = random.nextInt(weight);
        float z = random.nextInt(height);
        return new Vec3(x, y, z);
    }


    public synchronized void updateBlock(int eid) {
        Block block = eBlockList.get(eid);
        if (block.isUsed()) {
            return;
        }
        Vec3 newPos = nextBlockPosition();
        block.setPos(newPos);
        block.setIsUsed(true);
        BlockChangeNotice.INSTANCE.noticeBlockChange(roomId, block);
    }

    public synchronized List<Block> getAllBlock() {
        List<Block> result = new ArrayList<>();
        eBlockList.values().stream().filter(block -> !block.isUsed()).forEach(block -> result.add(block));
        return result;
    }

}
