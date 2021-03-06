
//
package gov.nih.nlm.nls.metamap.lite;


import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;

import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.types.Annotation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bioc.BioCPassage;
import bioc.BioCSentence;

/**
 *
 */

public class SentenceExtractor 
{
  private static final Logger logger = LogManager.getLogger(SentenceExtractor.class);

  public static SentenceModel sentenceModel;
  public static SentenceDetectorME sentenceDetector;

  static {
    if (new File("data/models/en-sent.bin").exists()) {
      setModel(System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
    }
  }

  public static void setModel(String modelFilename)
  {
    InputStream modelIn = null;
    try {
      modelIn = new FileInputStream(modelFilename);
      sentenceModel = new SentenceModel(modelIn);
      sentenceDetector = new SentenceDetectorME(sentenceModel);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } 
    finally {
      if (modelIn != null) {
	try {
	  modelIn.close();
	} catch (IOException ioe) {
	  ioe.printStackTrace();
	} 
      }
    }
  }

  public static class SentenceImpl implements Sentence {
    Map<String, String> infons;
    String text;
    int offset;
    List<Annotation> annotationList = null;
    SentenceImpl(String id, String text, int offset) {
      this.text = text;
      this.offset = offset;
    }
    public Map<String, String> getInfons() {
      return this.infons;
    }

    public int getOffset() {
      return this.offset;
    }
    
    public String getText() {
      return this.text;
    }
    
    public List<Annotation> getAnnotations() {
      return this.annotationList;
    }

    public void addAnnotation(Annotation annotation) {
      if (this.annotationList == null) {
	this.annotationList = new ArrayList<Annotation>();
      }
      this.annotationList.add(annotation);
    }
  }
  
  public static List<Sentence> createSentenceList(String text) {
    logger.debug("createSentenceList");
    int sentenceCount = 0;
    int offset = 0;
    String[] sentenceArray = sentenceDetector.sentDetect(text);
    List<Sentence> sentenceList = new ArrayList<Sentence>();
    for (String sentenceText: sentenceArray) {
      sentenceList.add(new SentenceImpl("", sentenceText, offset));
      offset = offset + sentenceText.length();
      sentenceCount++;
    }
    return sentenceList;
  }

  public static List<Sentence> createSentenceList(String text, int offset) {
    logger.debug("createSentenceList");
    int sentenceCount = 0;
    String[] sentenceArray = sentenceDetector.sentDetect(text);
    List<Sentence> sentenceList = new ArrayList<Sentence>();
    for (String sentenceText: sentenceArray) {
      sentenceList.add(new SentenceImpl("", sentenceText, offset));
      offset = offset + sentenceText.length();
      sentenceCount++;
    }
    return sentenceList;
  }

  public static int addBioCSentence(BioCPassage passage,
				    String sentenceText, int offset, Map<String,String> infoNS) {
    BioCSentence sentence = new BioCSentence();
    sentence.setText(sentenceText);
    sentence.setOffset(offset);
    sentence.setInfons(infoNS);
    passage.addSentence(sentence);
    offset = offset + sentenceText.length() + 1;
    return offset;
  }

  public static BioCPassage createSentences(BioCPassage passage) {
    logger.debug("createSentenceList");
    int sentenceCount = 0;
    int offset = passage.getOffset();
    String[] sentenceArray = sentenceDetector.sentDetect(passage.getText());
    for (String sentenceText: sentenceArray) {
      // If sentence contains a semi-colon then split the sentence
      // into two utterances and add each to the passage, preserving
      // whitespace.
      int semicolonIndex = sentenceText.indexOf(";");
      if (semicolonIndex > 3) {
	String[] utteranceArray = sentenceText.split(";");
	for (String utteranceText: utteranceArray) {
	  offset = addBioCSentence(passage, utteranceText, offset, passage.getInfons());
	  sentenceCount++;
	}
      } else {
	offset = addBioCSentence(passage, sentenceText, offset, passage.getInfons());
	sentenceCount++;
      }
    }
    return passage;
  }
}
