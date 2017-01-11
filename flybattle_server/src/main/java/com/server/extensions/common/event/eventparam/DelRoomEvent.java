package com.server.extensions.common.event.eventparam;

import com.server.extensions.common.event.EventBase;
import com.server.extensions.common.event.EventType;

/**
 * Created by wuyingtan on 2016/12/13.
 */
public class DelRoomEvent extends EventBase {

    public DelRoomEvent() {
        super(EventType.DELROOM);
    }
}
