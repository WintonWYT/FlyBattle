package com.server.extensions.battle;

import com.server.extensions.config.GameConfig;
import com.server.protobuf.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyingtan on 2017/1/9.
 */
public class BlockPool {
    private Map<Integer, EnergyBlock> eBlockList = new ConcurrentHashMap<>();
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
            Vec3 pos = nextBlockPosition(length, weight, height);
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

    private Vec3 nextBlockPosition(int length, int weight, int height) {
        float x = random.nextInt(length);
        float y = random.nextInt(weight);
        float z = random.nextInt(height);
        return new Vec3(x, y, z);
    }

    public void updateBlock(int eid) {
        EnergyBlock energyBlock = eBlockList.get(eid);
        if (energyBlock.isUsed()) {
            return;
        }
        Vec3 newPos = nextBlockPosition(length, weight, height);
        energyBlock.setPos(newPos);
        energyBlock.setIsUsed(true);
        BlockChangeNotice.INSTANCE.noticeBlockChange(roomId, energyBlock);
    }

    public List<EnergyBlock> getAllBlcok() {
        return (List<EnergyBlock>) eBlockList.values();
    }

}
