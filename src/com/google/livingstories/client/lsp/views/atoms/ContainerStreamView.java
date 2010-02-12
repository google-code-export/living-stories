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

package com.google.livingstories.client.lsp.views.atoms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.atomlist.AtomListElement;
import com.google.livingstories.client.lsp.BylineWidget;
import com.google.livingstories.client.lsp.event.BlockToggledEvent;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.views.DateTimeRangeWidget;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.ui.ToggleDisclosurePanel;
import com.google.livingstories.client.ui.WindowScroll;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Code for rendering a stream view for a content entity.  Displays the entity
 * content as well as linked content.  This class is extended by EventStreamView
 * and NarrativeStreamView.
 */
public abstract class ContainerStreamView<T extends BaseAtom> extends Composite
    implements AtomListElement {
  private static EventStreamViewUiBinder uiBinder = GWT.create(EventStreamViewUiBinder.class);

  @SuppressWarnings("unchecked")
  @UiTemplate("ContainerStreamView.ui.xml")
  interface EventStreamViewUiBinder extends UiBinder<Widget, ContainerStreamView> {
    // This interface should theoretically use a genericized version of ContainerStreamView,
    // but there's a bug in GWT that prevents that from working.  Instead, we use the raw
    // type here.  This works in most situations, though there are certain things
    // you won't be able to do (e.g. @UiHandler won't be able to bind to a method that
    // takes a parameterized type.)
    // TODO: fix this when the next version of GWT comes out and the bug is fixed.
  }
  
  @UiField ContainerStreamViewStyles style;
  
  @UiField HTML title;
  @UiField SimplePanel byline;
  @UiField DateTimeRangeWidget timestamp;
  @UiField ToggleDisclosurePanel content;
  
  protected T atom;
  protected Map<AtomType, List<BaseAtom>> linkedAtomsByType;
  private HandlerRegistration toggleEventHandler;

  public ContainerStreamView(T containerAtom, Map<AtomType, List<BaseAtom>> linkedAtomsByType) {
    this.atom = containerAtom;
    this.linkedAtomsByType = linkedAtomsByType;
    boolean lowImportance = atom.getImportance() == Importance.LOW;
    
    initWidget(uiBinder.createAndBindUi(this));
    
    if (atom.getRenderAsSeen()) {
      this.addStyleName(Resources.INSTANCE.css().read());
    }

    title.setHTML(getHeadline());
    
    GlobalUtil.addIfNotNull(byline,
        BylineWidget.makeContextSensitive(atom, new HashSet<Long>()));
    timestamp.setDateTime(getStartDate(), getEndDate());

    if (lowImportance) {
      title.removeStyleName(style.headline());
      byline.setVisible(false);
      timestamp.setTimeVisible(false);
    }

    ShortContainerView<T> shortView
        = new ShortContainerView<T>(atom, linkedAtomsByType);
    LongContainerView<T> longView = getLongContainerView();

    if (longView.hasExtraContent()) {
      title.addStyleName(Resources.INSTANCE.css().clickable());
      title.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          EventBus.INSTANCE.fireEvent(new BlockToggledEvent(!content.isOpen(), atom.getId()));
        }
      });
      content.setContent(shortView, longView);
      content.handleAtomEvents(atom.getId());
      ContainerStreamViewFooter disclosurePanelHeader = new ContainerStreamViewFooter(atom);
      disclosurePanelHeader.addNavLinks(longView.getExtraContentNavLinks(), content);
      content.setHeader(disclosurePanelHeader);
    } else {
      content.setContent(shortView, null);
    }
    
    toggleEventHandler = EventBus.INSTANCE.addHandler(BlockToggledEvent.TYPE,
        new BlockToggledEvent.Handler() {
          @Override
          public void onToggle(BlockToggledEvent e) {
            if (atom.getId().equals(e.getAtomId())
                && !e.isOpened()
                && e.shouldScrollOnClose()) {
              // Scroll to the top of this event if it's being closed
              // and the window is below the event.
              int blockPosition = title.getAbsoluteTop();
              if (blockPosition < Window.getScrollTop()) {
                WindowScroll.scrollTo(blockPosition,
                    new Command() {
                      @Override
                      public void execute() {
                        LivingStoryControls.repositionAnchoredPanel();
                      }
                    });
              }
            }
          }
        });
  }

  protected abstract String getHeadline();
  
  protected abstract Date getStartDate();
  
  protected abstract Date getEndDate();
  
  protected abstract LongContainerView<T> getLongContainerView();
  
  @Override
  protected void onUnload() {
    super.onUnload();
    if (toggleEventHandler != null) {
      toggleEventHandler.removeHandler();
      toggleEventHandler = null;
    }
  }
  
  @Override
  public String getDateString() {
    return timestamp.getDateString();
  }

  @Override
  public Long getId() {
    return atom.getId();
  }

  @Override
  public Importance getImportance() {
    return atom.getImportance();
  }

  @Override
  public Set<Long> getThemeIds() {
    return atom.getThemeIds();
  }

  @Override
  public Widget render(boolean includeAtomName) {
    return this;
  }

  /**
   * Method for expanding and collapsing the view programmatically.  This is only used
   * by the atom list when setting view expansion in bulk.
   */
  @Override
  public boolean setExpansion(boolean expand) {
    EventBus.INSTANCE.fireEvent(new BlockToggledEvent(expand, atom.getId())
        .skipAnimation().skipHistory().skipScrollOnClose());
    return true;
  }

  @Override
  public void setTimeVisible(boolean visible) {
    timestamp.setTimeVisible(visible);
  }
}
