/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.html5.renderkit.input.delegate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.html5.component.input.HtmlInputText;
import org.apache.myfaces.html5.renderkit.util.Html5RendererUtils;
import org.apache.myfaces.html5.renderkit.util.PassThroughAttributes;
import org.apache.myfaces.html5.renderkit.util.PassThroughClientBehaviorEvents;
import org.apache.myfaces.shared_html5.renderkit.html.HTML;
import org.apache.myfaces.shared_html5.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_html5.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared_html5.renderkit.html.HtmlTextareaRendererBase;

/**
 * Delegate renderer that is used when the type of < hx:input > is one of "textarea".
 * 
 * @author Ali Ok
 * 
 */
public class HtmlTextAreaRendererDelegate extends HtmlTextareaRendererBase
{
    @Override
    protected void renderTextAreaBegin(FacesContext facesContext,
            UIComponent uiComponent) throws IOException
    {
        renderPassThruAttrsAndEvents(facesContext, uiComponent);
    }

    // to make this extendible
    protected void renderPassThruAttrsAndEvents(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        Map<String, List<ClientBehavior>> clientBehaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();

        Html5RendererUtils.renderPassThroughClientBehaviorEventHandlers(facesContext, uiComponent,
                PassThroughClientBehaviorEvents.BASE_INPUT, clientBehaviors);

        Html5RendererUtils.renderPassThroughAttributes(facesContext.getResponseWriter(), uiComponent,
                PassThroughAttributes.INPUT_TEXTAREA);
    }

    @Override
    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        return ((HtmlInputText) uiComponent).isDisabled();
    }

}
