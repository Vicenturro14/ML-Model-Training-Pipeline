package org.mdp.g10;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.mdp.kafka.def.KafkaConstants;

public class ArxivSimulator {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: [arXiv_json_file] [arXiv_topic]");
			return;
		}
		String arXiv_topic = args[1];
		String json_file = args[0];
		try {

			BufferedReader papers = new BufferedReader(new InputStreamReader(new FileInputStream(json_file)));			
			Producer<String, String> paperProducer = new KafkaProducer<String, String>(KafkaConstants.PROPS);
			ArxivStream paperStream = new ArxivStream(papers, paperProducer, arXiv_topic);
	
			// Create a thread in charge of the data streaming
			Thread paperThread = new Thread(paperStream);
			paperThread.start();
			
			try {
				paperThread.join();
			} catch(InterruptedException e) {
				System.err.println("Interrupted!");
			}
		} catch(FileNotFoundException e) {
			System.err.println("File" + json_file + "couldn't be opened or found");
		}
		
		
	}
}
