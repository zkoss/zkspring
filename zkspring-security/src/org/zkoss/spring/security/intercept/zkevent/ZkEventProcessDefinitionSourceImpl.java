/* ZkEventProcessDefinitionSourceImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 1, 2008 4:25:22 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.security.intercept.zkevent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.UrlMatcher;
import org.zkoss.spring.security.config.ZkEventProcessDefinitionSourceBeanDefinitionParser;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;

/**
 * Default implementation of {@link ZkEventProcessDefinitionSource}.
 * <p>
 * Stores an ordered map of compiled {@link Component} paths and event name to 
 * <tt>ConfigAttributeDefinition</tt>s and provides Component path and event 
 * matching against the items stored in this map using the configured 
 * <tt>UrlMatcher</tt>.
 * </p>
 * <p>
 * The order of registering the regular expressions using the
 * {@link #addSecureEvent} is very important. The matching is done per
 * the following rules:
 * <ol>
 * <li>The event-specific matches will take precedence over any Component 
 * paths which are registered without an event name.</li>
 * <li>The system will identify the <b>first</b> matching
 * expression for a given Component path. It will not proceed to 
 * evaluate later expressions if a match has already been found.</li>
 * </ol>
 * Accordingly, the most specific expressions should be 
 * registered first, with the most general regular expressions registered last.
 * </p>
 * @author henrichen
 * @see ZkEventProcessDefinitionSourceBeanDefinitionParser
 * @since 1.0
 */
public class ZkEventProcessDefinitionSourceImpl implements ZkEventProcessDefinitionSource {
    protected final Log logger = LogFactory.getLog(getClass());

    private UrlMatcher _pathMatcher;
	private Map _eventMap = new LinkedHashMap();
	
	
	public ZkEventProcessDefinitionSourceImpl(UrlMatcher pathMatcher) {
		_pathMatcher = pathMatcher;
	}
	
    /**
     * Builds the internal request map from the supplied map. The key elements 
     * should be of type {@link EventProcessKey}, which contains a Component path 
     * and an event name (may be null). The path stored in the key will depend 
     * on the type of the supplied UrlMatcher.
     * 
     * @param pathMatcher typically an ant or regular expression matcher.
     * @param requestMap order-preserving map of <RequestKey, ConfigAttributeDefinition>.
     */
    public ZkEventProcessDefinitionSourceImpl(UrlMatcher pathMatcher, LinkedHashMap requestMap) {
        _pathMatcher = pathMatcher;

        for(final Iterator it = requestMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            EventProcessKey key = (EventProcessKey) entry.getKey();
            addSecureEvent(key.getPath(), key.getEvent(), (Collection<ConfigAttribute>) entry.getValue());
        }
    }

	public void addSecureEvent(String path, String event, Collection<ConfigAttribute> attr) {
		Map mapToUse = getPathMapForEvent(event);

		mapToUse.put(_pathMatcher.compile(path), attr);

        if (logger.isDebugEnabled()) {
            logger.debug("Added ZK component path pattern: " + path + "; attributes: " + attr +
                    (event == null ? "" : " for event '" + event + "'"));
        }
	}
	
    /**
     * Return the event specific path map, creating it if it doesn't already exist.
     * @param event onXxx events
     * @return map of Component path patterns to <tt>ConfigAttributeDefinition</tt>s for this method.
     */
    private Map getPathMapForEvent(String event) {
        if (event == null || "*".equals(event)) {
            event = "on*";
        }
        if (!event.startsWith("on")) {
            throw new IllegalArgumentException("Unrecognised event: '" + event + "'");
        }

        Map pathmap = (Map) _eventMap.get(event);

        if (pathmap == null) {
            pathmap = new LinkedHashMap();
            _eventMap.put(event, pathmap);
        }

        return pathmap;
    }

	public Collection<ConfigAttribute> getAttributes(Object object)
		throws IllegalArgumentException {
        if ((object == null) || !this.supports(object.getClass())) {
            throw new IllegalArgumentException("Object must be an Event");
        }

        final Component comp = ((Event) object).getTarget();
        final String path = toPath(comp, new StringBuffer()).toString();
        final String eventnm = ((Event) object).getName();

        return lookupAttributes(path, eventnm);
    }
	
	private StringBuffer toPath(Object comp, StringBuffer sb) {
		if (comp instanceof Page) {
			sb.append("//").append(((Page)comp).getId());
		} else {
			IdSpace spaceOwner = null;
			if (comp instanceof IdSpace) {
				final Component parent = ((Component)comp).getParent();
				spaceOwner = parent == null ? ((Component)comp).getPage() : parent.getSpaceOwner();
			} else if (comp instanceof Component){
				spaceOwner = ((Component)comp).getSpaceOwner();
				if (!(spaceOwner instanceof Component)){
					//ZKSPRING-22 in zk6, space owner might be VirtualIdSpace
					spaceOwner = ((Component)comp).getPage(); 
				}
			} else {
				sb.append("null");
				return sb;
			}
			toPath(spaceOwner, sb);
			sb.append('/').append(((Component)comp).getId());			

		}
		return sb;
	}

	
    /**
     * Performs the actual lookup of the relevant <code>ConfigAttributeDefinition</code> for the specified
     * <code>Event</code>.
     * <p>
     * By default, iterates through the stored Path map and calls the
     * {@link UrlMatcher#pathMatchesUrl(Object pattern, String path)} method until a match is found.
     * <p>
     * Subclasses can override if required to perform any modifications to the path.
     *
     * @param path the Component path to retrieve configuration attributes for
     * @param event the event name
     *
     * @return the <code>ConfigAttributeDefinition</code> that applies to the 
     * specified <code>Event</code> or null if no match is foud
     */
    public Collection<ConfigAttribute> lookupAttributes(String path, String event) {
    	Collection<ConfigAttribute> attributes = null;

        {
	        final Map pathMap = (Map) _eventMap.get(event);
	
	        if (pathMap != null) {
	            attributes = lookupPathInMap(pathMap, path, event);
	        }
        }

        if (attributes == null) {
        	final Map pathMap = (Map) _eventMap.get("on*");
        	if (pathMap != null) {
        		attributes = lookupPathInMap(pathMap, path, event);
        	}
        }

        return attributes;
    }

    private Collection<ConfigAttribute> lookupPathInMap(Map pathMap, String path, String event) {
        for(final Iterator it= pathMap.entrySet().iterator();it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Object pattern = entry.getKey();
            boolean matched = _pathMatcher.pathMatchesUrl(pattern, path);

            if (logger.isDebugEnabled()) {
                logger.debug("Candidate ZK component is: '" + path + "'; event is: '" + event +"'; path pattern is " + pattern + "; matched=" + matched);
            }

            if (matched) {
                return (Collection<ConfigAttribute>) entry.getValue();
            }
        }

        return null;
    }


	public Collection getConfigAttributeDefinitions() {
		final List list = new ArrayList();
		for(final Iterator it=_eventMap.values().iterator();it.hasNext();) {
			final Map pathMap = (Map) it.next();
			list.addAll(pathMap.values());
		}
        return list;
	}

	public boolean supports(Class clazz) {
		return Event.class.isAssignableFrom(clazz);
	}

	public Collection<ConfigAttribute> getAllConfigAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}
