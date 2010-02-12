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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.lsp.ContentRenderer;

/**
 * Base rendering for any atom type.  Just wraps the content with a ContentRenderer.
 */
public class BaseAtomPreview extends Composite {

  private static BaseAtomPreviewUiBinder uiBinder = GWT.create(BaseAtomPreviewUiBinder.class);

  interface BaseAtomPreviewUiBinder extends UiBinder<Widget, BaseAtomPreview> {
  }

  @UiField SimplePanel content;

  public BaseAtomPreview(BaseAtom atom) {
    bind();
    content.add(new ContentRenderer(atom.getContent(), false));
  }

  protected void bind() {
    initWidget(uiBinder.createAndBindUi(this));    
  }
}
