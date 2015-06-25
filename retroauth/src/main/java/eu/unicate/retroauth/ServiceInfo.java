package eu.unicate.retroauth;

import java.lang.reflect.Method;
import java.util.Map;

public class ServiceInfo {
	public final Map<Method, AuthRestMethodInfo> methodInfoCache;
	public final String accountType;
	public final String tokenType;

	public ServiceInfo(Map<Method, AuthRestMethodInfo> methodInfoCache, String accountType, String tokenType) {
		this.methodInfoCache = methodInfoCache;
		this.accountType = accountType;
		this.tokenType = tokenType;
	}
}
