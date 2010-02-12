// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.livingstories.client.atomlist;

import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusPanel;

import java.util.Set;

/**
 * Wraps an AtomListElement in a FocusPanel and fires AtomClickedEvents whenever
 * they are clicked.
 * 
 * @author ericzhang@google.com (Eric Zhang)
 */
public class ClickableAtomListElement extends AtomListElement {
  private AtomListElement element;
  private FocusPanel focusPanel;
  
  public ClickableAtomListElement(AtomListElement child, final AtomClickHandler handler) {
    this.element = child;
    focusPanel = new FocusPanel(element);
    focusPanel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        handler.onClick(element.getAtom());
      }
    });
  }

  @Override
  public String getDateString() {
    return element.getDateString();
  }

  @Override
  public Long getId() {
    return element.getId();
  }

  @Override
  public BaseAtom getAtom() {
    return element.getAtom();
  }
  
  @Override
  public Importance getImportance() {
    return element.getImportance();
  }

  @Override
  public Set<Long> getThemeIds() {
    return element.getThemeIds();
  }

  @Override
  public boolean setExpansion(boolean expand) {
    return element.setExpansion(expand);
  }

  @Override
  public void setTimeVisible(boolean visible) {
    element.setTimeVisible(visible);
  }
}
