package org.mdp.g10;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ArxivStream implements Runnable {
	public final SimpleDateFormat ARXIV_DATE = new SimpleDateFormat("yyyy-MM-dd");
	// Frequency of the data stream
	final long DELAY = 3;
	
	BufferedReader br;
	Producer<String, String> producer;
	String topic;
	
	
	public ArxivStream(BufferedReader br, Producer<String, String> producer, String topic) {
		this.br = br;
		this.producer = producer;
		this.topic = topic;
	}
	
	@Override
	public void run() {
		ObjectMapper mapper = new ObjectMapper();
		String line;
		
		try {
			// Each line is a arXiv paper represented in a JSON Object
			while((line = br.readLine()) != null) {
				JsonNode jsonNode = mapper.readTree(line);
				try {
					String id_str = jsonNode.get("id").asText();
					long lastUpdateTime = getUnixTime(jsonNode.get("update_date").asText());
					
					// Wait before sending an arXiv paper to the output topic
					Thread.sleep(DELAY);
					producer.send(new ProducerRecord<String, String>(topic, 0, lastUpdateTime, id_str, line));					
				} catch(ParseException | NumberFormatException pe){
					System.err.println("Cannot parse date "+ jsonNode.get("update_date").asText());
				}
			}
		} catch(IOException ioe){
			System.err.println(ioe.getMessage());
		} catch(InterruptedException ie){
			System.err.println("Interrupted "+ie.getMessage());
		}
		System.err.println("Finished! Messages were "+ DELAY +" ms from target speed-up times.");
	}

	public long getUnixTime(String dateTime) throws ParseException{
		Date d = ARXIV_DATE.parse( dateTime );
		return d.getTime();
	}
}
