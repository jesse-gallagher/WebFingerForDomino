package org.openntf.webfinger.ext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonObject;

/**
 * This extension interface allows classes to contribute to the result
 * profile JSON.
 * 
 * @since 2.0.0
 */
public interface WebFingerContributor {
	/**
	 * Called to retrieve a collection of item names to request
	 * from the directory.
	 * 
	 * @return a {@link Collection} of strings; cannot be {@code null}
	 */
	Collection<String> getItems();
	
	/**
	 * Called to ask the contributor to add its information to the result payload.
	 * 
	 * <p>When this is called, {@code root} is guaranteed to contain a {@code "subject"}
	 * string, a {@code "links"} array, and an {@link "aliases"} array.</p>
	 * 
	 * @param items a {@link Map} of the requested items to their values from the directory
	 * @param root the root JSON result object
	 * @param requestedRel a {@link Collection} of rel values that the client requested. If
	 *        the client did not request specific rels, this will be empty
	 * @param requestedResource the originally requested resource name
	 * @throws JsonException if there is a problem building the JSON response
	 */
	void contribute(Map<String, List<?>> items, JsonObject root, Collection<String> requestedRel, String requestedResource) throws JsonException;
}
