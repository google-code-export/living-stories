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

package com.google.livingstories.client.contentmanager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

/**
 * Page that handles data import.
 * 
 * See comments in DataImportServlet.java for explanation of
 * how this page helps spread the import work across multiple
 * requests to avoid going over the appengine request timeout.
 */
public class ImportManager extends ManagerPane {
  private FormPanel fileUploadForm;
  private FormPanel progressForm;
  private HTML statusLabel;
  
  private int progress;
  
  public ImportManager() {
    final VerticalPanel contentPanel = new VerticalPanel();
    
    contentPanel.add(createFileUploadForm());
    contentPanel.add(createProgressForm());
    
    initWidget(contentPanel);
  }
  
  private Widget createFileUploadForm() {
    fileUploadForm = new FormPanel();
    fileUploadForm.setAction("/singletonImport");
    fileUploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
    fileUploadForm.setMethod(FormPanel.METHOD_POST);

    VerticalPanel fileUploadPanel = new VerticalPanel();
    fileUploadForm.setWidget(fileUploadPanel);
    
    FileUpload upload = new FileUpload();
    upload.setName("data");
    fileUploadPanel.add(upload);
    
    CheckBox override = new CheckBox("Override running servlet");
    override.setName("override");
    override.setFormValue("true");
    fileUploadPanel.add(override);
    
    Button submit = new Button("Submit");
    submit.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        fileUploadForm.submit();
        statusLabel.setHTML("Uploading...");
        progress = 0;
      }
    });
    fileUploadPanel.add(submit);
    
    fileUploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete(SubmitCompleteEvent e) {
        processResult(e.getResults());
      }
    });
    return fileUploadForm;
  }

  private Widget createProgressForm() {
    progressForm = new FormPanel();
    progressForm.setAction("/singletonImport");
    progressForm.setEncoding(FormPanel.ENCODING_URLENCODED);
    progressForm.setMethod(FormPanel.METHOD_POST);

    VerticalPanel progressPanel = new VerticalPanel();
    progressForm.setWidget(progressPanel);
    
    statusLabel = new HTML();
    progressPanel.add(statusLabel);

    progressForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete(SubmitCompleteEvent e) {
        processResult(e.getResults());
      }
    });
    return progressForm;
  }
  
  private void processResult(String result) {
    if (result.contains("RUNNING")) {
      progressForm.submit();
      StringBuilder sb = new StringBuilder(result);
      for (int i = 0; i < progress; i++) {
        sb.append(".");
      }
      statusLabel.setHTML(sb.toString());
      progress++;
    } else {
      statusLabel.setHTML(result);
    }
  }
}
