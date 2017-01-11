package com.server.extensions.common.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wuyingtan on 2016/12/12.
 */
public enum CommonEventDispatcher {
    INSTANCE;
    private Map<EventType, List<IEventHandler>> eventMap;
    private AsyncEventRunner<Long, EventBase> asyncEventRunner;

    CommonEventDispatcher() {
        eventMap = new HashMap<>();
        for (EventType type : EventType.values()) {
            eventMap.put(type, new CopyOnWriteArrayList<>());
        }
        asyncEventRunner = new AsyncEventRunner<>(10, 1000, "异步事件处理", new processor());

    }

    public void register(EventType eventType, EventJoin eventJoin) {
        List<IEventHandler> eventJoinList = eventMap.get(eventType);
        if (!eventJoinList.contains(eventJoin)) {
            eventJoinList.add(eventJoin);
        }

    }

    public void dispatcher(EventBase e) {
        if (e.isAsync()) {
            handleEvent(e);
        } else {
            asyncEventRunner.accept(Long.valueOf(1), e);
        }
    }

    private void handleEvent(EventBase e) {
        List<IEventHandler> eventList = eventMap.get(e.getValue());
        if (eventList != null && eventList.size() > 0) {
            eventList.forEach(handler -> handler.handleEvent(e));
        }
    }

    private class processor implements IProcessor<Long, EventBase> {

        @Override
        public void process(Long K, EventBase eventBase) {
            handleEvent(eventBase);
        }
    }
}
