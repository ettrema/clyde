package com.bradmcevoy.web.forms;

import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.forms.MultipleChoiceQaDef.Item;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 *
 * @author brad
 */
public final class QaXmlUtils {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( QaXmlUtils.class );

    public static Object parse( String xml ) throws JDOMException {
        if( StringUtils.isEmpty( xml ) ) {
            log.trace( "empty xml" );
            return null;
        }
        Document doc = new XmlUtils2().getJDomDocument( xml );
        Element elRoot = doc.getRootElement();
        if( !elRoot.getName().equals( "multiplechoiceqa" ) ) {
            log.warn( "root is not correct type. Should be multiplechoiceqa" );
            return null;
        }
        List<Item> items = new ArrayList<Item>();
        for( Element elQuestion : JDomUtils.children( elRoot ) ) {
            if( !elQuestion.getName().equals( "question" ) ) {
                log.warn( "Wrong element. Should be question, is a: " + elQuestion.getName() );
            } else {
                String questionHtml = JDomUtils.getInnerXmlOf( elQuestion, "html" );
                String sCorrect = elQuestion.getAttributeValue( "correct" );
                Integer correct = null;
                if( StringUtils.isBlank( sCorrect ) ) {
                    log.warn( "no correct answer given for: " + questionHtml );
                } else {
                    correct = Integer.parseInt( sCorrect );
                }
                List<String> answers = new ArrayList<String>();
                log.trace("add answers...");
                for( Element elAnswer : JDomUtils.childrenOf( elQuestion, "answers" ) ) {
                    log.trace("add answer");
                    String answerText = JDomUtils.getInnerXml( elAnswer );
                    answers.add( answerText );
                }
                Item i = new Item( questionHtml, correct, answers );
                items.add( i );
            }
        }
        return items;
    }

    public static String formatAsXml( Object oVal ) {
        log.trace( "formatValue" );
        List<Item> items = null;
        if( oVal instanceof List ) {
            items = (List<Item>) oVal;
        } else if( oVal == null ) {
            log.trace( "no items" );
            return "";
        } else {
            log.trace( "not compatible value:" + oVal.getClass() );
            return "";
        }

        Element e2 = new Element( "multiplechoiceqa" );
        Document doc = new Document( e2 );
        log.trace( "items: " + items.size() );
        for( Item i : items ) {
            Element elQ = new Element( "question" );
            e2.addContent( elQ );
            Element elQuestionHtml = new Element( "html" );
            elQ.addContent( elQuestionHtml );
            JDomUtils.setInnerXml( elQuestionHtml, i.getQuestion() );
            elQ.setAttribute( "correct", i.getCorrectNum() + "" );
            Element elAnswers = new Element( "answers" );
            elQ.addContent( elAnswers );
            for( String s : i.getAnswers() ) {
                Element elA = new Element( "answer" );
                elA.setText( s );
                elAnswers.addContent( elA );
            }
        }
        return new XmlUtils2().getXml( doc.getRootElement() );
    }
}
