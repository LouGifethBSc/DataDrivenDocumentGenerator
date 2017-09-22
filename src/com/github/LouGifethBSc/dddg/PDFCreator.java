package com.github.LouGifethBSc.dddg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class PDFCreator { // TODO: should be a singleton class (only one instance needed)

	private NameValuePairList config;
	private PDFFontHelper fontHelper;
	
	PDFCreator(NameValuePairList config_param) { // constructor
		config = config_param;
		fontHelper = new PDFFontHelper(config);
	}
	
	// all dimensions are in points (as in font sizes)
	// 72 points = 1 inch
	
	static final String DEFAULT_CARD_SIZE = "243,153"; // credit card size = 3.375x2.125 inches
	static final String DEFAULT_CARD_MARGIN = "9"; // one eighth of an inch
	static final String DEFAULT_IMAGE_FILENAME = "image.jpg";
	static final String DEFAULT_IMAGE_POSITION = "0,0";
	static final String DEFAULT_IMAGE_SIZE = "100,100";
	static final String DEFAULT_RECTANGLE_POSITION = "0,0";
	static final String DEFAULT_RECTANGLE_SIZE = "100,100";
	static final String DEFAULT_TEXT_POSITION = "0,0";
	static final PDFont DEFAULT_FONT = PDType1Font.TIMES_ROMAN;
	static final String DEFAULT_FONT_SIZE = "10";

	private static final Logger log = LogManager.getLogger();

	// PDF document stuff ...
	private PDDocument pdf_document;
	private PDPage pdf_page;
	private PDPageContentStream pdf_content;

	// XML document stuff ...
    private DocumentBuilderFactory xml_dbFactory;
    private DocumentBuilder xml_dBuilder;
    private Document xml_document;
    
    private NameValuePairList text_substitutions;
    private int card_margin = Integer.parseInt(DEFAULT_CARD_MARGIN);
    private PDFont global_font = DEFAULT_FONT;
    private int global_font_size = Integer.parseInt(DEFAULT_FONT_SIZE);

	public void create (String cardname, String card_layout_file, NameValuePairList subs) {
		log.trace("create({})",cardname);
        text_substitutions = subs;
        
    	try {	
    		pdf_document = new PDDocument();
    		pdf_page = new PDPage();
    		pdf_document.addPage(pdf_page);
    		pdf_content = new PDPageContentStream(pdf_document, pdf_page, AppendMode.APPEND, true, true);

    		// the pdf document is now a single blank a4 page
    		// add remaining elements as defined in xml layout file
    		
	        File xmlLayoutFile = new File(card_layout_file);
	        xml_dbFactory = DocumentBuilderFactory.newInstance();
	        xml_dBuilder = xml_dbFactory.newDocumentBuilder();
	        xml_document = xml_dBuilder.parse(xmlLayoutFile);
	        xml_document.getDocumentElement().normalize();
    		
	        NodeList nodeList = xml_document.getChildNodes();
	          
	        if (nodeList.getLength() ==1) { // only one top-level node expected
	        	Node node = nodeList.item(0);
	        	NodeList childNodes = node.getChildNodes();
		        for (int temp = 0; temp < childNodes.getLength(); temp++) {
		            Node childNode = childNodes.item(temp);
		            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
		                Element element = (Element) childNode;
		                String elementName = element.getNodeName();
		                // log.trace("element:{}", elementName);
		                
		                if (elementName.equals("card_size")) {
		            	    set_card_size(element);
		                }
		                else
			            if (elementName.equals("rectangle")) {
			            	add_rectangle(element);
			            }
			            else
		                if (elementName.equals("font")) {
		            	    set_font(element);
		                }
		                else
			            if (elementName.equals("image")) {
			            	add_image(element);
			            }
			            else
		                if (elementName.equals("text")) {
		            	    add_text(element);
		                }
		                else {
		                	log.error("unrecognised element ({}) in card layout file {} ",
		                			elementName, card_layout_file);
		                }
		            }
		        }
	        }
	        else {
	        	log.error("unexpected number of nodes ({}) in card layout file {}",
	        			nodeList.getLength(), card_layout_file);
	        }
    		
    		pdf_content.close();
    		pdf_document.save(cardname);
    		pdf_document.close();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

	void set_card_size(Element element) {
	    try {
            String size_string = get_subelement_value(element, "size");
            if (size_string.equals("")) {
         	   size_string = DEFAULT_CARD_SIZE;
          	   log.error("card size missing, using default [{}]", size_string);
            }
            String margin_string = get_subelement_value(element, "margin");
            if (margin_string.equals("")) {
         	   margin_string = DEFAULT_CARD_MARGIN;
          	   log.warn("card margin missing, using default [{}]", margin_string);
            }
            String[] size_xy = size_string.split(",");
            int width = Integer.parseInt(size_xy[0]);
            int height = Integer.parseInt(size_xy[1]);
	    	card_margin = Integer.parseInt(margin_string);
	    	
    		PDRectangle pageSize = new PDRectangle
    				(card_margin+width+card_margin,card_margin+height+card_margin);
    		pdf_page.setMediaBox(pageSize);
    		
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	void add_rectangle(Element element) {
	    try {
            String position_string = get_subelement_value(element, "position");
            if (position_string.equals("")) {
         	   position_string = DEFAULT_RECTANGLE_POSITION;
          	   log.error("rectangle position missing, using default [{}]", position_string);
            }
            String size_string = get_subelement_value(element, "size");
            if (size_string.equals("")) {
         	   size_string = DEFAULT_RECTANGLE_SIZE;
          	   log.error("rectangle size missing, using default [{}]", size_string);
            }
            String[] position_xy = position_string.split(",");
            int x = Integer.parseInt(position_xy[0]);
            int y = Integer.parseInt(position_xy[1]);
            String[] size_xy = size_string.split(",");
            int width = Integer.parseInt(size_xy[0]);
            int height = Integer.parseInt(size_xy[1]);
	    	
    		pdf_content.addRect(card_margin+x, card_margin+y, width, height);
    		pdf_content.stroke();
    		
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	void set_font(Element element) {
		
		// set global font and font size to be used for all future text elements
		// (can be over-ridden for individual text elements)

		PDFont new_font = get_font(element);
		if (new_font == null) {
			// leave global_font unchanged if <font> tag is missing
		}
		else {
			global_font = new_font;
		}
        
        String size_string = get_subelement_value(element, "size");
        if (size_string.equals("")) {
        	// leave global_font_size unchanged if <size> tag is missing
        }
        else {
            global_font_size = Integer.parseInt(size_string);
        }

	}

	void add_image(Element element) {
	    try {	
               String image_filename = get_subelement_value(element, "file");
               if (image_filename.equals("")) {
            	   image_filename = DEFAULT_IMAGE_FILENAME;
             	   log.error("image filename missing, using default [{}]", image_filename);
               }
               // log.trace("image_filename={}", image_filename);
               String position_string = get_subelement_value(element, "position");
               if (position_string.equals("")) {
            	   position_string = DEFAULT_IMAGE_POSITION;
             	   log.error("image position missing, using default [{}]", position_string);
               }
               String size_string = get_subelement_value(element, "size");
               if (size_string.equals("")) {
            	   size_string = DEFAULT_IMAGE_SIZE;
             	   log.error("image size missing, using default [{}]", size_string);
               }
               String[] position_xy = position_string.split(",");
               int x = Integer.parseInt(position_xy[0]);
               int y = Integer.parseInt(position_xy[1]);
               String[] size_xy = size_string.split(",");
               int width = Integer.parseInt(size_xy[0]);
               int height = Integer.parseInt(size_xy[1]);
               
       		   PDImageXObject pdImage = PDImageXObject.createFromFile(image_filename, pdf_document);
     		   pdf_content.drawImage(pdImage, card_margin+x, card_margin+y, width, height);	
          	
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	void add_text(Element element) {
	    try {
            String position_string = get_subelement_value(element, "position");
            if (position_string.equals("")) {
         	   position_string = DEFAULT_TEXT_POSITION;
          	   log.error("text position missing, using default [{}]", position_string);
            }
            String[] position_xy = position_string.split(",");
            int x = Integer.parseInt(position_xy[0]);
            int y = Integer.parseInt(position_xy[1]);

            PDFont text_font;
            PDFont new_font = get_font(element);
            if (new_font == null) {
            	// font not specified - use global font
            	text_font = global_font;
            }
            else {
            	// override global font for this text element
            	text_font = new_font;
            }
            String size_string = get_subelement_value(element, "size");
            int text_font_size;
            if (size_string.equals("")) {
            	// font size not specified - use global font size
         	    text_font_size = global_font_size;
            }
            else {
            	// override global font size for this text element
                text_font_size = Integer.parseInt(size_string);
            }
            
            String text_string = get_text(element);            
            
            // deal with text that would otherwise not fit properly
            // e.g. people with very long names 
            String max_length_string = get_subelement_value(element, "max_length");
            if (max_length_string.equals("")) {
            	// max length not specified - leave text as it is
            	// it may overflow the edge of the card
            }
            else {
                int max_length = Integer.parseInt(max_length_string);
                if (text_string.length() > max_length) {
                    String overflow_position_string = get_subelement_value(element, "overflow");
                    if (overflow_position_string.equals("")) {
                   	   log.warn("overflow position missing, text will be truncated: {}", text_string);
                   	   text_string = text_string.substring(0,max_length);
                    }
                    else {
                	   // split the text into two parts (hopefully this will be enough!)
                    	int split_position = max_length; // default split position
                        // see if the text can be split  more logically at a space
                    	for (int test_position=max_length-1; test_position>=0; test_position--) {
                    		if (text_string.charAt(test_position) == ' ') {
                    			split_position = test_position+1; // split after the space
                    			break;
                    		}
                    	}
                    	String text_string_overflow = text_string.substring(split_position);
                        text_string = text_string.substring(0,split_position);
                        String[] overflow_position_xy = overflow_position_string.split(",");
                        int overflow_x = Integer.parseInt(overflow_position_xy[0]);
                        int overflow_y = Integer.parseInt(overflow_position_xy[1]);
                        
                        // the overflow text may still itself overflow but that's just too bad ...
                        add_text_to_pdf(overflow_x, overflow_y, text_string_overflow, text_font, text_font_size);
                    }
                }
            }
            
	    	add_text_to_pdf(x, y, text_string, text_font, text_font_size);
	    	
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	void add_text_to_pdf(int x, int y, String text, PDFont font, int font_size) {
		try {
    		pdf_content.setFont(font, font_size);
    		pdf_content.beginText();
  		    pdf_content.newLineAtOffset(card_margin+x, card_margin+y);
  		    pdf_content.showText(text);
  		    pdf_content.endText();			
		} catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	String get_subelement_value(Element e, String subelementName) {
		String value=""; // default value if no subelement with that name
        NodeList subelementList = e.getElementsByTagName(subelementName);
        if (subelementList.getLength() >=1) {
     	  value = subelementList.item(0).getTextContent();
     	  // ignore any further subelements with the same name
        }
		return value;
	}
	
	PDFont get_font(Element e) {

		String font_string = get_subelement_value(e, "font");
		String style_string = get_subelement_value(e, "style");
		
		return fontHelper.get_font(pdf_document, font_string, style_string);
		
	}
	
	String get_text(Element e) {
		// concatenate the text sub-elements
		// each sub-element can either be literal text <text>xxx</text>
		// or a value to be looked up in the csv file <value>column_name</value>
		String text="";
        NodeList subelementList = e.getChildNodes();
        for (int temp = 0; temp < subelementList.getLength(); temp++) {
           Node childNode = subelementList.item(temp);
           if (childNode.getNodeType() == Node.ELEMENT_NODE) {
               Element childElement = (Element) childNode;
               String childElementName = childElement.getNodeName();
               
               if (childElementName.equals("text")) {
               	  text = text + childElement.getTextContent();
               }
               else
               if (childElementName.equals("value")) {
             	  String lookup = childElement.getTextContent();
             	  String default_value = "<" + lookup + ">";
             	  String value_string = text_substitutions.get(lookup, default_value);
	              text = text + value_string;
              }
              else {
            	  // other sub-elements such as <position> and <font_size> are handled elsewhere
              }
           }
        }
		return text;
	}

}
