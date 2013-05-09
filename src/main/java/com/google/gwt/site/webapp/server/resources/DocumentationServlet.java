/*
 * Copyright 2013 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.site.webapp.server.resources;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DocumentationServlet extends HttpServlet {

  /**
	 * 
	 */
  private static final long serialVersionUID = 458719890608890896L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String fullPath = normalizePath(req.getRequestURI());

    Key key = KeyFactory.createKey("DocModel", fullPath);
    Query query = new Query("DocModel", key);
    Entity e = datastore.prepare(query).asSingleEntity();

    if (e == null) {
      resp.sendError(404);
      return;
    }
    String html = ((Text) e.getProperty("html")).getValue();

    setContentTypeByFileEnding(resp, fullPath);

    if (isBinaryFile(fullPath)) {
      byte[] decodeBase64 =
          org.apache.commons.codec.binary.Base64.decodeBase64(html.getBytes("UTF-8"));
      resp.getOutputStream().write(decodeBase64);

    } else {
      resp.getWriter().write(html);

    }

  }

  private void setContentTypeByFileEnding(HttpServletResponse resp, String fullPath) {

    if (fullPath.endsWith(".html")) {
      resp.setContentType("text/html");
      return;
    }

    if (fullPath.endsWith(".js")) {
      resp.setContentType("application/javascript");
      return;
    }
    if (fullPath.endsWith(".css")) {
      resp.setContentType("text/css");
      return;
    }

    if (fullPath.endsWith(".png")) {
      resp.setContentType("image/png");
      return;
    }

    if (fullPath.endsWith(".jpg")) {
      resp.setContentType("image/jpg");
      return;
    }

    if (fullPath.endsWith(".gif")) {
      resp.setContentType("image/gif");
      return;
    }

    // TODO: what is a good default value?

  }

  private boolean isBinaryFile(String path) {
    return path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif");
  }

  private String normalizePath(String fullPath) {

    fullPath = fullPath.substring("/docs/".length(), fullPath.length());

    if ("".equals(fullPath) || fullPath.endsWith("/")) {
      fullPath += "index.html";
    }
    return fullPath;
  }
}