/*
 * Copyright (c) 2022-2024 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.webfinger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonArray;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject; 
import com.ibm.commons.util.io.json.util.JsonWriter;

import lotus.domino.Directory;
import lotus.domino.DirectoryNavigator;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

public class WebFingerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String PARAM_RESOURCE = "resource";
	private static final String PREFIX = "acct:";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter w = resp.getWriter();
		
		String resource = req.getParameter(PARAM_RESOURCE);
		if(StringUtil.isEmpty(resource) || !resource.startsWith(PREFIX)) {
			throw new FileNotFoundException();
		}
		
		String username = resource.substring(PREFIX.length());
		if(StringUtil.isEmpty(username)) {
			throw new FileNotFoundException();
		}
		
		try {
			Session session = NotesFactory.createSession();
			try {
				Directory dir = session.getDirectory();
				Vector<String> names = new Vector<>();
				names.add(username);
				Vector<String> items = new Vector<>();
				items.add("WebFingerEnable");
				items.add("WebFingerAliases");
				items.add("WebFingerProfile");
				items.add("WebFingerMastodon");
				items.add("MastodonUsername");
				items.add("MastodonHost");
				DirectoryNavigator nav = dir.lookupNames("$Users", names, items, false);
				if(!nav.findFirstMatch()) {
					throw new FileNotFoundException();
				}
				
				String enable = stringVal(nav.getFirstItemValue());
				if(!"1".equals(enable)) {
					throw new FileNotFoundException();
				}
				Vector<?> aliases = nav.getNextItemValue();
				String profileUrl = stringVal(nav.getNextItemValue());
				String includeMastodon = stringVal(nav.getNextItemValue());
				String mastodonUsername = stringVal(nav.getNextItemValue());
				String mastodonHost = stringVal(nav.getNextItemValue());
				
				// Build the WebFinger response object
				JsonObject result = new JsonJavaObject();
				result.putJsonProperty("subject", resource);
				
				JsonArray aliasesJson = new JsonJavaArray();
				for(Object aliasObj : aliases) {
					String alias = StringUtil.toString(aliasObj);
					if(StringUtil.isNotEmpty(alias)) {
						aliasesJson.add(alias);
					}
				}
				result.putJsonProperty("aliases", aliasesJson);

				JsonArray linksJson = new JsonJavaArray();
				
				if(StringUtil.isNotEmpty(profileUrl)) {
					JsonObject linkObj = new JsonJavaObject();
					linkObj.putJsonProperty("rel", "http://webfinger.net/rel/profile-page");
					linkObj.putJsonProperty("href", profileUrl);
					linksJson.add(linkObj);
				}
				
				if("1".equals(includeMastodon) && StringUtil.isNotEmpty(mastodonHost)) {
					if(!(mastodonHost.startsWith("http://") || mastodonHost.startsWith("https://"))) {
						mastodonHost = "https://" + mastodonHost;
					}
					
					if(StringUtil.isNotEmpty(mastodonUsername)) {
						JsonObject self = new JsonJavaObject();
						self.putJsonProperty("rel", "self");
						self.putJsonProperty("type", "application/activity+json");
						self.putJsonProperty("href", PathUtil.concat(mastodonHost, "/users/" + urlEncode(mastodonUsername), '/'));
						linksJson.add(self);
					}
					
					JsonObject subscribe = new JsonJavaObject();
					subscribe.putJsonProperty("rel", "http://ostatus.org/schema/1.0/subscribe");
					subscribe.putJsonProperty("template", PathUtil.concat(mastodonHost, "/authorize_interaction?uri={uri}", '/'));
					linksJson.add(subscribe);
				}
				result.putJsonProperty("links", linksJson);
				
				
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.setContentType("application/jrd+json");
				JsonWriter jw = new JsonWriter(w, false);
				jw.outObject(result);
				
			} finally {
				session.recycle();
			}
		} catch(FileNotFoundException e) {
			// Generic use for user-not-found
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			resp.setContentType("application/json");
			JsonObject result = new JsonJavaObject();
			result.putJsonProperty("message", "User not found or not available");
			JsonWriter jw = new JsonWriter(w, false);
			try {
				jw.outObject(result);
			} catch (JsonException | IOException e2) {
				// Not much to do here
			}
		} catch(Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.setContentType("text/plain");
			e.printStackTrace(w);
		}
	}
	
	private String stringVal(Vector<?> vec) {
		if(vec == null || vec.isEmpty()) {
			return null;
		} else {
			return StringUtil.toString(vec.get(0));
		}
	}
	
	private String urlEncode(String val) {
		try {
			return URLEncoder.encode(val, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
	}
}
