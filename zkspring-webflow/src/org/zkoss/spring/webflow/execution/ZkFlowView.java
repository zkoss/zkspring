/* ZkFlowView.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 12, 2008 13:01:13 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.spring.webflow.execution;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.FluentParserContext;
import org.springframework.binding.mapping.MappingResult;
import org.springframework.binding.mapping.MappingResults;
import org.springframework.binding.mapping.MappingResultsCriteria;
import org.springframework.binding.mapping.impl.DefaultMapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageContextErrors;
import org.springframework.binding.message.MessageResolver;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.View;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.servlet.ServletMvcView;
import org.springframework.webflow.validation.WebFlowMessageCodesResolver;
import org.zkoss.spring.webflow.context.servlet.ZkFlowContextManager;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zkplus.databind.Binding;
import org.zkoss.zkplus.databind.DataBinder;

/**
 * ZK implementation for {@link org.springframework.webflow.execution.View}. Resolve ZK event to Spring Web Flow event.
 * 
 * @author henrichen
 * @since 1.1
 */
public class ZkFlowView extends ServletMvcView {
	private static final Log logger = LogFactory.getLog(ZkFlowView.class);
	private static final MappingResultsCriteria PROPERTY_NOT_FOUND_ERROR = new PropertyNotFoundError();
	private static final MappingResultsCriteria MAPPING_ERROR = new MappingError();
	
	private String eventId;
	private boolean viewErrors;
	private RequestContext requestContext;
	private ExpressionParser expressionParser;
	private MappingResults mappingResults;
	
	private final MessageCodesResolver messageCodeResolver = new WebFlowMessageCodesResolver();
	
	
	public ZkFlowView(View view, RequestContext context) {
		super(view, context);
		requestContext = context;
	}
	
	public void setExpressionParser(ExpressionParser expressionParser) {
		super.setExpressionParser(expressionParser);
		this.expressionParser = expressionParser;
	}
	
	public void processUserEvent() {
		determineEventId(requestContext);
		if (eventId == null) {
			return;
		}

		TransitionDefinition transition = requestContext.getMatchingTransition(eventId);
		if (shouldBind(transition)) {
			mappingResults = bind();

			if (mappingResults == null) {
				return;
			}
			if (hasMappingErrors(mappingResults)) {
				viewErrors = true;
				addErrorMessages(mappingResults);
			} else {
				if (shouldValidate(transition)) {
					//model is used for validation only!
					Object model = getModelObject();
					if (model == null) {
						return;
					}
					validate(model);
					if (requestContext.getMessageContext().hasErrorMessages()) {
						viewErrors = true;
					}
				}
			}
		}
	}

	public boolean hasFlowEvent() {
		return eventId != null && !viewErrors;
	}

	public Event getFlowEvent() {
		if (!hasFlowEvent()) {
			return null;
		}
		return new Event(this, eventId, requestContext.getRequestParameters().asAttributeMap());
	}

	protected String determineEventId(RequestContext context) {
		eventId = context.getExternalContext().getRequestMap().getString("action");
		return eventId;
	}

	private MappingResults bind() {
		final Execution exec = Executions.getCurrent();
		final Component self = ZkFlowContextManager.getSelf(exec);
		if (self != null) {
			final DataBinder binder = (DataBinder) self.getAttributeOrFellow("binder", true);
			if (binder != null) {
				final Collection bindings = binder.getAllBindings();
				if (!bindings.isEmpty()) {
					final RequestContext srcRequestContext = 
						ZkFlowContextManager.getFlowRequestContext(exec);
					if (srcRequestContext != null) {
						if (logger.isDebugEnabled()) {
							logger.debug("Setting up view->model mappings");
						}
						DefaultMapper mapper = new DefaultMapper();
						if (addModelBindingMappings(mapper, bindings, srcRequestContext, requestContext)) {
							return mapper.map(srcRequestContext, requestContext);
						}
					}
				}
			}
		}
		return null;
	}
	
	private boolean addModelBindingMappings(DefaultMapper mapper, Collection bindings, RequestContext src, RequestContext target) {
		boolean everMapping = false;
		for(final Iterator it = bindings.iterator(); it.hasNext();) {
			final Binding binding = (Binding)it.next();
			if (binding.isSavable()) {
				final String expr = (String) binding.getExpression();
				addMapping(mapper, expr);
				everMapping = true;
			}
		}
		return everMapping;
	}
	
	private void addMapping(DefaultMapper mapper, String expressionString) {
		Expression expression = expressionParser.parseExpression(expressionString,
				new FluentParserContext().evaluate(RequestContext.class));
		DefaultMapping mapping = new DefaultMapping(expression, expression);
		mapping.setRequired(false);
		if (logger.isDebugEnabled()) {
			logger.debug("Adding mapping for expression '" + expressionString + "'");
		}
		mapper.addMapping(mapping);
	}

	private boolean shouldBind(TransitionDefinition transition) {
		if (transition == null) {
			return true;
		}
		return transition.getAttributes().getBoolean("bind", Boolean.TRUE).booleanValue();
	}
	
	private boolean hasMappingErrors(MappingResults results) {
		return results.hasErrorResults() && !onlyPropertyNotFoundErrorsPresent(results);
	}

	private boolean onlyPropertyNotFoundErrorsPresent(MappingResults results) {
		return results.getResults(PROPERTY_NOT_FOUND_ERROR).size() == mappingResults.getErrorResults().size();
	}

	private void addErrorMessages(MappingResults results) {
		List errors = results.getResults(MAPPING_ERROR);
		for (Iterator it = errors.iterator(); it.hasNext();) {
			MappingResult error = (MappingResult) it.next();
			requestContext.getMessageContext().addMessage(createMessageResolver(error));
		}
	}

	private MessageResolver createMessageResolver(MappingResult error) {
		String field = error.getMapping().getTargetExpression().getExpressionString();
		String errorCode = error.getCode();
		String propertyErrorCode = new StringBuffer().append(getModelExpression().getExpressionString()).append('.')
				.append(field).append('.').append(errorCode).toString();
		return new MessageBuilder().error().source(field).code(propertyErrorCode).code(errorCode).resolvableArg(field)
				.defaultText(errorCode + " on " + field).build();
	}

	private boolean shouldValidate(TransitionDefinition transition) {
		if (transition == null) {
			return true;
		}
		return transition.getAttributes().getBoolean("validate", Boolean.TRUE).booleanValue();
	}

	private void validate(Object model) {
		String validateMethodName = "validate" + StringUtils.capitalize(requestContext.getCurrentState().getId());
		Method validateMethod = ReflectionUtils.findMethod(model.getClass(), validateMethodName,
				new Class[] { MessageContext.class });
		if (validateMethod != null) {
			ReflectionUtils.invokeMethod(validateMethod, model, new Object[] { requestContext.getMessageContext() });
		}
		BeanFactory beanFactory = requestContext.getActiveFlow().getApplicationContext();
		if (beanFactory != null) {
			String validatorName = getModelExpression().getExpressionString() + "Validator";
			if (beanFactory.containsBean(validatorName)) {
				Object validator = beanFactory.getBean(validatorName);
				validateMethod = ReflectionUtils.findMethod(validator.getClass(), validateMethodName, new Class[] {
						model.getClass(), MessageContext.class });
				if (validateMethod != null) {
					ReflectionUtils.invokeMethod(validateMethod, validator, new Object[] { model,
							requestContext.getMessageContext() });
				} else {
					validateMethod = ReflectionUtils.findMethod(validator.getClass(), validateMethodName, new Class[] {
							model.getClass(), Errors.class });
					if (validateMethod != null) {
						ReflectionUtils.invokeMethod(validateMethod, validator, new Object[] { model,
								new MessageContextErrors(requestContext.getMessageContext(), validatorName, validator, expressionParser, messageCodeResolver, mappingResults) });
					}
				}
			}
		}
	}
	
	private Object getModelObject() {
		Expression model = getModelExpression();
		if (model != null) {
			return model.getValue(requestContext);
		} else {
			return null;
		}
	}

	private Expression getModelExpression() {
		return (Expression) requestContext.getCurrentState().getAttributes().get("model");
	}

	private static class PropertyNotFoundError implements MappingResultsCriteria {
		public boolean test(MappingResult result) {
			return result.isError() && "propertyNotFound".equals(result.getCode());
		}
	}

	private static class MappingError implements MappingResultsCriteria {
		public boolean test(MappingResult result) {
			return result.isError() && !PROPERTY_NOT_FOUND_ERROR.test(result);
		}
	}
}
