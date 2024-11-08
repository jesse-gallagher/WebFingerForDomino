package org.openntf.webfinger;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.StringUtil;

public enum WebFingerUtil {
	;
	
	public static String stringVal(List<?> vec) {
		if(vec == null || vec.isEmpty()) {
			return null;
		} else {
			return StringUtil.toString(vec.get(0));
		}
	}
	
	public static <T> List<T> findExtensions(final Class<T> extensionClass) {
		return AccessController.doPrivileged((PrivilegedAction<List<T>>)() ->
			ExtensionManager.findServices(null, extensionClass.getClassLoader(), extensionClass.getName(), extensionClass)
		);
	}
}
