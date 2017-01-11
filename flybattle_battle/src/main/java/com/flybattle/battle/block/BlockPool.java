package com.flybattle.battle.block;

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

    public BlockPool(int length, int weight, int height, int num) {
        this.length = length;
        this.weight = weight;
        this.height = height;
        for (int i = 0; i < num; i++) {
            Vec3 pos = getVec3(length, weight, height);
            EnergyBlock energyBlock = new EnergyBlock(i, pos);
            eBlockList.put(i, energyBlock);
        }
    }

    private Vec3 getVec3(int length, int weight, int height) {
        float x = random.nextInt(length);
        float y = random.nextInt(weight);
        float z = random.nextInt(height);
        return new Vec3(x, y, z);
    }

    public EnergyBlock updateBlock(int eid) {
        Vec3 newPos = getVec3(length, weight, height);
        EnergyBlock energyBlock = eBlockList.get(eid);
        energyBlock.setPos(newPos);
        return energyBlock;
    }

    public List<EnergyBlock> getAllInfo() {
        return (List<EnergyBlock>) eBlockList.values();
    }

}
