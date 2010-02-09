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

package com.google.livingstories.client.lsp;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.DisplayAtomBundle;
import com.google.livingstories.client.atomlist.AtomClickHandler;
import com.google.livingstories.client.atomlist.AtomList;
import com.google.livingstories.client.ui.Slideshow;
import com.google.livingstories.client.util.FourthestateUtil;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Widget for the main area of the LSP that contains the list of content. By default, a list of
 * events and narrative atoms is shown. It can be filtered in various different ways, such as
 * to see only atoms of a particular type, to see only important atoms, etc.; the callers
 * generally handle this filtering by making asynchronous calls and then replacing the
 * contents of this widget with new contents. A simple chronological reversal of the
 * elements to show, however, can be accomplished by calling doSimpleReversal().
 */
public class LspAtomListWidget extends Composite {
  private VerticalPanel panel;
  private AtomList atomList;

  private Date nextDateInSequence;
  
  private Map<Long, BaseAtom> idToAtomMap;
  private Label moreLink;
  private Image loadingImage;
  private Label problemLabel;
  private ImageClickHandler imageClickHandler = new ImageClickHandler();
  
  private static String VIEW_MORE = LspMessageHolder.consts.viewMore();
  private static String PROBLEM_TEXT = LspMessageHolder.consts.viewMoreProblem();
  
  public LspAtomListWidget() {
    super();
    
    panel = new VerticalPanel();
    panel.addStyleName("atomList");
    
    atomList = createAtomList();

    moreLink = new Label(VIEW_MORE);
    moreLink.setStylePrimaryName("primaryLink");
    moreLink.addStyleName("biggerFont");
    moreLink.setVisible(false);
    DOM.setStyleAttribute(moreLink.getElement(), "padding", "5px");
    addMoreLinkHandler(moreLink);

    loadingImage = new Image("/images/loading.gif");
    loadingImage.setVisible(false);
    
    problemLabel = new Label(PROBLEM_TEXT);
    problemLabel.addStyleName("error");
    problemLabel.setVisible(false);
    
    panel.add(atomList);
    panel.add(moreLink);
    panel.add(loadingImage);
    panel.add(problemLabel);

    clear();
    
    initWidget(panel);
  }
  
  protected AtomList createAtomList() {
    return AtomList.create(false);
  }
  
  protected void addMoreLinkHandler(HasClickHandlers moreLink) {
    moreLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        LivingStoryControls.getMoreAtoms();
      }
    });
  }
  
  public void clear() {
    atomList.clear();
    this.idToAtomMap = new HashMap<Long, BaseAtom>();
  }
  
  public void setIsImageList(boolean isImageList) {
    atomList.setAtomClickHandler(isImageList ? imageClickHandler : null);
  }
  
  private class ImageClickHandler implements AtomClickHandler {
    @Override
    public void onClick(BaseAtom clickedAtom) {
      int clickedIndex = -1, i = 0;
      List<BaseAtom> imagesAsBase = atomList.getAtomList();
      List<AssetAtom> images = new ArrayList<AssetAtom>(imagesAsBase.size());
      for (BaseAtom atom : imagesAsBase) {
        if (!FourthestateUtil.isContentEmpty(atom.getContent())) {
          images.add((AssetAtom) atom);
          if (atom.equals(clickedAtom)) {
            clickedIndex = i;
          }
          i++;
        }
      }
      
      if (clickedIndex != -1) {
        new Slideshow(images).show(clickedIndex);
      }
    }
  }
  
  public void beginLoading() {
    moreLink.setVisible(false);
    loadingImage.setVisible(true);
    problemLabel.setVisible(false);
  }
  
  public void showError() {
    moreLink.setVisible(false);
    loadingImage.setVisible(false);
    problemLabel.setVisible(true);
  }
  
  public void finishLoading(DisplayAtomBundle bundle) {
    // Create a map of all ids to the corresponding atoms
    List<BaseAtom> coreAtoms = bundle.getCoreAtoms();
    for (BaseAtom atom : coreAtoms) {
      idToAtomMap.put(atom.getId(), atom);
    }
    for (BaseAtom atom : bundle.getLinkedAtoms()) {
      idToAtomMap.put(atom.getId(), atom);
    }

    atomList.adjustTimeOrdering(bundle.getAdjustedFilterSpec().oldestFirst);

    atomList.appendAtoms(coreAtoms, idToAtomMap);
    
    nextDateInSequence = bundle.getNextDateInSequence();
    
    moreLink.setVisible(bundle.getNextDateInSequence() != null);
    loadingImage.setVisible(false);
    problemLabel.setVisible(false);
  }
  
  public void doSimpleReversal(boolean oldestFirst) {
    atomList.adjustTimeOrdering(oldestFirst);
  }
  
  /**
   * "Jumps to" the event indicated by atomId, scrolling it into view and opening its contents.
   * @return true if the event was found, false otherwise.
   */
  public boolean goToAtom(long atomId) {
    Set<Long> atomIds = new HashSet<Long>();
    atomIds.add(atomId);
    return atomList.openElements(atomIds);
  }
  
  public Date getNextDateInSequence() {
    return nextDateInSequence == null ? null : new Date(nextDateInSequence.getTime());
  }
  
  public boolean hasMore() {
    return moreLink.isVisible();
  }
}
