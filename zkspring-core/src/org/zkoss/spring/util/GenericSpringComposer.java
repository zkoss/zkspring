/**
 * 
 */
package org.zkoss.spring.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.lang.Classes;
import org.zkoss.spring.SpringUtil;
import org.zkoss.spring.context.annotation.EventHandler;
import org.zkoss.util.CollectionsX;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zk.ui.util.ComposerExt;

/**
 * @author ashish
 *
 */
public class GenericSpringComposer implements Composer, ComposerExt, EventListener {

	private static final Log log = Log.lookup(GenericSpringComposer.class);
	/** map of individual componet events and associated Events annotation values */
	private Map<String,List<String>> eventsMap = null;

	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.Composer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("super.doAfterCompose()");
		try {
			ZkSpringIntegrationContext.setContextComponent(comp);
			// to trigger zk component injection
			accessFields();
			registerEventHandlers(comp);
		} finally {
			ZkSpringIntegrationContext.clearContextComponent();
		}
	}
	
	/**
	 * accesses individual fields of composer instance to trigger component injection
	 * @throws IllegalAccessException
	 */
	private void accessFields() throws IllegalAccessException {
		Class cls = this.getClass();
		Field[] flds = cls.getDeclaredFields();
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < flds.length; ++j) {
			Field f = flds[j];
			f.setAccessible(true);
			Object o = f.get(this);
			sb.append(o == null ? "" : o.toString());
		}
	}

	protected Object getController() {
		return this;
	}

	/**
	 * sets this composer as event listner to all child events specified 
	 * in Events annotation
	 * @param comp
	 * @param controller
	 */
	private void registerEventHandlers(Component comp) {
		final Method[] metds = getController().getClass().getMethods();
		eventsMap = new HashMap<String, List<String>>();
		for (int i = 0; i < metds.length; i++) {
			final Method md = metds[i];
			String mdname = md.getName();
			String annotationValue = getEventsParameterAnnotation(md);
			processEventsAnnotation(comp, annotationValue, mdname);						
		}
	}

	/**
	 * returns EventHandler annotation value if present on any method parameter
	 * @param md
	 * @return annotationValue if any parameter is annotated with EventHandler annotation returns its value or else returns null
	 */
	private String getEventsParameterAnnotation(Method md) {
		Annotation[] annotations = md.getAnnotations();
		for (int j = 0; j < annotations.length; j++) {
			Annotation a = annotations[j];
			if (a instanceof EventHandler) {
				return ((EventHandler) a).value();
			}
		}
		return null;
	}

	/**
	 * adds this composer as event listner for each event specified in Events annotation value
	 * @param comp
	 * @param annotationValue
	 * @param mdname 
	 */
	private void processEventsAnnotation(Component comp, String annotationValue, String mdname) {
		if (annotationValue == null) {
			return;
		}
		List<String> annotationValueTokens = (List<String>) CollectionsX.parse(new ArrayList<String>(), annotationValue, ',');
		for (String annotationValueToken : annotationValueTokens) {
			String srccompid = annotationValueToken.substring(0, annotationValueToken.indexOf('.'));
			String srcevt  = annotationValueToken.substring(annotationValueToken.indexOf('.') + 1, annotationValueToken.length());
			String eventName = srcevt + "." + srccompid;
			Object o = SpringUtil.getBean(srccompid);
			if(o instanceof Component) {
				Component c = (Component) o;
				List<String> methodNames = eventsMap.get(eventName); 
				if(methodNames == null) {
					methodNames = new ArrayList<String>();
					methodNames.add(mdname);
					eventsMap.put(eventName, methodNames);
				} else {
					methodNames.add(mdname);
					eventsMap.put(eventName, methodNames);
				}
				comp.addEventListener(eventName, this);
				((Component) o).addForward(srcevt, comp, eventName);
			}
		}
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent,
			ComponentInfo compInfo) throws Exception {
		// TODO Auto-generated method stub
		return compInfo;
	}

	@Override
	public void doBeforeComposeChildren(Component comp) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doCatch(Throwable ex) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doFinally() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(Event event) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(event.getName());
		Event evtOrig = org.zkoss.zk.ui.event.Events.getRealOrigin((ForwardEvent) event); 
		List<String> methodNames = eventsMap.get(event.getName());
		for(String methodName : methodNames) {
			Method md = Classes.getAnyMethod(this.getClass(), methodName, new Class[] {Event.class});
			md.invoke(this, event);
		}
	}
	/**
	 * forwards component events to controller methods annotated with Events qualifier
	 * @param comp
	 * @param controller
	 */
	@SuppressWarnings("unchecked")
	public void addForwards(Component comp, Object controller) {
		// TODO Auto-generated method stub
		final Class cls = controller.getClass();
		final Method[] mtds = cls.getMethods();
		for (int j = 0; j < mtds.length; ++j) {
			final Method md = mtds[j];
			Annotation[] annotations = md.getAnnotations();
			for (int k = 0; k < annotations.length; k++) {
					Annotation a = annotations[k];
					if (a instanceof EventHandler) {
						addForward(md, a, comp);
					}
			}
		}
	}
	/**
	 * method to add forward event from child component to parent or composer component
	 * @param md method annotated with EventHandler annotation
	 * @param a EventHandler Annotation on md method
	 * @param comp
	 */
	
	@SuppressWarnings("unchecked")
	public void addForward(Method md, Annotation a, Component comp) {
		String mdname = md.getName();
		Component xcomp = comp;
		String annotationValue = ((EventHandler) a).value();
		List<String> annotationValueTokens = (List<String>) CollectionsX.parse(
				new ArrayList<String>(), annotationValue, ',');
		for (String annotationValueToken : annotationValueTokens) {
			String srccompid = annotationValueToken.substring(0,
					annotationValueToken.indexOf('.'));
			String srcevt = annotationValueToken.substring(annotationValueToken
					.indexOf('.') + 1, annotationValueToken.length());

			// TODO: get component instance from bean manager
			// try EL resolver or check any api/spi interface
			Object srccomp = SpringUtil.getBean(srccompid);
			if (srccomp == null || !(srccomp instanceof Component)) {
				log.debug("Cannot find the associated component to forward event: "
								+ mdname);
			} else {
				((Component) srccomp).addForward(srcevt, xcomp, srcevt + "."
						+ srccompid);
			}
		}
	}

}
