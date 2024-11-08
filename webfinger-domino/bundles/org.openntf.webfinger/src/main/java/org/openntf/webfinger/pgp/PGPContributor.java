package org.openntf.webfinger.pgp;

import static org.openntf.webfinger.WebFingerUtil.stringVal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonArray;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject;

import org.openntf.webfinger.WebFingerContributor;

public class PGPContributor implements WebFingerContributor {
	public static final String ITEM_WEBFINGERPGP = "WebFingerPGP";
	public static final String REL_PGPKEY = "pgpkey";

	@Override
	public Collection<String> getItems() {
		return Set.of(PGPServlet.ITEM_PGPKEY, ITEM_WEBFINGERPGP);
	}

	@Override
	public void contribute(Map<String, List<?>> items, JsonObject root, Collection<String> requestedRel, String requestedResource)
			throws JsonException {
		String usePgp = stringVal(items.get(ITEM_WEBFINGERPGP));
		if("1".equals(usePgp) && (requestedRel.isEmpty() || requestedRel.contains(REL_PGPKEY))) {
			String pgpKey = stringVal(items.get(PGPServlet.ITEM_PGPKEY));
			if(StringUtil.isNotEmpty(pgpKey)) {
				JsonArray links = (JsonArray)root.getJsonProperty("links");
				
				JsonObject link = new JsonJavaObject();
				link.putJsonProperty("rel", "pgpkey");
				link.putJsonProperty("type", PGPServlet.TYPE_KEYS);
				link.putJsonProperty("href", PGPServlet.EXPECTED_BASE + URLEncoder.encode(requestedResource, StandardCharsets.UTF_8));
				links.add(link);
			}
		}
	}

}
