/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.livingstories.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.List;

/**
 * A widget that consists of a header and a content panel.  Some items within the
 * content panel are initially hidden, and clicking the header will toggle the display
 * state of these items.  The panel will also animate, expanding and collapsing
 * to accomodate these elements.
 * 
 * This class uses the same style rules as the standard gwt DisclosurePanel.
 * .gwt-DisclosurePanel { the panel's primary style }
 * .gwt-DisclosurePanel-open { dependent style set when panel is open }
 * .gwt-DisclosurePanel-closed { dependent style set when panel is closed }
 * .gwt-DisclosurePanel .header { style for the header }
 * .gwt-DisclosurePanel .content { style for the content }
 * 
 * Quite a bit of this code comes directly from the standard DisclosurePanel itself;
 * however, extending that class didn't seem practical for our use case, so we rewrite
 * some functionality here.
 */
public final class PartialDisclosurePanel extends Composite implements HasAnimation,
    HasOpenHandlers<PartialDisclosurePanel>, HasCloseHandlers<PartialDisclosurePanel>,
    HasClickHandlers {
  /**
   * Used to wrap widgets in the header to provide click support. Effectively
   * wraps the widget in an <code>anchor</code> to get automatic keyboard
   * access.
   */
  private final class ClickableHeader extends SimplePanel implements HasClickHandlers {

    private ClickableHeader() {
      // Anchor is used to allow keyboard access.
      super(DOM.createAnchor());
      Element elem = getElement();
      DOM.setElementProperty(elem, "href", "javascript:void(0);");
      // Avoids layout problems from having blocks in inlines.
      DOM.setStyleAttribute(elem, "display", "block");
      sinkEvents(Event.ONCLICK);
      setStyleName(STYLENAME_HEADER);
    }
    
    public HandlerRegistration addClickHandler(ClickHandler handler) {
      return addHandler(handler, ClickEvent.getType());
    }

    @Override
    public void onBrowserEvent(Event event) {
      // no need to call super.
      switch (DOM.eventGetType(event)) {
        case Event.ONCLICK:
          // Prevent link default action.
          DOM.eventPreventDefault(event);
          ClickEvent.fireNativeEvent(event, this);
          setOpen(!isOpen);
      }
    }
  }

  // Stylename constants.
  private static final String STYLENAME_DEFAULT = "gwt-DisclosurePanel";

  private static final String STYLENAME_SUFFIX_OPEN = "open";

  private static final String STYLENAME_SUFFIX_CLOSED = "closed";

  private static final String STYLENAME_HEADER = "header";

  private static final String STYLENAME_CONTENT = "content";

  private final VerticalPanel mainPanel = new VerticalPanel();
  private final ClickableHeader header = new ClickableHeader();
  private final SimplePanel contentWrapper = new SimplePanel();

  private boolean isAnimationEnabled = false;
  private boolean isOpen = false;
  private List<Widget> toggledWidgets;
  private Command onAnimationCompletion;
  private Command oneTimeOnAnimationCompletion;
  
  public PartialDisclosurePanel(boolean headerOnTop) {
    init(headerOnTop);
  }
  
  public PartialDisclosurePanel(Widget panelHeader, boolean headerOnTop) {
    this(headerOnTop);
    setHeader(panelHeader);
  }

  public HandlerRegistration addCloseHandler(
      CloseHandler<PartialDisclosurePanel> handler) {
    return addHandler(handler, CloseEvent.getType());
  }

  public HandlerRegistration addOpenHandler(
      OpenHandler<PartialDisclosurePanel> handler) {
    return addHandler(handler, OpenEvent.getType());
  }

  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return header.addClickHandler(handler);
  }
  
  public void setAnimationCompletionCommand(Command onAnimationCompletion) {
    this.onAnimationCompletion = onAnimationCompletion;
  }
  
  public void clear() {
    setContent(null, null);
  }

  public Widget getContent() {
    return contentWrapper.getWidget();
  }

  public Widget getHeader() {
    return header.getWidget();
  }

  public HasText getHeaderTextAccessor() {
    Widget widget = header.getWidget();
    return (widget instanceof HasText) ? (HasText) widget : null;
  }
  
  public boolean isAnimationEnabled() {
    return isAnimationEnabled;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void setAnimationEnabled(boolean enable) {
    isAnimationEnabled = enable;
  }
  
  /**
   * Sets the content widget which can be opened and closed by this panel. If
   * there is a preexisting content widget, it will be detached.
   * 
   * @param content the widget to be used as the content panel
   * @param toggledWidgets items that will be shown and hidden by this panel.
   */
  public void setContent(Widget content, List<Widget> toggledWidgets) {
    final Widget currentContent = getContent();

    // Remove existing content widget.
    if (currentContent != null) {
      contentWrapper.setWidget(null);
      this.toggledWidgets = null;
      currentContent.removeStyleName(STYLENAME_CONTENT);
    }

    // Add new content widget if != null.
    if (content != null) {
      contentWrapper.setWidget(content);
      this.toggledWidgets = toggledWidgets;
      content.addStyleName(STYLENAME_CONTENT);
    }
  }

  /**
   * Sets the widget used as the header for the panel.
   * 
   * @param headerWidget the widget to be used as the header
   */
  public void setHeader(Widget headerWidget) {
    header.setWidget(headerWidget);
  }

  /**
   * Changes the visible state of this <code>DisclosurePanel</code>.
   * 
   * @param isOpen <code>true</code> to open the panel, <code>false</code> to
   * close
   */
  public void setOpen(boolean isOpen) {
    if (this.isOpen != isOpen) {
      this.isOpen = isOpen;
      setContentDisplay(isAnimationEnabled);
      fireEvent();
    }
  }

  public void scrollToContainedWidget(final Widget w) {
    Command doScroll = new Command() {
      @Override
      public void execute() {
        Window.scrollTo(0, w.getElement().getAbsoluteTop());
        LivingStoryControls.repositionAnchoredPanel();
      }
    };

    if (isOpen()) {
      doScroll.execute();
    } else {
      setOneTimeAnimationCompletionCommand(doScroll);
      setOpen(true);
    }
  }

  /**
   * <b>Affected Elements:</b>
   * <ul>
   * <li>-header = the clickable header.</li>
   * </ul>
   * 
   * @see UIObject#onEnsureDebugId(String)
   */
  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    header.ensureDebugId(baseID + "-header");
  }

  private void setOneTimeAnimationCompletionCommand(Command oneTimeOnAnimationCompletion) {
    this.oneTimeOnAnimationCompletion = oneTimeOnAnimationCompletion;
  }
          

  private void fireEvent() {
    if (isOpen) {
      OpenEvent.fire(this, this);
    } else {
      CloseEvent.fire(this, this);
    }
  }

  private void init(boolean headerOnTop) {
    initWidget(mainPanel);
    if (headerOnTop) {
      mainPanel.add(header);
      mainPanel.add(contentWrapper);
    } else {
      mainPanel.add(contentWrapper);
      mainPanel.add(header);
    }
    DOM.setStyleAttribute(contentWrapper.getElement(), "padding", "0px");
    DOM.setStyleAttribute(contentWrapper.getElement(), "overflow", "hidden");
    setStyleName(STYLENAME_DEFAULT);
    addStyleDependentName(STYLENAME_SUFFIX_CLOSED);
  }

  private void setContentDisplay(boolean animate) {
    if (isOpen) {
      removeStyleDependentName(STYLENAME_SUFFIX_CLOSED);
      addStyleDependentName(STYLENAME_SUFFIX_OPEN);
    } else {
      removeStyleDependentName(STYLENAME_SUFFIX_OPEN);
      addStyleDependentName(STYLENAME_SUFFIX_CLOSED);
    }

    if (getContent() != null) {
      int oldHeight = getContent().getElement().getClientHeight();
      DOM.setStyleAttribute(contentWrapper.getElement(), "height", oldHeight + "px");
      
      for (Widget widget : toggledWidgets) {
        widget.setVisible(!widget.isVisible());
      }

      int newHeight = getContent().getElement().getClientHeight();

      if (animate) {
        ExpandEffect animation = new ExpandEffect(contentWrapper, newHeight);
        animation.run(Constants.ANIMATION_DURATION);
      } else {
        DOM.setStyleAttribute(contentWrapper.getElement(), "height", "auto");
        runCompletionCode();
      }
    }
  }
  
  private class ExpandEffect extends StyleEffect {
    public ExpandEffect(Widget widget, int newValue) {
      super(widget, "height", newValue);
    }

    @Override
    public void onComplete() {
      DOM.setStyleAttribute(widget.getElement(), "height", "auto");
      runCompletionCode();
    }
  }
  
  private void runCompletionCode() {
    // for IE friendliness, we always run the completion code via the DeferredCommand
    // mechanism.
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        if (onAnimationCompletion != null) {
          onAnimationCompletion.execute();
        }
        if (oneTimeOnAnimationCompletion != null) {
          oneTimeOnAnimationCompletion.execute();
          oneTimeOnAnimationCompletion = null;
        }
      }
    });
  }
}
