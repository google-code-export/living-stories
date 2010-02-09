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

package com.google.livingstories.server.util;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.livingstories.client.LivingStory;

import name.neil.fraser.plaintext.diff_match_patch;
import name.neil.fraser.plaintext.diff_match_patch.Diff;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that creates html for a diffed lsp summary based on the
 * supplied last visit time.  Assumes that all interesting text is in
 * paragraph tags, and ignores everything else.
 */
public class SummaryDiffUtil {
  private static final String HIGHLIGHT_CLASS = "summaryHighlights";
  private static final int EDIT_DISTANCE_THRESHOLD = 50;
  private static final Pattern BODY_CONTENT_PATTERN = 
      Pattern.compile(".*<body>(.*)</body>.*", Pattern.DOTALL);
  
  private static final Logger logger = Logger.getLogger(SummaryDiffUtil.class.getCanonicalName());

  public static String getDiffedSummary(LivingStory lsp, Date lastVisitTime) {
    // Short circuits
    if (lastVisitTime == null) {
      return lsp.getSummary();
    }

    String currentRevisionString = lsp.getSummary();
    String lastRevisionString = lsp.getLastSummaryRevisionBeforeTime(lastVisitTime);
    if (currentRevisionString.equals(lastRevisionString)) {
      return currentRevisionString;
    }
    
    // Ok, the revisions are different.  Start by parsing the HTML
    Tidy tidy = new Tidy();
    Document currentRevision = tidy.parseDOM(new StringReader(currentRevisionString), null);
    Document lastSeenRevision = tidy.parseDOM(new StringReader(lastRevisionString), null);

    // Get all the paragraphs in the old and new text
    List<Node> newParagraphs = Lists.newArrayList();
    List<Node> oldParagraphs = Lists.newArrayList();
    
    NodeList newParagraphNodeList = currentRevision.getElementsByTagName("p");
    for (int i = 0; i < newParagraphNodeList.getLength(); i++) {
      newParagraphs.add(newParagraphNodeList.item(i));
    }
    NodeList oldParagraphNodeList = lastSeenRevision.getElementsByTagName("p");
    for (int i = 0; i < oldParagraphNodeList.getLength(); i++) {
      oldParagraphs.add(oldParagraphNodeList.item(i));
    }
    
    // Remove paragraphs that appear in both the old and new text
    int paragraph = 0;
    while (paragraph < newParagraphs.size()) {
      Node newParagraph = newParagraphs.get(paragraph);
      boolean foundMatch = false;
      for (Node oldParagraph : oldParagraphs) {
        if (getTextContent(newParagraph).equals(getTextContent(oldParagraph))) {
          foundMatch = true;
          oldParagraphs.remove(oldParagraph);
          break;
        }
      }
      if (foundMatch) {
        newParagraphs.remove(paragraph);
      } else {
        paragraph++;
      }
    }
    
    // If there are still paragraphs left over, determine whether or not they should be highlighted.
    // Since we can't tell which new paragraph mapped to which old one to do a straight up diff,
    // this method gets the edit distance between each remaining new paragraph
    // and each remaining old paragraph.  It finds the minimum edit distance for each new
    // paragraph, and if it's higher than the threshold,  we highlight it.
    if (!newParagraphs.isEmpty()) {
      diff_match_patch dmp = new diff_match_patch();
      for (Node newParagraph : newParagraphs) {
        int minEditDistance = Integer.MAX_VALUE;
        for (Node oldParagraph : oldParagraphs) {
          LinkedList<Diff> diffs = dmp.diff_main(
              getTextContent(oldParagraph), getTextContent(newParagraph));
          minEditDistance = Math.min(minEditDistance, modifiedLevenshteinDistance(diffs));
        }
        if (minEditDistance > EDIT_DISTANCE_THRESHOLD) {
          Element paragraphElement = (Element) newParagraph;
          String className = paragraphElement.getAttribute("class");
          className = (className + " " + HIGHLIGHT_CLASS).trim();
          paragraphElement.setAttribute("class", className);
        }
      }
    }

    // Pretty print the resulting html.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    tidy.pprint(currentRevision, outputStream);
    Matcher matcher = BODY_CONTENT_PATTERN.matcher(outputStream.toString());
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      // Something went terribly wrong; this shouldn't happen.
      // Just return the current revision without doing any diffing or parsing.
      logger.warning("Failed to get diffed summary HTML for lsp " + lsp.getUrl()
          + " and timestamp " + DateFormat.getDateTimeInstance().format(lastVisitTime));
      return lsp.getSummary();
    }
  }

  // Need this because Node.getTextContent() is not implemented by JTidy's DOM
  // implementation.
  private static String getTextContent(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      StringBuilder sb = new StringBuilder();
      NodeList childNodes = node.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        sb.append(getTextContent(childNodes.item(i)));
      }
      return sb.toString();
    } else if (node.getNodeType() == Node.TEXT_NODE) {
      return node.getNodeValue();
    } else {
      return "";
    }
  }
  
  /**
   * This is a modified version of the levenshtein distance algorithm used
   * by the diff_match_patch library.  We change it so that we don't care
   * about deletions, and only calculate the distance based on additions
   * and substitutions.
   */
  private static int modifiedLevenshteinDistance(LinkedList<Diff> diffs) {
    int levenshtein = 0;
    int insertions = 0;
    int deletions = 0;
    for (Diff aDiff : diffs) {
      switch (aDiff.operation) {
      case INSERT:
        insertions += aDiff.text.length();
        break;
      case DELETE:
        deletions += aDiff.text.length();
        break;
      case EQUAL:
        // A deletion and an insertion is one substitution.
        // We don't care about pure deletes.
        if (insertions > 0) {
          levenshtein += Math.max(insertions, deletions);
        }
        insertions = 0;
        deletions = 0;
        break;
      }
    }
    // We don't care about pure deletes.
    if (insertions > 0) {
      levenshtein += Math.max(insertions, deletions);
    }
    return levenshtein;
  }
}
