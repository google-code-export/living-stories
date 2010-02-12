// ===================================================================
//
//   WARNING: GENERATED CODE! DO NOT EDIT!
//
// ===================================================================
/*
 This file generated from:

 src/com/google/fourthestate/gxps/Html.gxp
*/

package com.google.livingstories.gxps;

import com.google.gxp.base.GxpContext;
import com.google.gxp.html.HtmlClosure;

import java.io.IOException;

public class Html implements HtmlClosure {

  private String html;
  
  public Html(String html) {
    this.html = html;
  }
  
  public void write(Appendable gxp_out, GxpContext gxp_content) throws IOException {
    gxp_out.append(html);
  }
}