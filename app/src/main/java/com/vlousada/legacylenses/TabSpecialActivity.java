package com.vlousada.legacylenses;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.github.ma1co.pmcademo.app.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class TabSpecialActivity extends BaseActivity{
    private ListView listView;
    List<ParsedDataSet> XmlContentHandler;
    List<ParsedDataSet> data_list2 = null;
    Preferences prefs;
    private ListAdapterSpecial adapter;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_special);
        //load specials from profiles
        initControls();
        prefs = new Preferences(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ParsedDataSet myItem = data_list2.get(i);
                //Toast.makeText(TabSpecialActivity.this, myItem.getName(), Toast.LENGTH_SHORT).show();
                //update TextView in Main Activity
                MainActivity.m_tvLegacySpecial.setText(myItem.getName());
                // return Main Activity after Text updated
                onBackPressed();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset LegacySpecial text
                MainActivity.m_tvLegacySpecial.setText("");
                // return Main Activity
                onBackPressed();
            }
        });

    }

    private void initControls() {

        listView=(ListView) findViewById(R.id.lvSpecial);
        button=(Button) findViewById(R.id.bt_resetSpecial);
        new BackgroundTask().execute();

    }

    public class BackgroundTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            displayData();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                synchronized (this) {

                    saxParser();
                    //publishProgress(25);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }




    private void saxParser() {

        try {
            XmlContentHandler = new ArrayList<ParsedDataSet>();
            data_list2 = new ArrayList<ParsedDataSet>(); //lenses

            // initialize our input source variable
            InputSource inputSource = null;

            // XML from sdcard
            File extStore = Environment.getExternalStorageDirectory();
            File myFile = new File(extStore.getAbsolutePath() + "/LLEGACY/profiles.xml");
            // make sure profiles.xml is in your root SD card directory LLEGACY
            if(myFile.exists()) {
                File xmlFile = new File(Environment.getExternalStorageDirectory() + "/LLEGACY/profiles.xml");
                FileInputStream xmlFileInputStream = new FileInputStream(xmlFile);
                inputSource = new InputSource(xmlFileInputStream);
            }
            else {
                // XML from assets folder
                inputSource = new InputSource(getAssets().open("profiles.xml"));
            }


            // instantiate SAX parser
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();

            // get the XML reader
            XMLReader xmlReader = saxParser.getXMLReader();

            // prepare and set the XML content or data handler before
            // parsing
            XmlContentHandler xmlContentHandler = new XmlContentHandler();
            xmlReader.setContentHandler(xmlContentHandler);

            // parse the XML input source
            xmlReader.parse(inputSource);

            // put the parsed data to a List
            List<ParsedDataSet> parsedDataSet = xmlContentHandler.getParsedData();

            // we'll use an iterator so we can loop through the data
            Iterator<ParsedDataSet> i = parsedDataSet.iterator();
            ParsedDataSet dataItem;

            while (i.hasNext()) {

                dataItem = (ParsedDataSet) i.next();
                String parentTag = dataItem.getParentTag();

                if (parentTag.equalsIgnoreCase("SPECIALS")) {
                    data_list2.add(dataItem);
                }

            }

        } catch (Exception e) {
           Logger.error(e.getMessage());
        }

    }

    private void displayData(){


        adapter = new ListAdapterSpecial(this,data_list2);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

    }



    @Override
    public void onResume()
    {
        super.onResume();


    }

    @Override
    protected void onPause()
    {
        super.onPause();


    }


    protected boolean onEnterKeyDown()
    {
        onBackPressed();
        return true;
    }

    @Override
    protected boolean onPlayKeyDown()
    {
        //take screenshot?
        Bitmap bitmap = LLUtils.takeScreenshot(this.findViewById(android.R.id.content).getRootView());
        LLUtils.saveBitmap(bitmap,2);
        return true;
    }
}

