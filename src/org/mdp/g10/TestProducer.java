package org.mdp.g10;

import java.util.Date;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.mdp.kafka.def.KafkaConstants;

public class TestProducer {
	String output_topic;
	public TestProducer(String output_topic) {
		this.output_topic = output_topic;
	}
	
	public void produce() {
		Thread producer_thread = new Thread() {
			public void run() {
				int partition = 0;
				
				Producer<String, String> producer = new KafkaProducer<String, String>(KafkaConstants.PROPS);
				try {
					int i = 0;
					while(true) {
						long timestamp = System.currentTimeMillis();
						String key = Integer.toString(i);
						String value = new Date().toString();
						
						producer.send(new ProducerRecord<String,String>(output_topic, partition, timestamp, key, value));
						Thread.sleep(1000);
						i++;
						
					}
				} catch (InterruptedException v) {
					System.err.println(v);
				} finally {
					producer.close();
				}
			}
		};
		producer_thread.start();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: [outputTopic]");
		}
		TestProducer tp = new TestProducer(args[0]);
		tp.produce();
	}

}
