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
package org.apache.myfaces.html5.handler;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.BehaviorConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.html5.behavior.DropTargetBehavior;
import org.apache.myfaces.html5.event.DropEvent;
import org.apache.myfaces.html5.event.DropListener;
import org.apache.myfaces.html5.renderkit.behavior.DropTargetBehaviorRenderer;
import org.apache.myfaces.html5.renderkit.util.ClientBehaviorEvents;
import org.apache.myfaces.shared_html5.renderkit.RendererUtils;

/* 
 * Facelets tag handler for DropTargetBehavior.
 *
 * 
 * DO NOT JAVADOC here since we don't want this doc to show up in Facelets doc.
 */
@JSFFaceletTag(name = "fx:dropTarget", behaviorClass="org.apache.myfaces.html5.behavior.DropTargetBehavior")
public class DropTargetBehaviorHandler extends javax.faces.view.facelets.BehaviorHandler
{

    private static final Logger log = Logger.getLogger(DropTargetBehaviorHandler.class.getName());

    /**
     * Action to allow for drop operation. Can be one of below:
     * <ul>
     * <li>copy: A copy of the source item may be made at the new location.</li>
     * <li>move: An item may be moved to a new location.</li>
     * <li>link: A link may be established to the source at the new location.</li>
     * <li>copyLink: A copy or link operation is permitted.</li>
     * <li>copyMove: A copy or move operation is permitted.</li>
     * <li>linkMove: A link or move operation is permitted.</li>
     * <li>all: All operations are permitted.</li>
     * <li>none: The item may not be dropped.</li>
     * </ul>
     * <br/>
     * 
     * If nothing is specified, any action will be accepted. Action is set by the hx:dragSource behavior, when the
     * element is generated by a MyFaces-Html5 component that has hx:dragSource behavior. For other elements, action is
     * set by the browser, and can be adjusted by pressing the modifier keys.
     * 
     */
    @JSFFaceletAttribute(name = "action", className = "javax.el.ValueExpression", deferredValueType = "java.lang.String")
    private final TagAttribute _action;

    /**
     * The type of the drop target. Can be comma separated set or String[] or Collection<String>. <br/>
     * If defined, drags from elements that are generated by MyFaces-Html5 components with hx:dragSource behavior, will
     * be filtered. The drag will be accepted if dropTargetTypes of hx:dragSource is one of the allowed. For the drags
     * that are originated from other elements, this property is ignored. Please see acceptMimeTypes property for
     * accepting/rejecting drags from non-MyFaces-Html5 components.
     */
    @JSFFaceletAttribute(name = "types", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Object")
    private final TagAttribute _types;

    /**
     * If this property is set, only content dropped into this drop zone with defined mime type will be accepted and
     * sent to server-side drop listener. Can be comma separated set or String[] or Collection<String>. <br/>
     * <br/>
     * HTML5 DnD allows us to drop anything into drop zone : files from desktop, images on some other document, etc. So
     * this property is a filter. If value is "*", any content dropped into this zone will be accepted. <br/>
     * <br/>
     * All type info and data of dropped stuff will be sent to dropListener. For example, if value of this property is
     * "*" and we drop some image from any Html page(even not generated by JSF), dropListener will be triggered with the
     * following data:
     * <table border="1">
     * <tr>
     * <td>content-type</td>
     * <td>value</td>
     * </tr>
     * <tr>
     * <td>text/uri-list</td>
     * <td>http://example.org/someImage.png</td>
     * </tr>
     * <tr>
     * <td>Text</td>
     * <td>http://example.org/someImage.png</td>
     * </tr>
     * <tr>
     * <td>text/plain</td>
     * <td>http://example.org/someImage.png</td>
     * </tr>
     * <tr>
     * <td>URL</td>
     * <td>http://example.org/someImage.png</td>
     * </tr>
     * </table>
     * <br/>
     * 
     * Mime type is "text/x-myfaces-html5-dnd-source" for drag&drop events that is generated by MyFaces-Html5 components
     * and that mime type is defined at {@link DropTargetBehaviorRenderer#DEFAULT_MYFACES_MIME_TYPE}. Default value of
     * this property is "text/x-myfaces-html5-dnd-source", thus only MyFaces generated components can be dropped into
     * the drop target.
     */
    @JSFFaceletAttribute(name = "acceptMimeTypes", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Object")
    private final TagAttribute _acceptMimeTypes;

    /**
     * Space separated ids of components to rerender. <br/>
     * Value of this property will be passed through to jsf.ajax.request, thus semantics is same with jsf.ajax.request
     * and f:ajax. Just like those, @all, @this etc. can be used.
     */
    @JSFFaceletAttribute(name = "rerender", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Object")
    private final TagAttribute _rerender;

    /**
     * Drop listener to trigger when a successful drop event is happened into this drop target. <br/>
     * Listener method must have a signature of :
     * <code>public void m(org.apache.myfaces.html5.event.DropEvent evt) throws javax.faces.event.AbortProcessingException</code>
     * <br/>
     * In the listener, application can get the parameter sent and other data sent.
     */
    @JSFFaceletAttribute(name = "dropListener", className = "javax.el.MethodExpression", deferredMethodSignature = "public void m(org.apache.myfaces.html5.event.DropEvent evt) throws javax.faces.event.AbortProcessingException")
    private final TagAttribute _dropListener;

    public DropTargetBehaviorHandler(BehaviorConfig config)
    {
        super(config);
        _action = getAttribute("action");
        _types = getAttribute("types");
        _acceptMimeTypes = getAttribute("acceptMimeTypes");
        _rerender = getAttribute("rerender");
        _dropListener = getAttribute("dropListener");
    }

    @Override
    public void apply(FaceletContext faceletContext, UIComponent parent)
    {
        if (!ComponentHandler.isNew(parent))
        {
            if (log.isLoggable(Level.FINE))
                log.fine("Component" + RendererUtils.getPathToComponent(parent)
                        + " is not new, thus return without any operation.");
            return;
        }

        if (parent instanceof ClientBehaviorHolder)
        {
            ClientBehaviorHolder holder = _getClientBehaviorHolder(parent);

            FacesContext context = faceletContext.getFacesContext();
            Application app = context.getApplication();
            String behaviorId = getBehaviorId();
            Behavior behavior = app.createBehavior(behaviorId);

            if (!(behavior instanceof DropTargetBehavior))
            {
                throw new FacesException("Behavior is not a DropTargetBehavior");
            }

            // manually added all of the properties, so no need for this:
            // setAttributes(faceletContext, behavior);

            DropTargetBehavior dropTargetBehavior = (DropTargetBehavior) behavior;

            if (_dropListener != null)
            {

                MethodExpression expr = _dropListener.getMethodExpression(faceletContext, Void.TYPE, new Class<?>[]
                {
                    DropEvent.class
                });
                dropTargetBehavior.addDropTargetBehaviorListener(new DropListener(expr));
            }

            // see https://issues.apache.org/jira/browse/MYFACES-2616
            // see the thread http://www.mail-archive.com/dev@myfaces.apache.org/msg46764.html
            // using the same approach in DropSourceBehavior too... see there for explanation!
            if (_action != null)
            {
                if (_action.isLiteral())
                {
                    dropTargetBehavior.setAction(_action.getValue(faceletContext));
                }
                else
                {
                    dropTargetBehavior.setValueExpression("action", _action.getValueExpression(faceletContext,
                            String.class));
                }
            }
            if (_types != null)
            {
                if (_types.isLiteral())
                {
                    dropTargetBehavior.setTypes(_types.getObject(faceletContext));
                }
                else
                {
                    dropTargetBehavior.setValueExpression("types", _types.getValueExpression(faceletContext,
                            Object.class));
                }
            }
            if (_acceptMimeTypes != null)
            {
                if (_acceptMimeTypes.isLiteral())
                {
                    dropTargetBehavior.setAcceptMimeTypes(_acceptMimeTypes.getObject(faceletContext));
                }
                else
                {
                    dropTargetBehavior.setValueExpression("acceptMimeTypes", _acceptMimeTypes.getValueExpression(
                            faceletContext, Object.class));
                }
            }
            if (_rerender != null)
            {
                if (_rerender.isLiteral())
                {
                    dropTargetBehavior.setRerender(_rerender.getObject(faceletContext));
                }
                else
                {
                    dropTargetBehavior.setValueExpression("rerender", _rerender.getValueExpression(faceletContext,
                            Object.class));
                }
            }

            holder.addClientBehavior(ClientBehaviorEvents.DRAGENTER_EVENT, dropTargetBehavior);
            holder.addClientBehavior(ClientBehaviorEvents.DRAGOVER_EVENT, dropTargetBehavior);
            holder.addClientBehavior(ClientBehaviorEvents.DROP_EVENT, dropTargetBehavior);
        }
        //XXX: try in a composite component!
        /*
         * else if (UIComponent.isCompositeComponent(parent)) { // COPIED FROM AjaxHandler! // It is supposed that for
         * composite components, this tag should // add itself as a target, but note that on whole api does not exists
         * // some tag that expose client behaviors as targets for composite // components. In RI, there exists a tag
         * called composite:clientBehavior, // but does not appear on spec or javadoc, maybe because this could be //
         * understand as an implementation detail, after all there exists a key // called
         * AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY that could be // used to create a tag outside jsf
         * implementation to attach targets. CompositeComponentResourceTagHandler.addAttachedObjectHandler(parent,
         * this); }
         */
    }

    private ClientBehaviorHolder _getClientBehaviorHolder(UIComponent parent)
    {
        if (!(parent instanceof ClientBehaviorHolder))
        {
            throw new TagException(getTag(),
                    "DropTargetBehavior must be attached to a ClientBehaviorHolder parent. Component "
                            + RendererUtils.getPathToComponent(parent) + "is not a ClientBehaviorHolder");
        }

        ClientBehaviorHolder holder = (ClientBehaviorHolder) parent;

        _checkEvent(holder, ClientBehaviorEvents.DRAGENTER_EVENT);
        _checkEvent(holder, ClientBehaviorEvents.DRAGOVER_EVENT);
        _checkEvent(holder, ClientBehaviorEvents.DROP_EVENT);

        return holder;
    }

    private void _checkEvent(ClientBehaviorHolder holder, String eventName)
    {
        Collection<String> eventNames = holder.getEventNames();

        if (!eventNames.contains(eventName))
        {
            StringBuilder message = new StringBuilder();
            message.append("Unable to attach DropTargetBehavior.  ");
            message.append("DropTargetBehavior may only be attached to ");
            message.append("ClientBehaviorHolders that support the '");
            message.append(eventName);
            message.append("' event.  The parent ClientBehaviorHolder "
                    + RendererUtils.getPathToComponent((UIComponent) holder) + " only ");
            message.append("supports the following events: ");

            for (String supportedEventName : eventNames)
            {
                message.append(supportedEventName);
                message.append(" ");
            }

            throw new TagException(getTag(), message.toString());
        }
    }

}
