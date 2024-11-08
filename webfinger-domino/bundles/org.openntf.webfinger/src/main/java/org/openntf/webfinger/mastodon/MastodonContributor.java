package org.openntf.webfinger.mastodon;

import static org.openntf.webfinger.WebFingerUtil.stringVal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonArray;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject;

import org.openntf.webfinger.WebFingerContributor;

public class MastodonContributor implements WebFingerContributor {

	@Override
	public Collection<String> getItems() {
		return Set.of("WebFingerMastodon", "MastodonUsername", "MastodonHost");
	}

	@Override
	public void contribute(Map<String, List<?>> items, JsonObject root) throws JsonException {
		String includeMastodon = stringVal(items.get("WebFingerMastodon"));
		String mastodonHost = stringVal(items.get("MastodonHost"));
		String mastodonUsername = stringVal(items.get("MastodonUsername"));
		
		JsonArray linksJson = (JsonArray)root.getJsonProperty("links");
		
		if("1".equals(includeMastodon) && StringUtil.isNotEmpty(mastodonHost)) {
			if(!(mastodonHost.startsWith("http://") || mastodonHost.startsWith("https://"))) {
				mastodonHost = "https://" + mastodonHost;
			}
			
			if(StringUtil.isNotEmpty(mastodonUsername)) {
				JsonObject self = new JsonJavaObject();
				self.putJsonProperty("rel", "self");
				self.putJsonProperty("type", "application/activity+json");
				self.putJsonProperty("href", PathUtil.concat(mastodonHost, "/users/" + URLEncoder.encode(mastodonUsername, StandardCharsets.UTF_8), '/'));
				linksJson.add(self);
			}
			
			JsonObject subscribe = new JsonJavaObject();
			subscribe.putJsonProperty("rel", "http://ostatus.org/schema/1.0/subscribe");
			subscribe.putJsonProperty("template", PathUtil.concat(mastodonHost, "/authorize_interaction?uri={uri}", '/'));
			linksJson.add(subscribe);
		}
	}
}
