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

import com.google.gwt.i18n.client.Messages;

/**
 * Translatable strings for base Profiles classes.
 */
public interface RichTextMessages extends Messages {

  @DefaultMessage("Toggle Bold")
  @Description("Tool tip for a button that toggles whether the selected text is bold")
  String bold();

  @DefaultMessage("Toggle Italic")
  @Description("Tool tip for a button that toggles whether the selected text is italic")
  String italic();

  @DefaultMessage("Toggle Underline")
  @Description("Tool tip for a button that toggles whether the selected text is underlined")
  String underline();

  @DefaultMessage("Toggle Strikethrough")
  @Description("Tool tip for a button that toggles whether the selected text is struck through")
  String strikeThrough();

  @DefaultMessage("Toggle Subscript")
  @Description("Tool tip for a button that toggles whether the selected text is subscripted "
               + "(lowered and reduced in size)")
  String subscript();

  @DefaultMessage("Toggle Superscript")
  @Description("Tool tip for a button that toggles whether the selected text is superscripted "
               + "(raised and reduced in size)")
  String superscript();

  @DefaultMessage("Indent Right")
  @Description("Tool tip for a button that indents text to the right")
  String indent();

  @DefaultMessage("Indent Left")
  @Description("Tool tip for a button that un-indents text (i.e. indents to the left)")
  String outdent();

  @DefaultMessage("Insert Horizontal Rule")
  @Description("Tool tip for a button that inserts a horizontal rule into the text")
  String hr();

  @DefaultMessage("Center")
  @Description("Tool tip for a button that centers the text")
  String justifyCenter();

  @DefaultMessage("Left Justify")
  @Description("Tool tip for a button that aligns the text on the left")
  String justifyLeft();

  @DefaultMessage("Right Justify")
  @Description("Tool tip for a button that aligns the text on the right")
  String justifyRight();

  @DefaultMessage("Insert Ordered List")
  @Description("Tool tip for a button that creates an ordered list")
  String ol();

  @DefaultMessage("Insert Unordered List")
  @Description("Tool tip for a button that creates an unordered list")
  String ul();

  @DefaultMessage("Create Link")
  @Description("Tool tip for a button that allows the user to a create a link in their text")
  String createLink();

  @DefaultMessage("Enter an link URL:")
  @Description("Label for a text field to input a link URL")
  String enterLinkUrl();

  @DefaultMessage("Remove Link")
  @Description("Tool tip for a button that removes the selected link")
  String removeLink();

  @DefaultMessage("Insert Image")
  @Description("Tool tip for a button that inserts an image into the document")
  String insertImage();

  @DefaultMessage("Enter an image URL:")
  @Description("Label for a text field to input an image URL")
  String enterImageUrl();

  @DefaultMessage("Insert Go To Content Entity Link")
  @Description("Tool tip for a button that inserts a goToAtom link into the document")
  String insertGoToAtom();

  @DefaultMessage("Insert Content Popup Link")
  @Description("Tool tip for a button that inserts an atom link into the document")
  String insertAtom();

  @DefaultMessage("Insert Source Link")
  @Description("Tool tip for a button that inserts an source link into the document")
  String insertSource();

  @DefaultMessage("Insert Lightbox Link")
  @Description("Tool tip for a button that inserts an lightbox link into the document")
  String insertLightbox();

  @DefaultMessage("Remove Formatting")
  @Description("Tool tip for a button that removes formatting from the selected text")
  String removeFormat();

  @DefaultMessage("Background")
  @Description("Label for a combo box that allows the user to select the background color of "
               + "their text")
  String background();

  @DefaultMessage("Foreground")
  @Description("Label for a combo box that allows the user to select the foreground color of "
               + "their text")
  String foreground();

  @DefaultMessage("Black")
  @Description("The color black")
  String black();

  @DefaultMessage("White")
  @Description("The color white")
  String white();

  @DefaultMessage("Red")
  @Description("The color red")
  String red();

  @DefaultMessage("Green")
  @Description("The color green")
  String green();

  @DefaultMessage("Yellow")
  @Description("The color yellow")
  String yellow();

  @DefaultMessage("Blue")
  @Description("The color blue")
  String blue();

  @DefaultMessage("Size")
  @Description("Label for a combo box that allows the user to select the size of their text")
  String size();

  @DefaultMessage("XX-Small")
  @Description("Selects a very very small text size")
  String xxsmall();

  @DefaultMessage("X-Small")
  @Description("Selects a very small text size")
  String xsmall();

  @DefaultMessage("Small")
  @Description("Selects a small text size")
  String small();

  @DefaultMessage("Medium")
  @Description("Selects a medium text size")
  String medium();

  @DefaultMessage("Large")
  @Description("Selects a large text size")
  String large();

  @DefaultMessage("X-Large")
  @Description("Selects a very large text size")
  String xlarge();

  @DefaultMessage("XX-Large")
  @Description("Selects a very very large text size")
  String xxlarge();

  @DefaultMessage("Font")
  @Description("Label for a combo box that allows the user to select the font of their text")
  String font();

  @DefaultMessage("Normal")
  @Description("Selects the default style of text")
  String normal();

  @DefaultMessage("Times New Roman")
  @Description("Selects the Times New Roman font")
  String timesNewRoman();

  @DefaultMessage("Arial")
  @Description("Selects the Arial font")
  String arial();

  @DefaultMessage("Courier New")
  @Description("Selects the Courier New font")
  String courierNew();

  @DefaultMessage("Georgia")
  @Description("Selects the Georgia font")
  String georgia();

  @DefaultMessage("Trebuchet")
  @Description("Selects the Trebuchet font")
  String trebuchet();

  @DefaultMessage("Verdana")
  @Description("Selects the Verdana font")
  String verdana();
}
