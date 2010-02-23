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

package com.google.livingstories.client.ui.richtexttoolbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.lsp.SourceLink;
import com.google.livingstories.client.ui.SingleContentItemSelectionPanel;
import com.google.livingstories.client.util.RichTextUtil;

import java.util.EnumSet;
import java.util.Set;

/**
 * A toolbar for use with {@link RichTextArea}. It provides a simple UI
 * for all rich text formatting, dynamically displayed only for the available
 * functionality.
 * 
 * <p>A limited subset of the buttons will provide user feedback depending on
 * the cursor location.  Specifically, the following toggle buttons will appear
 * depressed if the cursor is over a portion of text that exhibits the
 * associated style:
 *
 * <ul>
 *   <li>BOLD_TOGGLE</li>
 *   <li>ITALIC_TOGGLE</li>
 *   <li>UNDERLINE_TOGGLE</li>
 *   <li>SUBSCRIPT_TOGGLE</li>
 *   <li>SUPERSCRIPT_TOGGLE</li>
 *   <li>STRIKETHROUGH_TOGGLE</li>
 * </ul>
 *
 *
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-RichTextToolbar { primary style }</li>
 * <li>.gwt-BoldToggle { on the BoldToggle control }</li>
 * <li>.gwt-ItalicToggle { on the ItalicToggle control }</li>
 * <li>.gwt-UnderlineToggle { on the UnderlineToggle control }</li>
 * <li>.gwt-SubscriptToggle { on the SubscriptToggle control }</li>
 * <li>.gwt-SuperscriptToggle { on the SuperscriptToggle control }</li>
 * <li>.gwt-JustifyLeftButton { on the JustifyLeftButton control }</li>
 * <li>.gwt-JustifyCenterButton { on the JustifyCenterButton control }</li>
 * <li>.gwt-JustifyRightButton { on the JustifyRightButton control }</li>
 * <li>.gwt-StrikethroughButton { on the StrikethroughButton control }</li>
 * <li>.gwt-IndentButton { on the IndentButton control }</li>
 * <li>.gwt-OutdentButton { on the OutdentButton control }</li>
 * <li>.gwt-HorizontalRuleButton { on the HorizontalRuleButton control }</li>
 * <li>.gwt-OrderedListButton { on the OrderedListButton control }</li>
 * <li>.gwt-UnorderedListButton { on the UnorderedListButton control }</li>
 * <li>.gwt-InsertImageButton { on the InsertImageButton control }</li>
 * <li>.gwt-CreateLinkButton { on the CreateLinkButton control }</li>
 * <li>.gwt-RemoveLinkButton { on the RemoveLinkButton control }</li>
 * <li>.gwt-RemoveFormatButton { on the RemoveFormatButton control }</li>
 * <li>.gwt-hasRichTextToolbar { added to associated RichTextArea }</li>
 * </ul>
 */
public class RichTextToolbar extends Composite {

  /** I18n messages. */
  private static final RichTextMessages MESSAGES = GWT.create(RichTextMessages.class);

  private static final RichTextArea.FontSize[] fontSizesConstants = new RichTextArea.FontSize[] {
    RichTextArea.FontSize.XX_SMALL,
    RichTextArea.FontSize.X_SMALL,
    RichTextArea.FontSize.SMALL,
    RichTextArea.FontSize.MEDIUM,
    RichTextArea.FontSize.LARGE,
    RichTextArea.FontSize.X_LARGE,
    RichTextArea.FontSize.XX_LARGE
  };

  private Images images = (Images) GWT.create(Images.class);
  private RichTextUtil richTextUtil = GWT.create(RichTextUtil.class);

  private RichTextArea richText;
  private RichTextArea.BasicFormatter basic;
  private RichTextArea.ExtendedFormatter extended;

  // All of these controls can be null, if the creator so chooses, so always
  // null-check them before attempting to access them
  private ToggleButton bold;
  private ToggleButton italic;
  private ToggleButton underline;
  private ToggleButton subscript;
  private ToggleButton superscript;
  private ToggleButton strikethrough;
  private PushButton indent;
  private PushButton outdent;
  private PushButton justifyLeft;
  private PushButton justifyCenter;
  private PushButton justifyRight;
  private PushButton hr;
  private PushButton ol;
  private PushButton ul;
  private PushButton insertImage;
  private PushButton insertGoToContentItem;
  private PushButton insertContentItem;
  private PushButton insertSource;
  private PushButton insertLightbox;
  private PushButton createLink;
  private PushButton removeLink;
  private PushButton removeFormat;
  private ListBox backColors;
  private ListBox foreColors;
  private ListBox fonts;
  private ListBox fontSizes;

  /**
   * Creates a new toolbar that drives the given rich text area, provides all
   * available controls, and divides the toolbar into two rows.
   * 
   * @param richText the {@link RichTextArea} to be controlled
   */
  public RichTextToolbar(RichTextArea richText) {
    this(richText, true);
  }

  /**
   * Creates a new toolbar that drives the given rich text area, provides the
   * given controls, and divides the toolbar into two rows.  If you prefer to
   * specify which controls are disabled (and keep the rest), then use
   * {@link EnumSet#complementOf} or the combination of
   * {@link EnumSet#allOf} and {@link EnumSet#remove}.
   * 
   * @param richText the {@link RichTextArea} to be controlled
   * @param controls the set of enabled controls
   */
  public RichTextToolbar(RichTextArea richText, Set<Control> controls) {
    this(richText, controls, true);
  }

  /**
   * Creates a new toolbar that drives the given rich text area, provides all
   * available controls, and optionally divides the toolbar into two rows.
   * 
   * @param richText the {@link RichTextArea} to be controlled
   * @param divideToolbar whether or not to divide the toolbar
   */
  public RichTextToolbar(RichTextArea richText, boolean divideToolbar) {
    this(richText, EnumSet.allOf(Control.class), divideToolbar);
  }

  /**
   * Creates a new toolbar that drives the given rich text area, provides the
   * given controls, and optionally divides the toolbar into two rows.  If you
   * prefer to specify which controls are disabled (and keep the rest), then
   * use {@link EnumSet#complementOf} or the combination of
   * {@link EnumSet#allOf} and {@link EnumSet#remove}.
   *
   * <p>The {@code divideToolbar} argument, if true, will divide the toolbar into
   * two sections - one for buttons, and one for the lists.  If false, both
   * sections will be on the same row.  With all buttons enabled, a single-row
   * toolbar can take upwards of 800 pixels of horizontal space.
   *
   * <p>If {@code controls} is null, then all available controls are enabled.
   * 
   * @param richText the {@link RichTextArea} to be controlled
   * @param controls the set of enabled controls
   * @param divideToolbar whether or not to divide the toolbar
   */
  public RichTextToolbar(RichTextArea richText, Set<Control> controls, boolean divideToolbar) {
    this.richText = richText;
    this.basic = richText.getBasicFormatter();
    this.extended = richText.getExtendedFormatter();

    boolean hasToggle = false;
    boolean hasTopPanel = false;
    boolean hasBottomPanel = false;

    HorizontalPanel topPanel = new HorizontalPanel();
    HorizontalPanel bottomPanel = null;
    VerticalPanel outer = null;

    if (divideToolbar) {
      bottomPanel = new HorizontalPanel();
      outer = new VerticalPanel();
      outer.add(topPanel);
      outer.add(bottomPanel);
    }

    if (basic != null) {
      if (controls.contains(Control.BOLD_TOGGLE)) {
        bold = createToggleButton(images.bold(), MESSAGES.bold());
        addControlToToolbar(topPanel, bold, "gwt-BoldToggle");
        hasTopPanel = true;
        hasToggle = true;
      }

      if (controls.contains(Control.ITALIC_TOGGLE)) {
        italic = createToggleButton(images.italic(), MESSAGES.italic());
        addControlToToolbar(topPanel, italic, "gwt-ItalicToggle");
        hasTopPanel = true;
        hasToggle = true;
      }

      if (controls.contains(Control.UNDERLINE_TOGGLE)) {
        underline = createToggleButton(images.underline(), MESSAGES.underline());
        addControlToToolbar(topPanel, underline, "gwt-UnderlineToggle");
        hasTopPanel = true;
        hasToggle = true;
      }

      if (controls.contains(Control.SUBSCRIPT_TOGGLE)) {
        subscript = createToggleButton(images.subscript(), MESSAGES.subscript());
        addControlToToolbar(topPanel, subscript, "gwt-SubscriptToggle");
        hasTopPanel = true;
        hasToggle = true;
      }

      if (controls.contains(Control.SUPERSCRIPT_TOGGLE)) {
        superscript = createToggleButton(images.superscript(), MESSAGES.superscript());
        addControlToToolbar(topPanel, superscript, "gwt-SuperscriptToggle");
        hasTopPanel = true;
        hasToggle = true;
      }

      if (controls.contains(Control.JUSTIFY_LEFT_BUTTON)) {
        justifyLeft = createPushButton(images.justifyLeft(), MESSAGES.justifyLeft());
        addControlToToolbar(topPanel, justifyLeft, "gwt-JustifyLeftButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.JUSTIFY_CENTER_BUTTON)) {
        justifyCenter = createPushButton(images.justifyCenter(), MESSAGES.justifyCenter());
        addControlToToolbar(topPanel, justifyCenter, "gwt-JustifyCenterButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.JUSTIFY_RIGHT_BUTTON)) {
        justifyRight = createPushButton(images.justifyRight(), MESSAGES.justifyRight());
        addControlToToolbar(topPanel, justifyRight, "gwt-JustifyRightButton");
        hasTopPanel = true;
      }
    }

    if (extended != null) {
      if (controls.contains(Control.STRIKETHROUGH_TOGGLE)) {
        strikethrough = createToggleButton(images.strikeThrough(), MESSAGES.strikeThrough());
        addControlToToolbar(topPanel, strikethrough, "gwt-StrikethroughButton");
        hasTopPanel = true;
        hasToggle = true;
      }

      if (controls.contains(Control.INDENT_BUTTON)) {
        indent = createPushButton(images.indent(), MESSAGES.indent());
        addControlToToolbar(topPanel, indent, "gwt-IndentButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.OUTDENT_BUTTON)) {
        outdent = createPushButton(images.outdent(), MESSAGES.outdent());
        addControlToToolbar(topPanel, outdent, "gwt-OutdentButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.HORIZONTAL_RULE_BUTTON)) {
        hr = createPushButton(images.hr(), MESSAGES.hr());
        addControlToToolbar(topPanel, hr, "gwt-HorizontalRuleButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.ORDERED_LIST_BUTTON)) {
        ol = createPushButton(images.ol(), MESSAGES.ol());
        addControlToToolbar(topPanel, ol, "gwt-OrderedListButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.UNORDERED_LIST_BUTTON)) {
        ul = createPushButton(images.ul(), MESSAGES.ul());
        addControlToToolbar(topPanel, ul, "gwt-UnorderedListButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.INSERT_IMAGE_BUTTON)) {
        insertImage = createPushButton(images.insertImage(), MESSAGES.insertImage());
        addControlToToolbar(topPanel, insertImage, "gwt-InsertImageButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.INSERT_GO_TO_CONTENT_ITEM_BUTTON)) {
        insertGoToContentItem =
            createPushButton(images.insertGoToContentItem(), MESSAGES.insertGoToContentItem());
        addControlToToolbar(topPanel, insertGoToContentItem, "gwt-InsertGoToContentItemButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.INSERT_CONTENT_ITEM_BUTTON)) {
        insertContentItem =
            createPushButton(images.insertContentItem(), MESSAGES.insertContentItem());
        addControlToToolbar(topPanel, insertContentItem, "gwt-InsertContentItemButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.INSERT_SOURCE_BUTTON)) {
        insertSource = createPushButton(images.insertSource(), MESSAGES.insertSource());
        addControlToToolbar(topPanel, insertSource, "gwt-InsertSourceButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.INSERT_LIGHTBOX_BUTTON)) {
        insertLightbox = createPushButton(images.insertLightbox(), MESSAGES.insertLightbox());
        addControlToToolbar(topPanel, insertLightbox, "gwt-InsertLightboxButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.CREATE_LINK_BUTTON)) {
        createLink = createPushButton(images.createLink(), MESSAGES.createLink());
        addControlToToolbar(topPanel, createLink, "gwt-CreateLinkButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.REMOVE_LINK_BUTTON)) {
        removeLink = createPushButton(images.removeLink(), MESSAGES.removeLink());
        addControlToToolbar(topPanel, removeLink, "gwt-RemoveLinkButton");
        hasTopPanel = true;
      }

      if (controls.contains(Control.REMOVE_FORMAT_BUTTON)) {
        removeFormat = createPushButton(images.removeFormat(), MESSAGES.removeFormat());
        addControlToToolbar(topPanel, removeFormat, "gwt-RemoveFormatButton");
        hasTopPanel = true;
      }
    }

    if (basic != null) {
      HorizontalPanel panelToAddTo = bottomPanel != null ? bottomPanel : topPanel;
      if (controls.contains(Control.BACK_COLORS_LIST)) {
        backColors = createColorList(MESSAGES.background());
        addControlToToolbar(panelToAddTo, backColors, "gwt-BackColorsList");
        hasBottomPanel = true;
      }

      if (controls.contains(Control.FORE_COLORS_LIST)) {
        foreColors = createColorList(MESSAGES.foreground());
        addControlToToolbar(panelToAddTo, foreColors, "gwt-ForeColorsList");
        hasBottomPanel = true;
      }

      if (controls.contains(Control.FONTS_LIST)) {
        fonts = createFontList();
        addControlToToolbar(panelToAddTo, fonts, "gwt-FontsList");
        hasBottomPanel = true;
      }

      if (controls.contains(Control.FONT_SIZES_LIST)) {
        fontSizes = createFontSizes();
        addControlToToolbar(panelToAddTo, fontSizes, "gwt-FontSizesList");
        hasBottomPanel = true;
      }

      // We only use these listeners for updating status, so don't hook them up
      // unless at least basic editing is supported, and unless we have at
      // least one toggle button.
      if (hasToggle) {
        richText.addKeyUpHandler(new ToolbarKeyUpHandler());
        richText.addClickHandler(new ToolbarClickHandler());
      }
    }

    if (divideToolbar) {
      if (hasTopPanel && hasBottomPanel) {
        initWidget(outer);
      } else if (hasBottomPanel) {
        initWidget(bottomPanel);
      } else { // hasTopPanel (or has no panel)
        initWidget(topPanel);
      }
    } else { // divideToolbar == false
      // Also covers the case where they have no controls at all
      initWidget(topPanel);
    }

    setStyleName("gwt-RichTextToolbar");
    richText.addStyleName("gwt-hasRichTextToolbar");
  }

  private ListBox createColorList(String caption) {
    ListBox lb = new ListBox();
    lb.addChangeHandler(new ToolbarChangeHandler());
    lb.setVisibleItemCount(1);

    lb.addItem(caption);
    lb.addItem(MESSAGES.white(), "white");
    lb.addItem(MESSAGES.black(), "black");
    lb.addItem(MESSAGES.red(), "red");
    lb.addItem(MESSAGES.green(), "green");
    lb.addItem(MESSAGES.yellow(), "yellow");
    lb.addItem(MESSAGES.blue(), "blue");
    return lb;
  }

  private ListBox createFontList() {
    ListBox lb = new ListBox();
    lb.addChangeHandler(new ToolbarChangeHandler());
    lb.setVisibleItemCount(1);

    lb.addItem(MESSAGES.font(), "");
    lb.addItem(MESSAGES.normal(), "");
    lb.addItem(MESSAGES.timesNewRoman(), "Times New Roman");
    lb.addItem(MESSAGES.arial(), "Arial");
    lb.addItem(MESSAGES.courierNew(), "Courier New");
    lb.addItem(MESSAGES.georgia(), "Georgia");
    lb.addItem(MESSAGES.trebuchet(), "Trebuchet");
    lb.addItem(MESSAGES.verdana(), "Verdana");
    return lb;
  }

  private ListBox createFontSizes() {
    ListBox lb = new ListBox();
    lb.addChangeHandler(new ToolbarChangeHandler());
    lb.setVisibleItemCount(1);

    lb.addItem(MESSAGES.size());
    lb.addItem(MESSAGES.xxsmall());
    lb.addItem(MESSAGES.xsmall());
    lb.addItem(MESSAGES.small());
    lb.addItem(MESSAGES.medium());
    lb.addItem(MESSAGES.large());
    lb.addItem(MESSAGES.xlarge());
    lb.addItem(MESSAGES.xxlarge());
    return lb;
  }

  private PushButton createPushButton(AbstractImagePrototype img, String tip) {
    PushButton pb = new PushButton(img.createImage());
    pb.addClickHandler(new ToolbarClickHandler());
    pb.setTitle(tip);
    return pb;
  }

  private ToggleButton createToggleButton(AbstractImagePrototype img, String tip) {
    ToggleButton tb = new ToggleButton(img.createImage());
    tb.addClickHandler(new ToolbarClickHandler());
    tb.setTitle(tip);
    return tb;
  }

  private void addControlToToolbar(HorizontalPanel toolbar, Widget w, String styleName) {
    w.addStyleName(styleName);
    toolbar.add(w);
  }

  /**
   * Updates the status of all the stateful buttons.
   */
  private void updateStatus() {
    if (bold != null) {
      bold.setDown(basic.isBold());
    }

    if (italic != null) {
      italic.setDown(basic.isItalic());
    }

    if (underline != null) {
      underline.setDown(basic.isUnderlined());
    }

    if (subscript != null) {
      subscript.setDown(basic.isSubscript());
    }

    if (superscript != null) {
      superscript.setDown(basic.isSuperscript());
    }

    if (strikethrough != null) {
      strikethrough.setDown(extended.isStrikethrough());
    }
  }

  /**
   * The possible controls that are supported by this toolbar implementation.
   */
  public enum Control {
    BOLD_TOGGLE,
    ITALIC_TOGGLE,
    UNDERLINE_TOGGLE,
    SUBSCRIPT_TOGGLE,
    SUPERSCRIPT_TOGGLE,
    STRIKETHROUGH_TOGGLE,
    INDENT_BUTTON,
    OUTDENT_BUTTON,
    JUSTIFY_LEFT_BUTTON,
    JUSTIFY_CENTER_BUTTON,
    JUSTIFY_RIGHT_BUTTON,
    HORIZONTAL_RULE_BUTTON,
    ORDERED_LIST_BUTTON,
    UNORDERED_LIST_BUTTON,
    INSERT_IMAGE_BUTTON,
    INSERT_GO_TO_CONTENT_ITEM_BUTTON,
    INSERT_CONTENT_ITEM_BUTTON,
    INSERT_SOURCE_BUTTON,
    INSERT_LIGHTBOX_BUTTON,
    CREATE_LINK_BUTTON,
    REMOVE_LINK_BUTTON,
    REMOVE_FORMAT_BUTTON,
    BACK_COLORS_LIST,
    FORE_COLORS_LIST,
    FONTS_LIST,
    FONT_SIZES_LIST
  }
  
  private class ToolbarClickHandler implements ClickHandler {
    public void onClick(ClickEvent event) {
      Object sender = event.getSource();
      if (sender == bold) {
        basic.toggleBold();
      } else if (sender == italic) {
        basic.toggleItalic();
      } else if (sender == underline) {
        basic.toggleUnderline();
      } else if (sender == subscript) {
        basic.toggleSubscript();
      } else if (sender == superscript) {
        basic.toggleSuperscript();
      } else if (sender == strikethrough) {
        extended.toggleStrikethrough();
      } else if (sender == indent) {
        extended.rightIndent();
      } else if (sender == outdent) {
        extended.leftIndent();
      } else if (sender == justifyLeft) {
        basic.setJustification(RichTextArea.Justification.LEFT);
      } else if (sender == justifyCenter) {
        basic.setJustification(RichTextArea.Justification.CENTER);
      } else if (sender == justifyRight) {
        basic.setJustification(RichTextArea.Justification.RIGHT);
      } else if (sender == insertImage) {
        String url = Window.prompt(MESSAGES.enterImageUrl(), "http://");
        if (url != null) {
          extended.insertImage(url);
        }
      } else if (sender == insertGoToContentItem) {
        PopupPanel popup = new PopupPanel();
        FlowPanel contentPanel = new FlowPanel();
        final SingleContentItemSelectionPanel selectionPanel =
            new SingleContentItemSelectionPanel();
        contentPanel.add(selectionPanel);
        contentPanel.add(createButtonPanel(popup, new ContentItemSelectionHandler() {
          @Override
          public void onSelect() {
            BaseContentItem contentItem = selectionPanel.getSelection();
            String headline = null;
            if (contentItem.getContentItemType() == ContentItemType.EVENT) {
              headline = ((EventContentItem) contentItem).getEventUpdate();
            } else if (contentItem.getContentItemType() == ContentItemType.NARRATIVE) {
              headline = ((NarrativeContentItem) contentItem).getHeadline();
            } else {
              Window.alert("You must link to an event or top level narrative!");
              contentItem = null;
            }
            if (contentItem != null && contentItem.displayTopLevel()) {
              richTextUtil.createJavascriptLink(richText.getElement(),
                  "goToContentItem(" + contentItem.getId() + ")",
                  "Jump to: " + headline.replace("\"", "'"));
            }
          }
        }));
        popup.add(contentPanel);
        popup.showRelativeTo(insertContentItem);
      } else if (sender == insertContentItem) {
        PopupPanel popup = new PopupPanel();
        FlowPanel contentPanel = new FlowPanel();
        final SingleContentItemSelectionPanel selectionPanel =
            new SingleContentItemSelectionPanel();
        contentPanel.add(selectionPanel);
        contentPanel.add(createButtonPanel(popup, new ContentItemSelectionHandler() {
          @Override
          public void onSelect() {
            BaseContentItem contentItem = selectionPanel.getSelection();
            if (contentItem != null) {
              richTextUtil.createJavascriptLink(richText.getElement(),
                  "showContentItemPopup(" + contentItem.getId() + ", this)");
            }
          }
        }));
        popup.add(contentPanel);
        popup.showRelativeTo(insertContentItem);
      } else if (sender == insertSource) {
        PopupPanel popup = new PopupPanel();
        FlowPanel contentPanel = new FlowPanel();
        final TextBox descriptionBox = new TextBox();
        HorizontalPanel descriptionPanel = new HorizontalPanel();
        descriptionPanel.add(new Label("Source description:"));
        descriptionPanel.add(descriptionBox);
        contentPanel.add(descriptionPanel);
        final SingleContentItemSelectionPanel selectionPanel =
            new SingleContentItemSelectionPanel();
        contentPanel.add(selectionPanel);
        contentPanel.add(createButtonPanel(popup, new ContentItemSelectionHandler() {
          @Override
          public void onSelect() {
            String description = descriptionBox.getText();
            BaseContentItem contentItem = selectionPanel.getSelection();
            if (!description.isEmpty() || contentItem != null) {
              String selectedText = richTextUtil.getSelection(richText.getElement());
              richTextUtil.insertHTML(richText.getElement(), selectedText + " " +
                  new SourceLink(description, contentItem == null ? -1
                      : contentItem.getId()).getOuterHTML());
            }
          }
        }));
        popup.add(contentPanel);
        popup.showRelativeTo(insertSource);
      } else if (sender == insertLightbox) {
        PopupPanel popup = new PopupPanel();
        FlowPanel contentPanel = new FlowPanel();
        final SingleContentItemSelectionPanel selectionPanel =
            new SingleContentItemSelectionPanel();
        contentPanel.add(selectionPanel);
        contentPanel.add(createButtonPanel(popup, new ContentItemSelectionHandler() {
          @Override
          public void onSelect() {
            BaseContentItem contentItem = selectionPanel.getSelection();
            if (contentItem != null) {
              richTextUtil.createJavascriptLink(richText.getElement(),
                  "showLightboxForContentItem('" + contentItem.getTypeString()
                  + "', " + contentItem.getId() + ")");
            }
          }
        }));
        popup.add(contentPanel);
        popup.showRelativeTo(insertLightbox);
      } else if (sender == createLink) {
        String url = Window.prompt(MESSAGES.enterLinkUrl(), "http://");
        if (url != null) {
          extended.createLink(url);
        }
      } else if (sender == removeLink) {
        extended.removeLink();
      } else if (sender == hr) {
        extended.insertHorizontalRule();
      } else if (sender == ol) {
        extended.insertOrderedList();
      } else if (sender == ul) {
        extended.insertUnorderedList();
      } else if (sender == removeFormat) {
        extended.removeFormat();
      } else if (sender == richText) {
        // We use the RichTextArea's onKeyUp event to update the toolbar status.
        // This will catch any cases where the user moves the cursur using the
        // keyboard, or uses one of the browser's built-in keyboard shortcuts.
        updateStatus();
      }
    }

    private Widget createButtonPanel(final PopupPanel popup,
        final ContentItemSelectionHandler handler) {
      Button okButton = new Button("Ok");
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handler.onSelect();
          popup.hide();
        }
      });
      Button cancelButton = new Button("Cancel");
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          popup.hide();
        }
      });
      
      HorizontalPanel buttons = new HorizontalPanel();
      buttons.add(okButton);
      buttons.add(cancelButton);      
      return buttons;
    }
  }
    
  private class ToolbarChangeHandler implements ChangeHandler {
    public void onChange(ChangeEvent event) {
      Object sender = event.getSource();
      if (sender == backColors) {
        basic.setBackColor(backColors.getValue(backColors.getSelectedIndex()));
        backColors.setSelectedIndex(0);
      } else if (sender == foreColors) {
        basic.setForeColor(foreColors.getValue(foreColors.getSelectedIndex()));
        foreColors.setSelectedIndex(0);
      } else if (sender == fonts) {
        basic.setFontName(fonts.getValue(fonts.getSelectedIndex()));
        fonts.setSelectedIndex(0);
      } else if (sender == fontSizes) {
        basic.setFontSize(fontSizesConstants[fontSizes.getSelectedIndex() - 1]);
        fontSizes.setSelectedIndex(0);
      }
    }
  }
  
  private class ToolbarKeyUpHandler implements KeyUpHandler {
    public void onKeyUp(KeyUpEvent event) {
      if (event.getSource() == richText) {
        // We use the RichTextArea's onKeyUp event to update the toolbar status.
        // This will catch any cases where the user moves the cursur using the
        // keyboard, or uses one of the browser's built-in keyboard shortcuts.
        updateStatus();
      }
    }
  }

  private interface ContentItemSelectionHandler {
    void onSelect();
  }

  /**
   * This {@link ImageBundle} is used for all the button icons. Using an image
   * bundle allows all of these images to be packed into a single image, which
   * saves a lot of HTTP requests, drastically improving startup time.
   */
  public interface Images extends ImageBundle {
    AbstractImagePrototype bold();
    AbstractImagePrototype createLink();
    AbstractImagePrototype hr();
    AbstractImagePrototype indent();
    AbstractImagePrototype insertImage();
    AbstractImagePrototype insertGoToContentItem();
    AbstractImagePrototype insertContentItem();
    AbstractImagePrototype insertSource();
    AbstractImagePrototype insertLightbox();
    AbstractImagePrototype italic();
    AbstractImagePrototype justifyCenter();
    AbstractImagePrototype justifyLeft();
    AbstractImagePrototype justifyRight();
    AbstractImagePrototype ol();
    AbstractImagePrototype outdent();
    AbstractImagePrototype removeFormat();
    AbstractImagePrototype removeLink();
    AbstractImagePrototype strikeThrough();
    AbstractImagePrototype subscript();
    AbstractImagePrototype superscript();
    AbstractImagePrototype ul();
    AbstractImagePrototype underline();
  }

}
