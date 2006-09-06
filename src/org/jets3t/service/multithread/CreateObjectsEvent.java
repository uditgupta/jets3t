/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 * 
 * Copyright 2006 James Murty
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.jets3t.service.multithread;

import org.jets3t.service.model.S3Object;

public class CreateObjectsEvent extends ServiceEvent {
    private S3Object[] objects = null;
    
    private CreateObjectsEvent(int eventCode) {
        super(eventCode);
    }
    

    public static CreateObjectsEvent newErrorEvent(Throwable t) {
        CreateObjectsEvent event = new CreateObjectsEvent(EVENT_ERROR);
        event.setErrorCause(t);
        return event;
    }

    public static CreateObjectsEvent newStartedEvent(ThreadWatcher threadWatcher) {
        CreateObjectsEvent event = new CreateObjectsEvent(EVENT_STARTED);
        event.setThreadWatcher(threadWatcher);
        return event;
    }

    public static CreateObjectsEvent newInProgressEvent(ThreadWatcher threadWatcher, S3Object[] completedObjects) {
        CreateObjectsEvent event = new CreateObjectsEvent(EVENT_IN_PROGRESS);
        event.setThreadWatcher(threadWatcher);
        event.setObjects(completedObjects);
        return event;
    }

    public static CreateObjectsEvent newCompletedEvent() {
        CreateObjectsEvent event = new CreateObjectsEvent(EVENT_COMPLETED);
        return event;
    }
    
    public static CreateObjectsEvent newCancelledEvent(S3Object[] incompletedObjects) {
        CreateObjectsEvent event = new CreateObjectsEvent(EVENT_CANCELLED);
        event.setObjects(incompletedObjects);
        return event;
    }

    private void setObjects(S3Object[] objects) {
        this.objects = objects;
    }
    
    public S3Object[] getCreatedObjects() throws IllegalStateException {
        if (getEventCode() != EVENT_IN_PROGRESS) {
            throw new IllegalStateException("Created Objects are only available from EVENT_IN_PROGRESS events");
        }                
        return objects;
    }
    
    public S3Object[] getCancelledObjects() throws IllegalStateException {
        if (getEventCode() != EVENT_CANCELLED) {
            throw new IllegalStateException("Cancelled Objects are  only available from EVENT_CANCELLED events");
        }                
        return objects;
    }
    	
}