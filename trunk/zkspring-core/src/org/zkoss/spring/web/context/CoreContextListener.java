/* CoreContextListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Feb 26, 2011 2:57:10 PM, Created by ashish
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

package org.zkoss.spring.web.context;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @deprecated
 * Pre-processes Spring beans for possible auto-wiring of ZK components.<br/>
 * If initial parameter 'use-urlclassloader' is true, it uses {@link java.net.URLClassLoader} to find classpath.
 * Otherwise, it searches them by resource containing MANIFEST.MF.
 * @author ashish, Hawk
 *
 */
public class CoreContextListener implements ServletContextListener {

	private final Log logger = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent sce) {
		preprocessSpringBeansForZKComponentinjection(sce);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent sce) {

	}
	/**
	 * Scan all @Component annotations skip org.zkoss.* org.springframework.*
	 * java.* pkgs Generate @Configuration classes per @Component class
	 * containing methods for each ZK component injection point
	 * @param sce 
	 */
	private void preprocessSpringBeansForZKComponentinjection(ServletContextEvent sce) {
		try {
			String webInf = sce.getServletContext().getRealPath("/WEB-INF/classes");
	
			ClassPool cp = ClassPool.getDefault();
			cp.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
			cp.importPackage("java.lang");
			cp.importPackage("org.zkoss.zul");
			cp.importPackage("org.zkoss.zk.ui");
			cp.importPackage("org.zkoss.spring.util");
			cp.importPackage("org.springframework.context.annotation");
			CtClass mainClass = getZKComponentBeanMethodsClass(cp);
			
			final Set<URL> urlSet = new HashSet<URL>();
			if ("true".equalsIgnoreCase(sce.getServletContext().getInitParameter("use-urlclassloader"))){
				urlSet.addAll(getUrlsForCurrentClasspath());
			}else{
				urlSet.addAll(getClasspathUrlsByManifest());
				//above procedure doesn't ensure to obtain 'WEB-INF/classes'
				urlSet.add(ClasspathHelper.getUrlForServletContextClasses(sce.getServletContext()));
			}
			Reflections reflections = new Reflections(
					new ConfigurationBuilder().setUrls(urlSet)
					.setScanners(new FieldAnnotationsScanner()));
			
			Set<Field> fields = reflections.getFieldsAnnotatedWith(Autowired.class);
			int methodCounter = 0;
			for (Iterator<Field> iterator2 = fields.iterator(); iterator2.hasNext();) {
				Field mField = (Field) iterator2.next();
				CtClass cls = cp.get(mField.getType().getName());
				String pckgName = cls.getPackageName();
				if (Component.class.isAssignableFrom(mField.getType()) && !pckgName.endsWith("zul.api")) {
//					System.out.println("Adding @Bean method for " + mField.getName() + " of type:" + mField.getType().getName());
					// add a unique method name with @Bean("componentid")
					methodCounter = addBeanMethod(mainClass, mField, methodCounter);
				} 
			}
			mainClass.writeFile(webInf);
			mainClass.toClass(Thread.currentThread().getContextClassLoader(),this.getClass().getProtectionDomain());
			
		} catch(Throwable e) {
			logger.error(this.getClass().getName()+" is deprecated after zkspring 3.1, please do not use it.");
		}
	}
	
	/**
	 * Adds a @Bean annotated method to ZKComponentBeanMethods @Configuration class
	 * @param mainClass
	 * @param mField
	 * @param methodCounter 
	 * @return 
	 * @throws CannotCompileException 
	 */
	private int addBeanMethod(CtClass mainClass, Field mField, int methodCounter) throws CannotCompileException {

		CtMethod mnew = null;
		String beanType = mField.getType().getName();
		String beanName = mField.getName();
		StringBuilder sb = new StringBuilder();
		sb.append("public ");
		sb.append(beanType);
		sb.append(" m");
		sb.append(methodCounter);
		sb.append("() {");
		sb.append(beanType + " f = null;");
//		sb.append("System.out.println(\"Generating:" + beanName + "\");");
		sb.append("Component c = ZkSpringIntegrationContext.getContextComponent();");
		sb.append("if(c == null) {");
//		sb.append("return new " + beanType + "();} else {");
		sb.append("return null;} else {");
		sb.append("try {");		
		
		sb.append("f = (" + beanType + ")c.getFellow(\""
				+ beanName + "\");");
//		sb.append("System.out.println(\"Got fellow:" + beanName + "\");");
		sb.append("}catch(ComponentNotFoundException e) {throw new RuntimeException(e.getMessage());}}");
		
		sb.append("return f;");
		sb.append("}");
		mnew = CtNewMethod.make(sb.toString(), mainClass);

		ConstPool cp = mnew.getMethodInfo().getConstPool();
		AnnotationsAttribute attr1 = new AnnotationsAttribute(cp,
				AnnotationsAttribute.visibleTag);
		javassist.bytecode.annotation.Annotation beanAnnotation = new javassist.bytecode.annotation.Annotation(
				"org.springframework.context.annotation.Bean", cp);
		ArrayMemberValue a = new ArrayMemberValue(cp);
		MemberValue[] m = new MemberValue[]{new StringMemberValue(beanName, cp)};
		a.setValue(m);
		beanAnnotation.addMemberValue("name", a);
		javassist.bytecode.annotation.Annotation lazyAnnotation = new javassist.bytecode.annotation.Annotation(
				"org.springframework.context.annotation.Lazy", cp);
		lazyAnnotation.addMemberValue("value", new BooleanMemberValue(true, cp));
		javassist.bytecode.annotation.Annotation scopeAnnotation = new javassist.bytecode.annotation.Annotation(
				"org.springframework.context.annotation.Scope", cp);
		EnumMemberValue enumValue = new EnumMemberValue(cp);
		enumValue.setType("org.springframework.context.annotation.ScopedProxyMode");
		enumValue.setValue("TARGET_CLASS");
		scopeAnnotation.addMemberValue("proxyMode", enumValue);
		scopeAnnotation.addMemberValue("value", new StringMemberValue("idspace", cp));
		attr1.addAnnotation(beanAnnotation);
		attr1.addAnnotation(lazyAnnotation);
		attr1.addAnnotation(scopeAnnotation);
		mnew.getMethodInfo().addAttribute(attr1);
		mnew.getMethodInfo().addAttribute(attr1);
		mnew.getMethodInfo().addAttribute(attr1);
		mainClass.addMethod(mnew);
		methodCounter += 1;
		return methodCounter;
	}


	private CtClass getZKComponentBeanMethodsClass(ClassPool cp) {
		CtClass mainClass = cp.makeClass("org.zkoss.spring.beans.zkcomponents.ZKComponentBeanMethods");
		ClassFile cFile = mainClass.getClassFile();
		ConstPool cPool = cFile.getConstPool();
		AnnotationsAttribute attr = new AnnotationsAttribute(cPool,
				AnnotationsAttribute.visibleTag);
		javassist.bytecode.annotation.Annotation configurationAnnotation = new javassist.bytecode.annotation.Annotation(
				"org.springframework.context.annotation.Configuration", cPool);
		attr.addAnnotation(configurationAnnotation);
		cFile.addAttribute(attr);
		return mainClass;
	}


	/**
	 * returns list of URL paths to application jar files in WEB-INF/lib and WEB-INF/classes
	 * @deprecated the class loader provided by the container might not be an instance of URLClassLoader. 
	 * In that case, we can't obtains those URLs as expected.
	 * @return List
	 */
    private List<URL> getUrlsForCurrentClasspath() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        //is URLClassLoader?
        if (loader instanceof URLClassLoader) {
            return ImmutableList.of(((URLClassLoader) loader).getURLs());
        }

        List<URL> urls = Lists.newArrayList();

        //get from java.class.path
        String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {

            for (String path : javaClassPath.split(File.pathSeparator)) {
                try {
                    urls.add(new File(path).toURI().toURL());
                } catch (Exception e) {
                    throw new ReflectionsException("could not create url from " + path, e);
                }
            }
        }
        return urls;
    }

    /**
	 * returns set of URL paths of all classpath, also considering EAR case.
	 * @return Set
	 */ 
    private Set<URL> getClasspathUrlsByManifest(){
    	ClassLoader loader = Thread.currentThread().getContextClassLoader();
    	Set<URL> urls = new HashSet<URL>(); 
    	try {
			Enumeration<URL> r = loader.getResources("META-INF/MANIFEST.MF");
			while(r.hasMoreElements()){
				URL u = r.nextElement();
				String urlStr = u.toString();
				if("file".equalsIgnoreCase(u.getProtocol())){
					urlStr = urlStr.substring(0,urlStr.length()-20);
				}else if("jar".equalsIgnoreCase(u.getProtocol())){
					urlStr = urlStr.substring(4,urlStr.lastIndexOf("!"));//trim jar: and!
				}else if("zip".equalsIgnoreCase(u.getProtocol())){
					urlStr = urlStr.substring(4,urlStr.lastIndexOf("!"));//trim zip: and!
				}else{
					//ignore
					continue;
				}
				
				u = null;
				try{
					u = new URL(urlStr);
				}catch(MalformedURLException x){
					try{
						u = new URL("file:/"+urlStr);
					}catch(MalformedURLException ex){
						logger.info("Malformed: file:/"+urlStr);
					}
				}
				if(u != null){
					urls.add(u);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return urls;
    }
}
