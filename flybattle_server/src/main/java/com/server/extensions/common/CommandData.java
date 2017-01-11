package com.server.extensions.common;

import java.lang.reflect.Method;

/**
 * Created by wuyingtan on 2016/11/25.
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
