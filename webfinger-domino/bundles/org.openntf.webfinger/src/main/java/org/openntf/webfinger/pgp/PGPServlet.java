package org.openntf.webfinger.pgp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject;
import com.ibm.commons.util.io.json.util.JsonWriter;

import lotus.domino.Directory;
import lotus.domino.DirectoryNavigator;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

public class PGPServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String ITEM_PGPKEY = "pgpPublicKey";
	public static final String TYPE_KEYS = "application/pgp-keys";
	/**
	 * This should match the prefix registered for the Servlet in plugin.xml
	 */
	public static final String EXPECTED_BASE = "/.webfinger-pgp-key/";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter w = resp.getWriter();
		
		String resource = req.getPathInfo();
		if(StringUtil.isEmpty(resource) || "/".equals(resource)) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			resp.setContentType("application/json");
			JsonObject result = new JsonJavaObject();
			result.putJsonProperty("message", "Missing resource parameter");
			w.write(result.toString());
			return;
		}
		String username = resource.substring(1);
		
		try {
			Session session = NotesFactory.createSession();
			try {
				Directory dir = session.getDirectory();
				Vector<String> names = new Vector<>();
				names.add(username);
				Collection<String> items = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
				items.add(ITEM_PGPKEY);
				
				Vector<String> itemsList = new Vector<String>(items);
				DirectoryNavigator nav = dir.lookupNames("$Users", names, itemsList, false);
				if(!nav.findFirstMatch()) {
					throw new FileNotFoundException();
				}
				
				Vector<?> keys = nav.getFirstItemValue();
				
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.setContentType(TYPE_KEYS);
				for(Object key : keys) {
					w.println(key);
				}
				
			} finally {
				session.recycle();
			}
		} catch(FileNotFoundException e) {
			// Generic use for user-not-found
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			resp.setContentType("application/json");
			JsonObject result = new JsonJavaObject();
			result.putJsonProperty("message", "Resource not found or not available");
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
}
