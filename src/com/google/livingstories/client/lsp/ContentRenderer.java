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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ui.Link;
import com.google.livingstories.client.ui.ReadMoreLink;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to render HTML content from the summary or within content items. It does 2 things
 * currently:
 * 1. Breaks up the content into chunks separate by 'Read more' links if the text contains
 * break tags.
 * 2. If the text contains tags for other content items, converts them to links with appropriate
 * behavior.
 * If the tag contains the id of only 1 item, its content is displayed in a popup panel. If the tag
 * has multiple ids, the area below the summary is repopulated with the linked content items.  
 */
public class ContentRenderer extends Composite {
  private static final String BREAK_TAG = "<!--lsp:break-->";
  private static final String PARAGRAPH_END_TAG = "</p>";
  private static final String HIGHLIGHT_CLASS = "summaryHighlights";

  // Use a code tag for this since it's actually a valid html tag, recognized by JTidy.
  private static final String TIMELINE_TAG_NAME = "code";
  private static final String TIMELINE_IDENTIFIER = "lsp:timeline";
  
  private FlowPanel container;
  private String content;
  private int initiallyVisibleChunkCount;
  private boolean openHighlightedChunks;
  private boolean trackReadMoreWithAnalytics = false;
  
  private static String SHOW_LESS = LspMessageHolder.consts.showLess();
  
  public ContentRenderer(String content, boolean openHighlightedChunks) {
    this(content, openHighlightedChunks, false);
  }
  
  public ContentRenderer(String content, boolean openHighlightedChunks, 
      boolean trackReadMoreWithAnalytics) {
    super();
    this.container = new FlowPanel();
    this.content = content;
    this.openHighlightedChunks = openHighlightedChunks;
    this.trackReadMoreWithAnalytics = trackReadMoreWithAnalytics;
    populate();
    initWidget(container);
  }
  
  /**
   * Populates the flow panel with the content after inserting 'Read more' links where
   * <break> tags are present. Clicking on the link reveals the next section of the content.
   */
  private void populate() {
    String[] chunks = content.split(BREAK_TAG);
    
    if (chunks.length == 1) {
      container.add(processTagsInChunk(chunks[0]));
      return;
    }
    
    ReadMoreLink readMoreLink = null;
    boolean shouldExpand = false;
    boolean reachedExpandedChunk = false;

    // Create a link that causes everything to be hidden
    Link readLessLink = new Link(SHOW_LESS) {
      @Override
      protected void onClick(ClickEvent e) {
        // Hide all the widgets except the first few, as specified by the
        // initiallyVisibleChunkCount variable.
        for (int i = 1; i < container.getWidgetCount(); i++) {
          container.getWidget(i).setVisible(i < initiallyVisibleChunkCount);
        }
      }
    };
    readLessLink.setVisible(false);
    container.add(readLessLink);

    for (int i = chunks.length - 1; i >= 0; i--) {
      String chunk = chunks[i].trim();
      if (readMoreLink != null) {
        // Insert the previously created "read more" link at the beginning.
        container.insert(readMoreLink, 0);
      }

      // If this is the first chunk or if this chunk contains a highlight for new content,
      // expand it.
      if (i == 0 || (openHighlightedChunks && chunk.contains(HIGHLIGHT_CLASS))) {
        shouldExpand = true;
        if (!reachedExpandedChunk && readMoreLink != null) {
          // If all the previous chunks were hidden, then we should show the
          // read more link after this expanded chunk, if there is one.
          readMoreLink.setVisible(true);
        }
        if (i > 0) {
          // If this expanded chunk isn't the first chunk, then we should show
          // the read less link.
          readLessLink.setVisible(true);          
        }
        reachedExpandedChunk = true;
      }

      // Process this chunk.  First, check if this chunk ends with text in paragraph tags
      // followed by non-text content.
      int position = 0;
      int finalParagraphIndex = chunk.toLowerCase().lastIndexOf(PARAGRAPH_END_TAG);
      int finalParagraphEndIndex = finalParagraphIndex + PARAGRAPH_END_TAG.length();
      if (finalParagraphIndex > 0 &&  finalParagraphEndIndex < chunk.length()) {
        // There's non-text stuff after the last paragraph tag.
        // Split this into two chunks for processing,
        // and put the 'read more' link before the non-text content.
        HTMLPanel html1 = processTagsInChunk(chunk.substring(0, finalParagraphEndIndex + 1));
        html1.setVisible(shouldExpand);
        container.insert(html1, position++);
        if (readMoreLink != null) {
          container.insert(readMoreLink, position++);
        }
        HTMLPanel html2 = processTagsInChunk(chunk.substring(finalParagraphEndIndex));
        html2.setVisible(shouldExpand);
        container.insert(html2, position++);

        // Create a new readMoreLink that will show the text chunk,
        // the next read more link, the non-text chunk, and the read less link.
        readMoreLink = new ReadMoreLink(trackReadMoreWithAnalytics, 
            html1, readMoreLink, html2, readLessLink);
        
        initiallyVisibleChunkCount = 3;
      } else {
        // Otherwise, process this as a single chunk.
        HTMLPanel html = processTagsInChunk(chunk);
        html.setVisible(shouldExpand);
        container.insert(html, position++);
        if (readMoreLink != null) {
          container.insert(readMoreLink, position++);
        }

        // Create a new readMoreLink that will show the chunk,
        // the next read more link, and the read less link.
        readMoreLink = new ReadMoreLink(trackReadMoreWithAnalytics, 
            html, readMoreLink, readLessLink);

        initiallyVisibleChunkCount = 2;
      }
    }
  }
  
  /**
   * Find the custom tags in the HTML and process them.
   */
  private HTMLPanel processTagsInChunk(String chunk) {
    HTMLPanel contentPanel = new HTMLPanel(chunk);

    try {
      // Process each type of tag
      for (ContentTag tag : contentTags) {
        NodeList<Element> tagNodeList = 
          contentPanel.getElement().getElementsByTagName(tag.getTagName());
        List<Element> tagElements = new ArrayList<Element>();
        for (int i = 0; i < tagNodeList.getLength(); i++) {
          // First iterate over the node list and copy all the elements into a new list. Can't 
          // iterate and modify them at the same time because the list changes dynamically.
          tagElements.add(tagNodeList.getItem(i));
        }
        
        for (Element tagElement : tagElements) {
          Widget widget = tag.createWidgetToReplaceTag(tagElement);

          if (widget != null) {
            // To replace the existing tag with the widget created above, the HTMLPanel needs
            // to have the id of the element being replaced. Since we can't expect users to assign
            // unique ids in every tag, we do this here automatically.
            String uniqueId = HTMLPanel.createUniqueId();
            tagElement.setId(uniqueId);
            contentPanel.addAndReplaceElement(widget, uniqueId);
          }
        }
      }
    } catch (Exception e) {
      // Just return the panel with the original content
    }

    return contentPanel;
  }
  
  /**
   * Interface for the tags within the content that will be processed.
   * To add a new type of tag, create an implementation of this interface and also add the tag
   * to the contentTags array
   */
  private interface ContentTag {
    String getTagName();
    Widget createWidgetToReplaceTag(Element tagElement);
  }
  
  /**
   * Processes timeline tags in the content, replacing them with timeline widgets.
   * 
   * To insert a timeline, use the following syntax:
   * <code>lsp:timeline</code>
   *
   * To set the width and height, just set the 'width' and 'height' attributes on the code tag:
   * <code width="700" height="125">lsp:timeline</code>
   */
  private static class TimelineTag implements ContentTag {
    @Override
    public String getTagName() {
      return TIMELINE_TAG_NAME;
    }

    @Override
    public Widget createWidgetToReplaceTag(final Element tag) {
      if (!tag.getInnerHTML().equals(TIMELINE_IDENTIFIER)) {
        return null;
      }
      
      Integer width = null;
      Integer height = null;
      try {
        width = Integer.valueOf(tag.getAttribute("width"));
        height = Integer.valueOf(tag.getAttribute("height"));
      } catch (NumberFormatException ex) {
        // No size or invalid size specified.  Use defaults.
      }
      return EventTimelineCreator.createTimeline(width, height);
    }
  }

  private static final ContentTag[] contentTags = new ContentTag[] {
    new TimelineTag()
  };
}
