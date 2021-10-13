package xmltocsv;

import com.google.common.collect.LinkedHashMultimap;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
  import org.apache.commons.cli.CommandLine;
    import org.apache.commons.cli.Option;
    import org.apache.commons.cli.Options;
    import org.apache.commons.cli.Option.Builder;
    import org.apache.commons.cli.CommandLineParser;
    import org.apache.commons.cli.DefaultParser;
    import org.apache.commons.cli.ParseException;

public class App {

	// Identify argements 
		private static String xmlfile;
		// Arg2 csv file
		private static String csvfile;
		// Arg3 root element
		private static String rootelement;
		// Arg4 row element
		private static String rowelement;
		
    public static void main(String[] args) throws SAXException, FileNotFoundException, IOException {
        // First pass - to determine headers	
		
		
		Options options = new Options();

        Option input = new Option("input", "input", true, "input xml file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("output", "output", true, "output csv file");
        output.setRequired(false);
        options.addOption(output);
		
		Option root = new Option("root", "root", true, "xml root path");
        output.setRequired(true);
        options.addOption(root);
		
		Option row = new Option("row", "row", true, "xml root path");
        output.setRequired(true);
        options.addOption(row);

        CommandLineParser parser = new DefaultParser();
    //    HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose 

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
         //   formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
		String rootelement = cmd.getOptionValue("root");
		String rowelement = cmd.getOptionValue("row");

//        System.out.println(inputFilePath);
//        System.out.println(outputFilePath);
//		System.out.println(rootelement);
//		System.out.println(rowelement);
		
	
		
		
		// Arg1 xml file
		setxmlfile(inputFilePath);
		// Arg2 csv file
		//	setcsvfile(args[2]);
		// Arg3 root element
		setrootelement(rootelement);
		// Arg4 row element
		setrowelement(rowelement);
		
        XMLReader xr = XMLReaderFactory.createXMLReader();
        HeaderHandler handler = new HeaderHandler();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        FileReader r = new FileReader(getxmlfile());
        xr.parse(new InputSource(r));

        LinkedHashMap<String, Integer> headers = handler.getHeaders();
        int totalnumberofcolumns = 0;
        for (int headercount : headers.values()) {
            totalnumberofcolumns += headercount;
        }
        String[] columnheaders = new String[totalnumberofcolumns];
        int i = 0;
        for (Entry<String, Integer> entry : headers.entrySet()) {
            for (int j = 0; j < entry.getValue(); j++) {
                columnheaders[i] = entry.getKey();
                i++;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String h : columnheaders) {
            sb.append(h);
            sb.append(',');
        }
        System.out.println(sb.substring(0, sb.length() - 1));

        // Second pass - collect and output data

        xr = XMLReaderFactory.createXMLReader();

        DataHandler datahandler = new DataHandler();
        datahandler.setHeaderArray(columnheaders);

        xr.setContentHandler(datahandler);
        xr.setErrorHandler(datahandler);
        r = new FileReader(getxmlfile());
        xr.parse(new InputSource(r));
    }


	public static String getxmlfile() {
        
			return xmlfile;
    }
	
	public static void setxmlfile(String inputxmlfile) {
        
			xmlfile = inputxmlfile;
    }

	public static void setcsvfile(String inputcsvfile) {
        
			csvfile = inputcsvfile;
    }
	
	public static String getcsvfile() {
        
			return csvfile;
    }
	
	public static void setrootelement(String inputrootelement) {
        
			rootelement = inputrootelement;
    }
	
	public static String getrootelement() {
        
			return rootelement;
    }
	
	public static void setrowelement(String inputrowelement) {
        
			rowelement = inputrowelement;
    }

	
	public static String getrowelement() {
        
			return rowelement;
    }
	
    public static class HeaderHandler extends DefaultHandler {

        private String content;
        private String currentElement;
        private boolean insideElement = false;
        private Attributes attribs;
        private LinkedHashMap<String, Integer> itemHeader;
        private LinkedHashMap<String, Integer> accumulativeHeader = new LinkedHashMap<String, Integer>();

        public HeaderHandler() {
            super();
        }

        private LinkedHashMap<String, Integer> getHeaders() {
            return accumulativeHeader;
        }

        private void addItemHeader(String headerName) {
            if (itemHeader.containsKey(headerName)) {
                itemHeader.put(headerName, itemHeader.get(headerName) + 1);
            } else {
                itemHeader.put(headerName, 1);
            }
        }

        @Override
        public void startElement(String uri, String name,
                String qName, Attributes atts) {
            if (getrowelement().equalsIgnoreCase(qName)) {
                itemHeader = new LinkedHashMap<String, Integer>();
            }
            currentElement = qName;
            content = null;
            insideElement = true;
            attribs = atts;
        }

        @Override
        public void endElement(String uri, String name, String qName) {
            if (!getrowelement().equalsIgnoreCase(qName) && !getrootelement().equalsIgnoreCase(qName)) {
                if (content != null && qName.equals(currentElement) && content.trim().length() > 0) {
                    addItemHeader(qName);
                }
                if (attribs != null) {
                    int attsLength = attribs.getLength();
                    if (attsLength > 0) {
                        for (int i = 0; i < attsLength; i++) {
                            String attName = attribs.getLocalName(i);
                            addItemHeader(attName);
                        }
                    }
                }
            }
            if (getrowelement().equalsIgnoreCase(qName)) {
                for (Entry<String, Integer> entry : itemHeader.entrySet()) {
                    String headerName = entry.getKey();
                    Integer count = entry.getValue();
                    //System.out.println(entry.getKey() + ":" + entry.getValue());
                    if (accumulativeHeader.containsKey(headerName)) {
                        if (count > accumulativeHeader.get(headerName)) {
                            accumulativeHeader.put(headerName, count);
                        }
                    } else {
                        accumulativeHeader.put(headerName, count);
                    }
                }
            }
            insideElement = false;
            currentElement = null;
            attribs = null;
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (insideElement) {
                content = new String(ch, start, length);
            }
        }
    }

    public static class DataHandler extends DefaultHandler {

        private String content;
        private String currentElement;
        private boolean insideElement = false;
        private Attributes attribs;
        private LinkedHashMultimap dataMap;
        private String[] headerArray;

        public DataHandler() {
            super();
        }

        @Override
        public void startElement(String uri, String name,
                String qName, Attributes atts) {
            if (getrowelement().equalsIgnoreCase(qName)) {
                dataMap = LinkedHashMultimap.create();
            }
            currentElement = qName;
            content = null;
            insideElement = true;
            attribs = atts;
        }

        @Override
        public void endElement(String uri, String name, String qName) {
            if (!getrowelement().equalsIgnoreCase(qName) && !getrootelement().equalsIgnoreCase(qName)) {
                if (content != null && qName.equals(currentElement) && content.trim().length() > 0) {
                    dataMap.put(qName, content);
                }
                if (attribs != null) {
                    int attsLength = attribs.getLength();
                    if (attsLength > 0) {
                        for (int i = 0; i < attsLength; i++) {
                            String attName = attribs.getLocalName(i);
                            dataMap.put(attName, attribs.getValue(i));
                        }
                    }
                }
            }
            if (getrowelement().equalsIgnoreCase(qName)) {
                String data[] = new String[headerArray.length];
                int i = 0;
                for (String h : headerArray) {
                    if (dataMap.containsKey(h)) {
                        Object[] values = dataMap.get(h).toArray();
                        data[i] = (String) values[0];
                        if (values.length > 1) {
                            dataMap.removeAll(h);
                            for (int j = 1; j < values.length; j++) {
                                dataMap.put(h, values[j]);
                            }
                        } else {
                            dataMap.removeAll(h);
                        }
                    } else {
                        data[i] = "";
                    }
                    i++;
                }
                StringBuilder sb = new StringBuilder();
                for (String d : data) {
                    sb.append(d);
                    sb.append(',');
                }
                System.out.println(sb.substring(0, sb.length() - 1));
            }
            insideElement = false;
            currentElement = null;
            attribs = null;
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (insideElement) {
                content = new String(ch, start, length);
            }
        }

        public void setHeaderArray(String[] headerArray) {
            this.headerArray = headerArray;
        }
    }
}