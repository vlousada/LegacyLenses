package com.vlousada.legacylenses;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XmlContentHandler extends DefaultHandler {

    private static final String LOG_TAG = "XmlContentHandler";

    // used to track of what tags are we
    private boolean inLenses = false;
    private boolean inSpecials = false;

    // accumulate the values
    private StringBuilder mStringBuilder = new StringBuilder();

    // new object
    private ParsedDataSet mParsedDataSet = new ParsedDataSet();

    // the list of data
    private List<ParsedDataSet> mParsedDataSetList = new ArrayList<ParsedDataSet>();

    /*
     * Called when parsed data is requested.
     */
    public List<ParsedDataSet> getParsedData() {
        return this.mParsedDataSetList;
    }


    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {

        if (localName.equalsIgnoreCase("LENS")) {
            // meaning new data object will be made
            this.mParsedDataSet = new ParsedDataSet();
            this.inLenses = true;
        }

        else if (localName.equalsIgnoreCase("SPECIAL")) {
            this.mParsedDataSet = new ParsedDataSet();
            this.inSpecials = true;
        }

    }

    /*
     * @Receive notification of the end of an element.
     *
     * @Called in end tags such as </Owner>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {

        // Lenses
        if (this.inLenses == true && localName.equals("Lens")) {
            this.mParsedDataSetList.add(mParsedDataSet);
            mParsedDataSet.setParentTag("LENSES");
            this.inLenses = false;
        }

        else if (this.inLenses == true && localName.equalsIgnoreCase("name")) {
            mParsedDataSet.setName(mStringBuilder.toString().trim());
        }

        else if (this.inLenses == true && localName.equalsIgnoreCase("mount")) {
            mParsedDataSet.setMount(mStringBuilder.toString().trim());
        }

        else if (this.inLenses == true && localName.equalsIgnoreCase("focal")) {
            mParsedDataSet.setFocal(mStringBuilder.toString().trim());
        }

        else if (this.inLenses == true && localName.equalsIgnoreCase("apertures")) {
            mParsedDataSet.setApertures(mStringBuilder.toString().trim());
        }

        // Specials
        if (this.inSpecials == true && localName.equals("Special")) {
            this.mParsedDataSetList.add(mParsedDataSet);
            mParsedDataSet.setParentTag("SPECIALS");
            this.inSpecials = false;
        }

        else if (this.inSpecials == true && localName.equalsIgnoreCase("name")) {
            mParsedDataSet.setName(mStringBuilder.toString().trim());
        }

        else if (this.inSpecials == true && localName.equalsIgnoreCase("math")) {
            mParsedDataSet.setMath(mStringBuilder.toString().trim());
        }

        else if (this.inSpecials == true && localName.equalsIgnoreCase("description")) {
            mParsedDataSet.setDescription(mStringBuilder.toString().trim());
        }

        // empty our string builder
        mStringBuilder.setLength(0);
    }

    /*
     * @Receive notification of character data inside an element.
     *
     * @Gets be called on the following structure: <tag>characters</tag>
     */
    @Override
    public void characters(char ch[], int start, int length) {
        // append the value to our string builder
        mStringBuilder.append(ch, start, length);
    }
}