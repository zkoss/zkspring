/* EventProcessKey.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 2, 2008 9:12:43 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.intercept.zkevent;

/**
 * A Secured ZK Event process. 
 * @author henrichen
 * @since 1.0
 */
public class EventProcessKey {
    private String _path;
    private String _event;

    public EventProcessKey(String path, String event) {
        this._path = path;
        this._event = event;
    }
    
    String getPath() {
        return _path;
    }

    String getEvent() {
        return _event;
    }

    public int hashCode() {
        int code = 31;
        code ^= _path.hashCode();
        
        if (_event != null) {
            code ^= _event.hashCode();
        }

        return code;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof EventProcessKey)) {
            return false;
        }

        final EventProcessKey key = (EventProcessKey) obj;

        if (!_path.equals(key._path)) {
            return false;
        }
        
        if (_event == null) {
        	return key._event == null;
        }

        return _event.equals(key._event);        
    }
}
