package com.flybattle.battle.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wuyingtan on 2017/1/16.
 */
public class BattleLogger {
    private static Logger logger;

    public static void initLoggers() {
        logger = LoggerFactory.getLogger("BattleServerLogger");
    }


    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public static boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public static void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    public static void debug(String msg) {
        logger.debug(msg);
    }

    public static void debug(String msg, Object... args) {
        logger.debug(msg, args);
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void info(String msg, Object... args) {
        logger.info(msg, args);
    }

    public static void error(String msg) {
        logger.error(msg);
    }

    public static void error(String msg, Object... args) {
        logger.error(msg, args);
    }

    public static void error(String msg, Throwable t) {
        logger.error(msg, t);
    }
}
