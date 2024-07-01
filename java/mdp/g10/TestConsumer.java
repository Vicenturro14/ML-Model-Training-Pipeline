package org.mdp.g10;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.mdp.kafka.def.KafkaConstants;

public class TestConsumer {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: [inputTopic]");
		}
		Properties props = KafkaConstants.PROPS;
		props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		
		// The consumer subscribes to the input topic
		consumer.subscribe(Arrays.asList(args[0]));
		try {
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
				for (ConsumerRecord<String, String> single_record : records) {
					System.out.println(single_record.key() + single_record.value());
				}
			}
		} finally {
			consumer.close();
		}
	}

}
