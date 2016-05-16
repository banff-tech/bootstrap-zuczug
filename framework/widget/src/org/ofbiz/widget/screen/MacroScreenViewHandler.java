/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.widget.screen;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilJ2eeCompat;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.collections.MapStack;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.view.AbstractViewHandler;
import org.ofbiz.webapp.view.ViewHandlerException;
import org.ofbiz.widget.form.FormStringRenderer;
import org.ofbiz.widget.form.MacroFormRenderer;
import org.ofbiz.widget.menu.MacroMenuRenderer;
import org.ofbiz.widget.menu.MenuStringRenderer;
import org.ofbiz.widget.tree.TreeStringRenderer;
import org.ofbiz.widget.tree.MacroTreeRenderer;
import org.xml.sax.SAXException;

import freemarker.template.TemplateException;
import freemarker.template.utility.StandardCompress;

public class MacroScreenViewHandler extends AbstractViewHandler {

    public static final String module = MacroScreenViewHandler.class.getName();

    protected ServletContext servletContext = null;

    public void init(ServletContext context) throws ViewHandlerException {
        this.servletContext = context;
    }

    private ScreenStringRenderer loadRenderers(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> context, Writer writer) throws GeneralException, TemplateException, IOException {
        String screenMacroLibraryPath = UtilProperties.getPropertyValue("widget", getName() + ".screenrenderer");
        String formMacroLibraryPath = UtilProperties.getPropertyValue("widget", getName() + ".formrenderer");
        String treeMacroLibraryPath = UtilProperties.getPropertyValue("widget", getName() + ".treerenderer");
        String menuMacroLibraryPath = UtilProperties.getPropertyValue("widget", getName() + ".menurenderer");
        Map<String, Object> userPreferences = UtilGenerics.cast(context.get("userPreferences"));
        if (userPreferences != null) {
            String visualThemeId = (String) userPreferences.get("VISUAL_THEME");
            if (visualThemeId != null) {
                LocalDispatcher dispatcher = (LocalDispatcher) context.get("dispatcher");
                Map<String, Object> serviceCtx = dispatcher.getDispatchContext().makeValidContext("getVisualThemeResources",
                        ModelService.IN_PARAM, context);
                serviceCtx.put("visualThemeId", visualThemeId);
                Map<String, Object> serviceResult = dispatcher.runSync("getVisualThemeResources", serviceCtx);
                if (ServiceUtil.isSuccess(serviceResult)) {
                    Map<String, List<String>> themeResources = UtilGenerics.cast(serviceResult.get("themeResources"));
                    List<String> resourceList = UtilGenerics.cast(themeResources.get("VT_SCRN_MACRO_LIB"));
                    if (resourceList != null && !resourceList.isEmpty()) {
                        String macroLibraryPath = resourceList.get(0);
                        if (macroLibraryPath != null) {
                            screenMacroLibraryPath = macroLibraryPath;
                        }
                    }
                    resourceList = UtilGenerics.cast(themeResources.get("VT_FORM_MACRO_LIB"));
                    if (resourceList != null && !resourceList.isEmpty()) {
                        String macroLibraryPath = resourceList.get(0);
                        if (macroLibraryPath != null) {
                            formMacroLibraryPath = macroLibraryPath;
                        }
                    }
                    resourceList = UtilGenerics.cast(themeResources.get("VT_TREE_MACRO_LIB"));
                    if (resourceList != null && !resourceList.isEmpty()) {
                        String macroLibraryPath = resourceList.get(0);
                        if (macroLibraryPath != null) {
                            treeMacroLibraryPath = macroLibraryPath;
                        }
                    }
                    resourceList = UtilGenerics.cast(themeResources.get("VT_MENU_MACRO_LIB"));
                    if (resourceList != null && !resourceList.isEmpty()) {
                        String macroLibraryPath = resourceList.get(0);
                        if (macroLibraryPath != null) {
                            menuMacroLibraryPath = macroLibraryPath;
                        }
                    }
                }
            }
        }
        ScreenStringRenderer screenStringRenderer = new MacroScreenRenderer(UtilProperties.getPropertyValue("widget", getName()
                + ".name"), screenMacroLibraryPath);
        FormStringRenderer formStringRenderer = new MacroFormRenderer(formMacroLibraryPath, request, response);
        context.put("formStringRenderer", formStringRenderer);
        TreeStringRenderer treeStringRenderer = new MacroTreeRenderer(treeMacroLibraryPath, writer);
        context.put("treeStringRenderer", treeStringRenderer);
        MenuStringRenderer menuStringRenderer = new MacroMenuRenderer(menuMacroLibraryPath, request, response);
        context.put("menuStringRenderer", menuStringRenderer);
        return screenStringRenderer;
    }

    public void render(String name, String page, String info, String contentType, String encoding, HttpServletRequest request, HttpServletResponse response) throws ViewHandlerException {
        Writer writer = null;
        try {
            // use UtilJ2eeCompat to get this setup properly
            boolean useOutputStreamNotWriter = false;
            if (this.servletContext != null) {
                useOutputStreamNotWriter = UtilJ2eeCompat.useOutputStreamNotWriter(this.servletContext);
            }
            if (useOutputStreamNotWriter) {
                ServletOutputStream ros = response.getOutputStream();
                writer = new OutputStreamWriter(ros, UtilProperties.getPropertyValue("widget", getName() + ".default.contenttype", "UTF-8"));
            } else {
                writer = response.getWriter();
            }

            // compress output if configured to do so
            if (UtilValidate.isEmpty(encoding)) {
                encoding = UtilProperties.getPropertyValue("widget", getName() + ".default.encoding", "none");
            }
            boolean compressOutput = "compressed".equals(encoding);
            if (!compressOutput) {
                compressOutput = "true".equals(UtilProperties.getPropertyValue("widget", getName() + ".compress"));
            }
            if (!compressOutput && this.servletContext != null) {
                compressOutput = "true".equals(this.servletContext.getAttribute("compressHTML"));
            }
            if (compressOutput) {
                // StandardCompress defaults to a 2k buffer. That could be increased
                // to speed up output.
                writer = new StandardCompress().getWriter(writer, null);
            }

            MapStack<String> context = MapStack.create();
            ScreenRenderer.populateContextForRequest(context, null, request, response, servletContext);
            ScreenStringRenderer screenStringRenderer = loadRenderers(request, response, context, writer);
            ScreenRenderer screens = new ScreenRenderer(writer, context, screenStringRenderer);
            context.put("screens", screens);
            context.put("simpleEncoder", StringUtil.getEncoder(UtilProperties.getPropertyValue("widget", getName() + ".encoder")));
            screenStringRenderer.renderScreenBegin(writer, context);
            screens.render(page);
            screenStringRenderer.renderScreenEnd(writer, context);
            writer.flush();
        } catch (TemplateException e) {
            Debug.logError(e, "Error initializing screen renderer", module);
            throw new ViewHandlerException(e.getMessage());
        } catch (IOException e) {
            throw new ViewHandlerException("Error in the response writer/output stream: " + e.toString(), e);
        } catch (SAXException e) {
            throw new ViewHandlerException("XML Error rendering page: " + e.toString(), e);
        } catch (ParserConfigurationException e) {
            throw new ViewHandlerException("XML Error rendering page: " + e.toString(), e);
        } catch (GeneralException e) {
            throw new ViewHandlerException("Lower level error rendering page: " + e.toString(), e);
        }
    }
}
