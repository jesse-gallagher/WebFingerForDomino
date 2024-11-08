package org.openntf.webfinger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonObject;

public interface WebFingerContributor {
	Collection<String> getItems();
	
	/**
	 * Called to ask the contributor to add its information to the result payload.
	 * 
	 * @param items a {@link Map} of the requested items to their values from the directory
	 * @param root the root JSON result object
	 * @param requestedRel a {@link Collection} of rel values that the client requested. If
	 *        the client did not request specific rels, this will be empty
	 * @throws JsonException if there is a problem building the JSON response
	 */
	void contribute(Map<String, List<?>> items, JsonObject root, Collection<String> requestedRel) throws JsonException;
}
