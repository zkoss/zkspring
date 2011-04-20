/**
 * 
 */
package org.zkoss.spring.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * <p>An abstract composer that you can extend and write intuitive 
 * {@link org.zkoss.spring.context.annotation.EventHandler @EventHandler}("myComponent.onXXX") event handler methods and 
 * auto-wired components in a ZK ZUML page. This class will add 
 * forward condition to myComponent and forward source onXXX event 
 * received by teh source myComponent to the target component method 
 * annotated with (@link EventHandler) annotation. </p>
 * 
 * <p>Notice that since this composer kept references to the components, single
 * instance object cannot be shared by multiple components.</p>
 * 
 * <p> The following is an example. The onClick event received by Button will be 
 * forwarded to target Window myWin and the Textbox component with id name and 
 * Button component with id greetBtn are injected into name and greetBtn fields 
 * respectively (so you can use name and greetBtn variables directly in showGreeting 
 * without problem)
 * 
 * <pre><code>
 * GreetingCtrl.java
 * {@link org.springframework.stereotype.Component @org.springframework.stereotype.Component} ("greetingCtrl")
 * {@link org.springframework.context.annotation.Scope @Scope} ("desktop")
 * public class GreetingCtrl extends GenericSpringComposer {
 *
 * 		{@link org.springframework.beans.factory.annotation.Autowired @Autowired} private Textbox name;
 * 		{@link org.springframework.beans.factory.annotation.Autowired @Autowired} private Button greetBtn;
 *	
 *  	{@link org.zkoss.spring.context.annotation.EventHandler @EventHandler}("greetBtn.onClick")
 * 		public void showGreeting(Event evt) throws WrongValueException, InterruptedException {
 * 			Messagebox.show("Hello " + name.getValue() + "!");
 * 		}
 * }
 * 
 * test.zul
 * 
 * &lt;?variable-resolver class="org.zkoss.zkplus.spring.DelegatingVariableResolver"?&gt;
 * &lt;window id="myWin" apply="${greetingCtrl}"&gt;
 *		&lt;textbox id="name" /&gt;
 *		&lt;button id="greetBtn" label="Greet!" /&gt;
 * &lt;/window&gt;
 *
 * </code></pre> 
 * 
 * @author ashish
 * @since 3.0
 * 
 */
public class GenericSpringComposer implements Composer, ComposerExt, EventListener {

	private static final Log log = Log.lookup(GenericSpringComposer.class);
	
	/** map of individual componet events and associated Events annotation values */
	private Map<String,List<String>> eventsMap = null;
	private static Set _ignoreWires = new HashSet(5);
	static {
		final Class[] clses = new Class[] {
			GenericSpringComposer.class,
			Object.class
		};
		for (int j = 0; j < clses.length; ++j)
			_ignoreWires.add(clses[j].getName());
	}

	private static boolean ignoreFromWire(Class cls) {
		Package pkg;
		return cls != null && (_ignoreWires.contains(cls.getName())
		|| ((pkg = cls.getPackage()) != null && _ignoreWires.contains(pkg.getName())));
	}
	
	/**
	 * Auto-wires ZK Components in controllers and registers event handlers for
	 * 
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
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
		while (cls != null && !ignoreFromWire(cls)) {
			Field[] flds = cls.getDeclaredFields();
			for (int j = 0; j < flds.length; ++j) {
				Field f = flds[j];
				f.setAccessible(true);
				Object o = f.get(this);
				if (o != null)
					o.toString();
			}
			cls = cls.getSuperclass();
		}

		Field[] flds = cls.getSuperclass().getDeclaredFields();
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
			Object srcCompBean = SpringUtil.getBean(srccompid);
			if(srcCompBean == null) {
				srcCompBean  = comp.getAttributeOrFellow(srccompid, true);
				if (srcCompBean == null) {
					Page page = comp.getPage();
					if (page != null)
						srcCompBean = page.getXelVariable(null, null, srccompid, true);
				}
			}
			if(srcCompBean == null || !(srcCompBean instanceof Component))
				continue;
			
			Component c = (Component) srcCompBean;
			List<String> methodNames = eventsMap.get(eventName);
			if (methodNames == null) {
				methodNames = new ArrayList<String>();
				methodNames.add(mdname);
				eventsMap.put(eventName, methodNames);
			} else {
				methodNames.add(mdname);
				eventsMap.put(eventName, methodNames);
			}
			comp.addEventListener(eventName, this);
			((Component) srcCompBean).addForward(srcevt, comp, eventName);
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
	public void onEvent(Event evt) throws Exception {
//		Event evtOrig = org.zkoss.zk.ui.event.Events.getRealOrigin((ForwardEvent) event); 
		List<String> methodNames = eventsMap.get(evt.getName());
		if (methodNames != null) {
			for (String methodName : methodNames) {
				Method mtd = Classes.getAnyMethod(this.getClass(), methodName,
						new Class[] { Event.class });
				if (mtd != null) {
					if (mtd.getParameterTypes().length == 0)
						mtd.invoke(this, null);
					else if (evt instanceof ForwardEvent) { //ForwardEvent
						final Class paramcls = (Class) mtd.getParameterTypes()[0];
						//paramcls is ForwardEvent || Event
						if (ForwardEvent.class.isAssignableFrom(paramcls)
						|| Event.class.equals(paramcls)) { 
							mtd.invoke(this, new Object[] {evt});
						} else {
							do {
								evt = ((ForwardEvent)evt).getOrigin();
							} while(evt instanceof ForwardEvent);
							mtd.invoke(this, new Object[] {evt});
						}
					} else
						mtd.invoke(this, new Object[] {evt});
				}
//				md.invoke(this, event);
			}
		}
	}
}
