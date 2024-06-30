package org.mdp.g10;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.mdp.kafka.def.KafkaConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertiesSelector {
	static final private String SEPARATOR = ";;";
	public static void main(String[] args) {
		if(args.length != 2) {
			System.err.println("Usage [inputTopic] [outputTopic]");
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		Properties props = KafkaConstants.PROPS;
		props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		Producer<String, String> producer = new KafkaProducer<String, String>(props);
		
		// The consumer subscribes to the input topic
		consumer.subscribe(Arrays.asList(args[0]));
		String selected_properties = "";
		try {
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
				
				// For all records in the batch 
				for(ConsumerRecord<String, String> paper_record : records) {
					// Extract and the categories and abstracts of the papers received by the consumer
					JsonNode paperJsonNode = mapper.readTree(paper_record.value());
					selected_properties = paperJsonNode.get("abstract").asText() + SEPARATOR + paperJsonNode.get("categories");
					
					// Send the extracted properties to the output topic
					producer.send(new ProducerRecord<>(args[1], 0, paper_record.timestamp(), paper_record.key(), selected_properties));
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			consumer.close();
		}
	}
}
