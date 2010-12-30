package com.bradmcevoy.web.forms;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class MultipleChoiceQaDef implements ComponentDef {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MultipleChoiceQaDef.class );
    private static final long serialVersionUID = 1L;
    private String name;
    private Addressable container;

    public MultipleChoiceQaDef( Template res, String name ) {
        this.container = res;
        this.name = name;
    }

    public void init( Addressable container ) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate( RenderContext rc ) {
        return true; // todo
    }

    public String render( RenderContext rc ) {
        return "";
    }

    public String renderEdit( RenderContext rc ) {
        return "";
    }

    public String getName() {
        return name;
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        return null;
    }

    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "componentDef" );
        el.addContent( e2 );
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", getName() );

        return e2;
    }

    // These are the per value methods
    public boolean validate( ComponentValue c, RenderContext rc ) {
        return true; // todo
    }

    public String render( ComponentValue c, RenderContext rc ) {
        log.trace( "render" );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter w = new XmlWriter( out );
        XmlWriter.Element elOuterOl = w.begin( "ol" );
        int questionNum = 0;
        List<Item> items = null;
        Object oVal = c.getValue();
        if( oVal instanceof List ) {
            items = (List<Item>) oVal;
        }
        if( items != null ) {
            for( Item i : items ) {
                questionNum++;
                XmlWriter.Element elItem = elOuterOl.begin( "li" );
                elItem.writeText( i.getQuestion() );
                XmlWriter.Element elInnerOl = w.begin( "ol" );
                int answerNum = 0;
                for( String s : i.getAnswers() ) {
                    answerNum++;
                    XmlWriter.Element elAnswerLi = w.begin( "li" );
                    // <input type="radio" name="q1" value="a1" id="q1a1"/>
                    String id = "q" + questionNum + "a" + answerNum;
                    elAnswerLi.begin( "input" ).writeAtt( "type", "radio" ).writeAtt( "name", "q" + questionNum ).writeAtt( "value", answerNum + "" ).writeAtt( "id", id ).close();

                    // <label for="q1a1">anaesthetic</label>
                    elAnswerLi.begin( "label" ).writeAtt( "for", id ).writeText( s ).close();

                    elAnswerLi.close();
                }
                elInnerOl.close();
                elItem.close();
            }
        } else {
            log.trace( "items is null" );
        }
        elOuterOl.close();

        w.flush();
        return out.toString();

    }

    public String renderEdit( ComponentValue c, RenderContext rc ) {
        log.trace( "renderEdit" );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter w = new XmlWriter( out );
        XmlWriter.Element elOuterOl = w.begin( "ol" );
        int questionNum = 0;
        List<Item> items = null;
        Object oVal = c.getValue();
        if( oVal == null ) {
            log.trace( "items is null" );
        } else if( oVal instanceof List ) {
            items = (List<Item>) oVal;
        } else {
            log.warn( "unsupported type for value: " + oVal.getClass() );
        }
        if( items != null && items.size() > 0 ) {
            for( Item i : items ) {
                writeQuestion( elOuterOl, questionNum, i.getQuestion(), i.getAnswers(), w );
                questionNum++;
            }
        } else {
            writeQuestion( elOuterOl, questionNum, "", null, w );
        }
        elOuterOl.close();

        w.flush();
        return out.toString();
    }

    private void writeQuestion( XmlWriter.Element elOuterOl, int questionNum, String question, List<String> answers, XmlWriter w ) {
        XmlWriter.Element elItem = elOuterOl.begin( "li" );
        String qId = name + "." + questionNum;

        elItem.begin( "input" ).writeAtt( "type", "text" ).writeAtt( "name", qId + ".text" ).writeAtt( "value", question == null ? "" : question ).writeAtt( "id", qId + ".text" ).close();
//        elItem.begin( "button" ).writeAtt( "type", "button" ).writeAtt( "onclick", "addAnswer(this)" ).writeText( "Add" ).close();
        XmlWriter.Element elInnerOl = w.begin( "ol" );
        elInnerOl.writeAtt( "id", qId );
        int answerNum = 0;

        if( !CollectionUtils.isEmpty( answers ) ) {
            for( String s : answers ) {
                writeAnswer( elInnerOl, answerNum, s, qId );
                answerNum++;
            }
        } else {
            writeAnswer( elInnerOl, answerNum, "", qId );
        }
        elInnerOl.close();
        elItem.close();
    }

    private void writeAnswer( XmlWriter.Element elInnerOl, int answerNum, String text, String questionId ) {
        XmlWriter.Element elAnswerLi = elInnerOl.begin( "li" );
        String corName = questionId + ".correct";
        elAnswerLi.begin( "input" ).writeAtt( "type", "radio" ).writeAtt( "name", corName ).writeAtt( "value", answerNum + "" ).writeAtt( "id", corName + "." + answerNum ).close();
        String ansName = questionId + "." + answerNum + ".answer";
        elAnswerLi.begin( "input" ).writeAtt( "type", "text" ).writeAtt( "name", ansName ).writeAtt( "value", text ).writeAtt( "id", ansName ).close();
        elAnswerLi.close();
    }

    /**
     * Name patterns:
     * Question text = qa.1.text
     * Correct answer= qa.1.correct
     * Answer text   = qa.1.1.answer
     *
     * @param componentValue
     * @param rc
     * @param parameters
     * @param files
     */
    public void onPreProcess( ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        log.trace( "onPreProcess" );
        Map<Integer, Item> items = new HashMap<Integer, Item>();
        List<String> keysList = new ArrayList<String>( parameters.keySet() );
        Collections.sort( keysList );
        for( String key : keysList ) {
            String val = parameters.get( key );
            log.trace( "key: " + key + " - " + val );
            if( key.startsWith( name + "." ) ) {
                Item item = getItem( items, key );
                if( item != null ) {
                    if( key.endsWith( ".text" ) ) {
                        item.setQuestion( val );
                    } else if( key.endsWith( ".correct" ) ) {
                        int num = getInt( val );
                        item.setCorrectNum( num );
                    } else if( key.endsWith( ".answer" ) ) {
                        item.getAnswers().add( val );
                    }
                } else {
                    log.warn( "ignoring invalid entry: " + key );
                }
            }
        }
        ArrayList<Item> itemsList = new ArrayList<Item>();
        List<Integer> keys = new ArrayList<Integer>( items.keySet() );
        Collections.sort( keys );
        for( Integer key : keys ) {
            itemsList.add( items.get( key ) );
        }

        log.trace( "items size: " + itemsList.size() );
        componentValue.setValue( itemsList );
    }

    public ComponentValue createComponentValue( Templatable newRes ) {
        log.trace( "createComponentValue" );
        return new ComponentValue( name, newRes );
    }

    public Object parseValue( ComponentValue cv, Templatable ct, String s ) {
        log.trace( "parseValue" );
        return cv.getValue();
    }

    public String formatValue( Object oVal ) {
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
            elQ.setText( i.getQuestion() );
            elQ.setAttribute( "correct", i.getCorrectNum() + "" );
            for( String s : i.getAnswers() ) {
                Element elA = new Element( "answer" );
                elA.setText( s );
                elQ.addContent( elA );
            }
        }
        return new XmlUtils2().getXml( doc.getRootElement() );

    }

    public void changedValue( ComponentValue cv ) {
    }

    public Class getValueClass() {
        return String.class;
    }

    private Item getItem( Map<Integer, Item> items, String key ) {
        Integer num = getNum( key );
        if( num == null ) {
            log.trace( "getItem: key: " + key + " - num: " + num + " --- not found" );
            return null;
        } else {
            Item item = items.get( num );
            if( item == null ) {
                item = new Item();
                item.setAnswers( new ArrayList<String>() );
                items.put( num, item );
                log.trace( "getItem: key: " + key + " - num: " + num + " created new: " + item.getQuestion() );
            } else {
                log.trace( "getItem: key: " + key + " - num: " + num + " found existing: " + item.getQuestion() );
            }
            return item;
        }
    }

    /**
     * The question num is always after the first dot.
     *
     * Eg qa.1
     *
     * @param key
     * @return
     */
    private Integer getNum( String key ) {
        int pos = key.indexOf( "." );
        if( pos > 0 ) {
            int finish = key.indexOf( ".", pos + 1 );
            String s;
            if( finish > 0 ) {
                s = key.substring( pos + 1, finish );
            } else {
                s = key.substring( pos + 1 );
            }
            log.trace( "getNum: " + key + " -> " + s );
            return getInt( s );
        } else {
            return null;
        }
    }

    private int getInt( String s ) {
        return Integer.parseInt( s );
    }

    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;
        private String question;
        private int correctNum;
        private List<String> answers;

        public Item( String question, int correctNum, List<String> answers ) {
            this.question = question;
            this.correctNum = correctNum;
            this.answers = answers;
        }

        public Item() {
        }

        /**
         * @return the question
         */
        public String getQuestion() {
            return question;
        }

        /**
         * @param question the question to set
         */
        public void setQuestion( String question ) {
            this.question = question;
        }

        /**
         * @return the correctNum
         */
        public int getCorrectNum() {
            return correctNum;
        }

        /**
         * @param correctNum the correctNum to set
         */
        public void setCorrectNum( int correctNum ) {
            this.correctNum = correctNum;
        }

        /**
         * @return the answers
         */
        public List<String> getAnswers() {
            return answers;
        }

        /**
         * @param answers the answers to set
         */
        public void setAnswers( List<String> answers ) {
            this.answers = answers;
        }
    }

    public final void setValidationMessage( String s ) {
        RequestParams params = RequestParams.current();
        params.attributes.put( this.getName() + "_validation", s );
    }

    public final String getValidationMessage() {
        RequestParams params = RequestParams.current();
        return (String) params.attributes.get( this.getName() + "_validation" );
    }
}
