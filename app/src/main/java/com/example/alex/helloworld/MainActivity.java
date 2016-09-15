package com.example.alex.helloworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

        String temperature;
        Bitmap icon = null;
        TextView tempText;

        ProgressDialog dialog;

        private XPath xPath = XPathFactory.newInstance().newXPath();

        private String apiKey = "0c6f87664b51f1fd";

        private String [] sitesAll = { "Kmdsilve4", "Kmieastl10", "Kmdgaith12", "Kmdgerma14", "Kmdfrede39",
                "Kmdhager17", "Kmdmonro5", "Kmdedgew1", "Kmdspark2", "Kmdtimon1", "Kmdparkv5",
                "Kmdburto4", "Kmdelkri1", "Kmdodent3", "Kmdessex6", "Kmdhanov3", "Kmdmiddl4",
                "Kmdellic49", "Kmdupper16", "Kmdwaldo6", "Kmdgreen3", "Kmdarnol6", "Kmdglenb3",
                "Kmdglenb12", "Kmdodent3", "Kdebetha13", "Kdehenlo2", "Kmdannap43" };

        private String [] sitesSubset = { "Kmdsilve4", "Kmieastl10" };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            tempText = (TextView) findViewById(R.id.tempText);

            new retrieve_weatherTask().execute();

        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
            return true;
        }

        private String QuerySite(String siteQuery){
            String qResult = "";
            try {
                URL myURL = new URL(siteQuery);
                URLConnection myURLConnection = myURL.openConnection();
                myURLConnection.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        myURLConnection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String stringReadLine = null;
                while ((stringReadLine = in.readLine()) != null) {
                    stringBuilder.append(stringReadLine + "\n");
                }
                qResult = stringBuilder.toString();
                in.close();
            }
            catch (MalformedURLException e) {
                // new URL() failed
                // ...
            }
            catch (IOException e) {
                // openConnection() failed
                // ...
            }

            return qResult;
        }

        protected class retrieve_weatherTask extends AsyncTask<Void, String, List<Site>> {

            protected void onPreExecute(){
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Loading…");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected List<Site> doInBackground(Void ...arg0) {
                // Query the weather underground for site information
                List<Site> sites = new ArrayList<Site>();
                for (String site : sitesSubset) {
                    String qResult = QuerySite(MessageFormat.format(
                            "http://api.wunderground.com/api/{0}/conditions/q/pws:{1}.xml", apiKey, site));

                    // Create document to use XPath on
                    Document dest = null;
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                            .newInstance();
                    DocumentBuilder parser;
                    try {
                        parser = dbFactory.newDocumentBuilder();
                        dest = parser
                                .parse(new ByteArrayInputStream(qResult.getBytes()));
                    } catch (ParserConfigurationException e1) {
                        e1.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String precipitation = sv ("//current_observation/precip_today_string", dest);
                    sites.add(new Site(site, precipitation));

                    Log.d("PRECIPITATION", MessageFormat.format("{0}: {1}", site, precipitation));
                }

                return sites;
            }

            public String sv(String query, Node node) {

                String rs = "";

                try {
                    Node n = (Node) xPath.evaluate(query, node, XPathConstants.NODE);
                    if (n != null) {
                        rs = n.getTextContent();
                    }
                } catch (Exception e) {
                    rs = "";
                }
                return rs;
            }

            protected void onPostExecute(List<Site> result) {
                if(dialog.isShowing()){
                    dialog.dismiss();
                    String displayText = "";
                    for (Site site : result) {
                        displayText += MessageFormat.format("{0}: {1}\n", site.Name, site.Precipitation);
                    }
                    tempText.setText(displayText);
                }
            }
        }
}
