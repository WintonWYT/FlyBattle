package com.server.extensions.common.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyingtan on 2016/12/12.
 */
public class EventJoin implements IEventHandler {
    private Map<EventType, Method> eventMap = new HashMap<>();
    private Object handler;

    public EventJoin(Object handler) {
        this.handler = handler;
        Method[] methods = handler.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EventSubscribe.class)) {
                EventSubscribe eventSubscribe = method.getDeclaredAnnotation(EventSubscribe.class);
                EventType[] eventType = eventSubscribe.value();
                if (eventType.length == 0) {
                    eventType = EventType.values();
                }
                for (EventType type : eventType) {
                    eventMap.put(type, method);
                    CommonEventDispatcher.INSTANCE.register(type, this);
                }
            }
        }
    }

    public void handleEvent(EventBase e) {
        Method method = eventMap.get(e.getValue());
        try {
            method.invoke(handler, e);
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        }

    }
}
