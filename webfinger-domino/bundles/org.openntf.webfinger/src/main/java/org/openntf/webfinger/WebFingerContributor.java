package org.openntf.webfinger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonObject;

public interface WebFingerContributor {
	Collection<String> getItems();
	
	void contribute(Map<String, List<?>> items, JsonObject root) throws JsonException;
}
