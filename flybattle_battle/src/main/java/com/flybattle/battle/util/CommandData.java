package com.flybattle.battle.util;

import java.lang.reflect.Method;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public class CommandData {
    public final Method method;
    public final Class<?> inputClass;
    public final Class<?> outputClass;

    public CommandData(Method method, Class inputClass, Class outputClass) {
        this.method = method;
        this.inputClass = inputClass;
        this.outputClass = outputClass;
    }
}
