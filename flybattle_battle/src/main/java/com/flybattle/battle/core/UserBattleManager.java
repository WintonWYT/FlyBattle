package com.flybattle.battle.core;

import com.flybattle.battle.domain.UserBattle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public enum UserBattleManager {
    INSTANCE;
    private Map<Integer, UserBattle> uid2UserBattle = new ConcurrentHashMap<>();

    public void addUserBattle(UserBattle userBattle) {
        uid2UserBattle.put(userBattle.getUid(), userBattle);
    }

    public UserBattle getUserBattle(int uid) {
        return uid2UserBattle.get(uid);
    }

    public void removeUserBattle(int uid) {
        uid2UserBattle.remove(uid);
    }
}
