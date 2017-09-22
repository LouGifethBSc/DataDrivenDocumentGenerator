package com.github.LouGifethBSc.dddg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PDFFontHelper {

	private NameValuePairList config;

	static final String DEFAULT_FONT_DIR = "C:/Windows/Fonts";

	PDFFontHelper(NameValuePairList config_param) { // constructor
		config = config_param;
	}
	private static final Logger log = LogManager.getLogger();
	
	// some constants to handle standard fonts and styles ...
	
	final int VAL_NULL = 0;
	final int VAL_TIMES = 1;
	final int VAL_HELVETICA = 2;
	final int VAL_COURIER = 3;
	final int VAL_ITALIC = 10;
	final int VAL_BOLD = 100;
	
	final int VAL_TIMES_NORMAL = VAL_TIMES;
	final int VAL_TIMES_ITALIC = VAL_TIMES + VAL_ITALIC;
	final int VAL_TIMES_BOLD = VAL_TIMES + VAL_BOLD;
	final int VAL_TIMES_BOLD_ITALIC = VAL_TIMES + VAL_BOLD + VAL_ITALIC;
	
	final int VAL_HELVETICA_NORMAL = VAL_HELVETICA;
	final int VAL_HELVETICA_ITALIC = VAL_HELVETICA + VAL_ITALIC;
	final int VAL_HELVETICA_BOLD = VAL_HELVETICA + VAL_BOLD;
	final int VAL_HELVETICA_BOLD_ITALIC = VAL_HELVETICA + VAL_BOLD + VAL_ITALIC;
	
	final int VAL_COURIER_NORMAL = VAL_COURIER;
	final int VAL_COURIER_ITALIC = VAL_COURIER + VAL_ITALIC;
	final int VAL_COURIER_BOLD = VAL_COURIER + VAL_BOLD;
	final int VAL_COURIER_BOLD_ITALIC = VAL_COURIER + VAL_BOLD + VAL_ITALIC;
	
	PDFont get_font(PDDocument document, String font_string, String style_string) {
		PDFont font = null;
		
        if (font_string.equals("")) {
        	// font not specified - return null
        }
        else
        if (font_string.equals("Times") || font_string.equals("Helvetica") || font_string.equals("Courier")) {
        	// these are the standard fonts built into the pdf specification
        	font = get_standard_font(font_string, style_string);
        }
        else {
        	// otherwise assume its a truetype font located on the host computer
            font = get_ttf_font(document, font_string);
        }
       
        return font;
	}
	
	PDFont get_standard_font(String font_string, String style_string) {
		PDFont font = null;
		
		int val_font = VAL_NULL;
		
        if (font_string.equals("Times")) {
        	val_font += VAL_TIMES;
        }
        else
        if (font_string.equals("Helvetica")) {
            val_font += VAL_HELVETICA;
        }
        else
        if (font_string.equals("Courier")) {
            val_font += VAL_COURIER;
        }
        else
        if (font_string.equals("")) {
            // leave val_font equal to null if font not specified
        }
        else {
            log.error("unrecognised font: {}", font_string);
        }
        
        if (style_string.equals("Bold")) {
        	val_font += VAL_BOLD;
        }
        else
        if (style_string.equals("Italic")) {
            val_font += VAL_ITALIC;
        }
        else
        if (style_string.equals("BoldItalic")) {
            val_font += VAL_BOLD;
            val_font += VAL_ITALIC;
        }
        else
        if (style_string.equals("")) {
            // leave val_font equal to the standard font if style not specified
        }
        else {
            log.error("unrecognised style: {}", style_string);
        }
              
        switch(val_font) {
        case VAL_TIMES_NORMAL:
        	font = PDType1Font.TIMES_ROMAN;
        	break;
        case VAL_TIMES_BOLD:
        	font = PDType1Font.TIMES_BOLD;
        	break;
        case VAL_TIMES_ITALIC:
        	font = PDType1Font.TIMES_ITALIC;
        	break;
        case VAL_TIMES_BOLD_ITALIC:
        	font = PDType1Font.TIMES_BOLD_ITALIC;
        	break;
        case VAL_HELVETICA_NORMAL:
        	font = PDType1Font.HELVETICA;
        	break;
        case VAL_HELVETICA_BOLD:
        	font = PDType1Font.HELVETICA_BOLD;
        	break;
        case VAL_HELVETICA_ITALIC:
        	font = PDType1Font.HELVETICA_OBLIQUE;
        	break;
        case VAL_HELVETICA_BOLD_ITALIC:
        	font = PDType1Font.HELVETICA_BOLD_OBLIQUE;
        	break;
        case VAL_COURIER_NORMAL:
        	font = PDType1Font.COURIER;
        	break;
        case VAL_COURIER_BOLD:
        	font = PDType1Font.COURIER_BOLD;
        	break;
        case VAL_COURIER_ITALIC:
        	font = PDType1Font.COURIER_OBLIQUE;
        	break;
        case VAL_COURIER_BOLD_ITALIC:
        	font = PDType1Font.COURIER_BOLD_OBLIQUE;
        	break;
        // if it doesn't match any of the above then leave font equal to null
        }
        
		return font;
	}

	PDFont get_ttf_font(PDDocument document, String font_string) {
		PDFont font = null;
		String font_file = "";
		
		try {
			String font_dir = config.get("font_dir",DEFAULT_FONT_DIR);
			String font_ext = ".ttf";
			font_file = font_dir + "/" + font_string;
			if (!font_file.contains(".")) {
				font_file = font_file + font_ext;
			}
			font = PDType0Font.load(document, new File(font_file));
		}
		catch (FileNotFoundException e) {
			log.error("file not found: {}", font_file);
			// continue and return null
		}
	    catch (IOException e) {
	    	e.printStackTrace();
	    }
        
		return font;
	}
	
}
