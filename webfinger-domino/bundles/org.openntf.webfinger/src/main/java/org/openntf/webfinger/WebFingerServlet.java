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

import static org.openntf.webfinger.WebFingerUtil.stringVal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonArray;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject;
import com.ibm.commons.util.io.json.util.JsonWriter;

import lotus.domino.Directory;
import lotus.domino.DirectoryNavigator;
import lotus.domino.NotesException;
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
			List<WebFingerContributor> contributors = WebFingerUtil.findExtensions(WebFingerContributor.class);
			
			Session session = NotesFactory.createSession();
			try {
				Directory dir = session.getDirectory();
				Vector<String> names = new Vector<>();
				names.add(username);
				Collection<String> items = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
				items.add("WebFingerEnable");
				items.add("WebFingerAliases");
				items.add("WebFingerProfile");
				
				// Add items requested by contributors
				for(WebFingerContributor contrib : contributors) {
					items.addAll(contrib.getItems());
				}
				
				Vector<String> itemsList = new Vector<String>(items);
				DirectoryNavigator nav = dir.lookupNames("$Users", names, itemsList, false);
				if(!nav.findFirstMatch()) {
					throw new FileNotFoundException();
				}
				
				Map<String, List<?>> vals = toMap(nav, itemsList);
				
				String enable = stringVal(vals.get("WebFingerEnable"));
				if(!"1".equals(enable)) {
					throw new FileNotFoundException();
				}
				List<?> aliases = vals.get("WebFingerAliases");
				String profileUrl = stringVal(vals.get("WebFingerProfile"));
				
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
				result.putJsonProperty("links", linksJson);
				
				for(WebFingerContributor contrib : contributors) {
					Collection<String> itemNames = contrib.getItems();
					Map<String, List<?>> payload = vals.entrySet().stream()
						.filter(entry -> itemNames.contains(entry.getKey()))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					contrib.contribute(payload, result);
				}
				
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
	
	private Map<String, List<?>> toMap(DirectoryNavigator nav, List<String> itemNames) throws NotesException {
		Map<String, List<?>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for(int i = 0; i < itemNames.size(); i++) {
			String itemName = itemNames.get(i);
			List<?> value = i == 0 ? nav.getFirstItemValue() : nav.getNextItemValue();
			result.put(itemName, value);
		}
		return result;
	}
}
