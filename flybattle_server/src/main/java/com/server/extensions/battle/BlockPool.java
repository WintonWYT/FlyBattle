package com.server.extensions.battle;

import com.server.extensions.config.GameConfig;
import com.server.protobuf.Vec3;

import java.util.*;

/**
 * Created by wuyingtan on 2017/1/9.
 */
public class BlockPool {
    private Map<Integer, EnergyBlock> eBlockList = new HashMap<>();
    //random类是否可靠
    private Random random = new Random();
    private int length;
    private int weight;
    private int height;
    private int roomId;

    public BlockPool(int roomId) {
        this.length = GameConfig.BLOCK_LENGTH;
        this.weight = GameConfig.BLOCK_WEIGHT;
        this.height = GameConfig.BLOCK_HEIGHT;
        int expNum = GameConfig.BLOCK_EXP_NUM;
        int hpNum = GameConfig.BLOCK_HP_NUM;
        int toolNum = GameConfig.BLOCK_TOOL_NUM;
        int num = GameConfig.BLOCK_NUM;
        this.roomId = roomId;
        for (int i = 0; i < num; i++) {
            Vec3 pos = nextBlockPosition();
            EnergyBlock energyBlock = new EnergyBlock(i, pos);
            if (i < expNum) {
                energyBlock.setType(GameConfig.BLOCK_EXP_CODE);
            } else if (i < expNum + hpNum) {
                energyBlock.setType(GameConfig.BLOCK_HP_CODE);
            } else {
                energyBlock.setType(GameConfig.BLOCK_TOOL_CODE);
            }
            eBlockList.put(i, energyBlock);
        }
    }

    private Vec3 nextBlockPosition() {
        float x = random.nextInt(length);
        float y = random.nextInt(weight);
        float z = random.nextInt(height);
        return new Vec3(x, y, z);
    }

    public synchronized void updateBlock(int eid) {
        EnergyBlock energyBlock = eBlockList.get(eid);
        if (energyBlock.isUsed()) {
            return;
        }
        Vec3 newPos = nextBlockPosition();
        energyBlock.setPos(newPos);
        energyBlock.setIsUsed(true);
        BlockChangeNotice.INSTANCE.noticeBlockChange(roomId, energyBlock);
    }

    public synchronized List<EnergyBlock> getAllBlcok() {
        List<EnergyBlock> result = new ArrayList<>();
        eBlockList.values().stream().filter(block -> !block.isUsed()).forEach(block -> result.add(block));
        return result;
    }

}
