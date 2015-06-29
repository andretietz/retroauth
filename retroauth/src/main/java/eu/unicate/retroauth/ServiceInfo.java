package eu.unicate.retroauth;

import java.lang.reflect.Method;
import java.util.Map;

import eu.unicate.retroauth.interceptors.TokenInterceptor;

/**
 * This is a pojo, containing all the usefull information and classes, that are
 * needed to handle the authenticated requests
 */
public class ServiceInfo {
	public final Map<Method, AuthRestMethodInfo> methodInfoCache;
	public final String accountType;
	public final String tokenType;
	public final TokenInterceptor authenticationInterceptor;

	public ServiceInfo(Map<Method, AuthRestMethodInfo> methodInfoCache, String accountType, String tokenType, TokenInterceptor requestInterceptor) {
		this.methodInfoCache = methodInfoCache;
		this.accountType = accountType;
		this.tokenType = tokenType;
		this.authenticationInterceptor = requestInterceptor;
	}
}