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
import org.apache.myfaces.html5.renderkit.util.ClientBehaviorEvents;
import org.apache.myfaces.shared_html5.renderkit.RendererUtils;

@JSFFaceletTag(name = "fx:dropTarget")
// TODO: doc me
public class DropTargetBehaviorHandler extends javax.faces.view.facelets.BehaviorHandler
{

    private static final Logger log = Logger.getLogger(DropTargetBehaviorHandler.class.getName());

    /**
     * @see DropTargetBehavior#getAction()
     */
    @JSFFaceletAttribute(name = "action", className = "javax.el.ValueExpression", deferredValueType = "java.lang.String")
    private final TagAttribute _action;

    /**
     * @see DropTargetBehavior#getTypes()
     */
    @JSFFaceletAttribute(name = "types", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Object")
    private final TagAttribute _types;

    /**
     * @see DropTargetBehavior#getAcceptMimeTypes()
     */
    @JSFFaceletAttribute(name = "acceptMimeTypes", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Object")
    private final TagAttribute _acceptMimeTypes;

    /**
     * @see DropTargetBehavior#getRerender()
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

            // XXX: see https://issues.apache.org/jira/browse/MYFACES-2616
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
        // XXX: check this out
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
