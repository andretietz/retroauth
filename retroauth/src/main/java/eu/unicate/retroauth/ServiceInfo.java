package eu.unicate.retroauth;

import android.app.Activity;

import java.lang.reflect.Method;
import java.util.Map;

public class ServiceInfo {
	public final Activity activity;
	public final Map<Method, AuthRestMethodInfo> methodInfoCache;
	public final String accountType;
	public final String tokenType;

	public ServiceInfo(Activity activity, Map<Method, AuthRestMethodInfo> methodInfoCache, String accountType, String tokenType) {
		this.activity = activity;
		this.methodInfoCache = methodInfoCache;
		this.accountType = accountType;
		this.tokenType = tokenType;
	}
}
