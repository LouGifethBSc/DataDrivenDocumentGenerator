package com.github.LouGifethBSc.dddg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class NameValuePairList {

	private static final Logger log = LogManager.getLogger();
	private ArrayList<NameValuePair> nvp_list;
	
	// constructor
	NameValuePairList() {
		nvp_list = new ArrayList<NameValuePair>();
	}
	
	// assume there is no need to delete objects tidily in destructor like you would in C
	// because the java garbage collector will take care of things
	
	// read parameters from a file with each line in the format name=value
	void read(String filename ) {
		log.trace("read({})",filename);
		try {
	        FileReader f = new FileReader(filename);
	        BufferedReader b = new BufferedReader(f);
	        String line;
	        
	        while((line = b.readLine())!=null)
	        {
	        if (line.length() >= 3) { // minimum sensible content is a=b
	        if (!line.startsWith("#")) { // skip over comments
	        		
	        	String[] substring_array = line.split("=");
	        	if (substring_array.length >= 1) {
	        		NameValuePair p = new NameValuePair();
	        		p.name=substring_array[0];
	        		if (substring_array.length >= 2) {
	        			p.value=substring_array[1];
	        		}
	        		else {
	        			p.value="";
	        		}
	        		nvp_list.add(p);
	        	}
	        }
	        }
	        }
	        b.close();
	        
		} catch (IOException e) {
			log.error("error reading parameters from file {}", filename);
			e.printStackTrace();
		}
	}
	
	void add(String name, String value) {
		NameValuePair nvp = new NameValuePair();
		nvp.name = name;
		nvp.value = value;
		nvp_list.add(nvp);
	}
	
	public String get(String name) {
		return get(name,"");
	}
	
	public String get(String name, String default_value) {
		String value=default_value;
        int i=1;
        while (i <= nvp_list.size()) {
            NameValuePair nvp = nvp_list.get(i-1);
            if (nvp.name.equals(name)) {
            	value = nvp.value;
            	break;
            }
            i++;
        }
        return value;
	}
}
