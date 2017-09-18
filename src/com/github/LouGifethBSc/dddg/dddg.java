package com.github.LouGifethBSc.dddg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class dddg { // data driven document generator
	
	// this program generates a set of documents (cards)
	// using data from an input csv file
	// and an xml layout template
	//
	// there is an extension to send each card in an email, commented out below

	private static String CONFIG_FILE_DEFAULT = "config.txt";
	private static String INPUT_CSV_DEFAULT = "input.csv";
	private static String MAX_ROWS_DEFAULT = "300";
	private static String CARD_LAYOUT_FILE_DEFAULT = "card_layout.xml";
	private static String CARD_NAME_PREFIX_DEFAULT = "card_";
	
	private static Logger log;
	
	private static NameValuePairList config;
	// private static EmailSender emailSender;
	
	public static void main(String[] args) {

		Properties props = System.getProperties();
		props.setProperty("log4j.configurationFile", "log4j2.xml"); // should find this automatically but it doesn't ...
		log = LogManager.getLogger();

		String config_file = CONFIG_FILE_DEFAULT;
		if (args.length>=1) {
			config_file = args[0];
		}
		config = new NameValuePairList();
		config.read(config_file);
		
		// emailSender = new EmailSender(config);
    	String send_email_string = config.get("send_email", "false");
    	boolean send_email = false;
    	if (send_email_string.equals("true")) {
    		send_email = true;
    	}
		String input_csv_file = config.get("input_csv",INPUT_CSV_DEFAULT);
		processFile(input_csv_file, send_email);
	}
	
	public static void processFile(String csv_filename, boolean send_email) {
		
		log.trace("processFile({})",csv_filename);
		int rows_processed = 0;
		int cards_created = 0;
		int cards_emailed = 0;
		
		String id_column_name = config.get("id_column_name", "id");
		// String email_column_name = config.get("email_column_name", "email");
		String card_layout_file = config.get("card_layout", CARD_LAYOUT_FILE_DEFAULT);
		String card_name_prefix = config.get("card_name_prefix", CARD_NAME_PREFIX_DEFAULT);
		// String email_subject = config.get("email_subject", "Your new membership card");
		// String email_message_text = config.get("email_message_text", "email_message_text.txt");

		PDFCreator pdfCreator = new PDFCreator();
		
		try {
	        FileReader f = new FileReader(csv_filename);
	        BufferedReader b = new BufferedReader(f);
	        String line;
	        String[] column_names = null;
	        
	        // read column names from header row
	        line = b.readLine();
	        if (line != null) {
	        	log.trace("header: {}",line);
		        column_names = line.split(",");
	        }
	        else {
	        	log.error("cannot read header row: {}",csv_filename);
        		b.close();
        		throw new IOException();
	        }
        	int id_index = findIndex(id_column_name, column_names);
        	if (id_index == -1) {
        		log.error("cannot read id, no column named {}", id_column_name);
        		b.close();
        		throw new IOException();
        	}
        	// int email_index = findIndex(email_column_name, column_names);
        	// if (email_index == -1) {
        	// 	log.error("cannot read email, no column named {}", email_column_name);
        	// 	b.close();
        	// 	throw new IOException();
        	// }
	        
	        String max_rows_string = config.get("max_rows", MAX_ROWS_DEFAULT);
	        int max_rows = Integer.valueOf(max_rows_string);
	        
	        while((line = b.readLine())!=null)
	        {
	        	String[] substring_array = line.split(",");
		        String id = substring_array[id_index];
		        // String email = substring_array[email_index];
		        NameValuePairList subs = new NameValuePairList();
		        
		        for (int column = 1; column <= column_names.length  && column <= substring_array.length; column++) {
		        	  log.trace("{}={}", column_names[column-1], substring_array[column-1]);
		        	  subs.add(column_names[column-1], substring_array[column-1]);
		        }

	        	String card_name = card_name_prefix + id + ".pdf";
	        	pdfCreator.create(card_name, card_layout_file, subs);
	        	cards_created++;
	        		
	        	if (send_email) {
	        		// if (emailSender.send(email, email_subject, email_message_text, card_name)) {
	        		// 	cards_emailed++;
	        		// }
	        	}
	        	rows_processed++;
	        	if (rows_processed >= max_rows) {
	        		log.warn("maximum number of rows processed ({}), set max_rows=n for more", max_rows);
	        		break;
	        	}
	        }
	        b.close();
	        log.info("finished. rows processed: {}, cards created: {}, cards emailed: {}",
	        		rows_processed, cards_created, cards_emailed);
	        
		} catch (IOException e) {
			log.error("error processing file {}", csv_filename);
			e.printStackTrace();
		}
	}
	
	public static int findIndex(String string_to_find, String[] string_array) {
		int index = -1; // default if not found
        int i=0;
        while (i < string_array.length) {
            if (string_to_find.equals(string_array[i])) {
            	index = i;
            	break;
            }
            i++;
        }		
		return index;
	}

}
