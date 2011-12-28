package com.ettrema.forms;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.security.PermissionChecker;
import com.ettrema.web.security.PermissionRecipient.Role;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class AnnotationFormProcessor implements FormProcessor {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AnnotationFormProcessor.class);
	
	private String PARAM_ACTION_NAME = "_action";
	private final FormProcessor wrapped;
	private final PermissionChecker permissionChecker;
	private final CurrentRequestService currentRequestService;

	public AnnotationFormProcessor(FormProcessor wrapped, PermissionChecker permissionChecker, CurrentRequestService currentRequestService) {
		this.wrapped = wrapped;
		this.permissionChecker = permissionChecker;
		this.currentRequestService = currentRequestService;
	}

	@Override
	public String processForm(CommonTemplated target, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
		System.out.println("annotation processform: " + parameters.get(PARAM_ACTION_NAME));
		if (parameters.containsKey(PARAM_ACTION_NAME)) {
			log.trace("processForm: found action parameter");
			String actionName = parameters.get(PARAM_ACTION_NAME);
			for (Method m : target.getClass().getMethods()) {
				if (m.getName().equals(actionName)) {
					log.trace("processForm: 2: found method with matching name");
					FormAction action = m.getAnnotation(FormAction.class);
					if (action != null) {
						log.trace("processForm: 3: found annotation");
						return performAction(action, m, target, parameters, files);
					} else {
						log.warn("processForm: found an action parameter in POST, and a matching method, but the method does not have a FormAction annotation");
					}
				}
			}
			log.warn("processForm: found an action parameter in POSt, but did not find a method and annotation. Resource class: " + target.getClass() + " expected method name: " + actionName);
		}
		log.info("doing form post with wrapped form processor");
		return wrapped.processForm(target, parameters, files);
	}


	private String performAction(FormAction anno, Method m, CommonTemplated target, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
		Role role = anno.requiredRole();
		Request request = currentRequestService.request();
		Auth auth = request.getAuthorization();
		if (permissionChecker.hasRole(role, target, auth)) {
			Object[] args = buildArgs(target, m, request);
			System.out.println("args: " + args.length);
			try {
				Object o = m.invoke(target, args);
				if( o instanceof String ) {
					return o.toString();
				} else {
					return null;
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			throw new NotAuthorizedException(target);
		}
	}

	private Object[] buildArgs(CommonTemplated target, Method m, Request request) {
		List<Object> args = new ArrayList<Object>();
		int paramNum = 0;
		Annotation[][] paramAnnos = m.getParameterAnnotations();
		for( Class<?> c : m.getParameterTypes()) {
			if( c.isAssignableFrom(Request.class)) {
				args.add(request);
			} else {
				FormParameter paramAnno = getParamAnnotation(paramNum, paramAnnos);
				if( paramAnno != null ) {
					String paramName = paramAnno.name();
					String val = request.getParams().get(paramName);
					args.add(val);
				} else {
					throw new RuntimeException("Unsupported argument type: " + c + " defined in method: " + m.getName() + "  on class: " + target.getClass() + " and FormParameter annotation not present");
				}
			}
			paramNum++;
		}
		if( m.getParameterTypes().length != args.size() ) {
			throw new RuntimeException("argument size mismatch. expected: " + m.getParameterTypes().length + "  got " + args.size());
		}
		Object[] arr = new Object[args.size()];
		args.toArray(arr);
		if( m.getParameterTypes().length != arr.length ) {
			throw new RuntimeException("argument size mismatch2. expected: " + m.getParameterTypes().length + "  got " + arr.length);
		}
		
		return arr;
	}

	private FormParameter getParamAnnotation(int paramNum, Annotation[][] paramAnnos) {
		Annotation[] annos = paramAnnos[paramNum];
		for(Annotation a : annos) {
			if( a instanceof FormParameter) {
				return (FormParameter)a;
			}
		}
		return null;
	}
}
