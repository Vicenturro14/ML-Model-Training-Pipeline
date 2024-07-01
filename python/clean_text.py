import re
import sys
from kafka import KafkaConsumer, KafkaProducer
from nltk.corpus import stopwords
import nltk
import uuid

try:
    stop_words = set(stopwords.words("english"))
except LookupError:
    nltk.download("stopwords")
    stop_words = set(stopwords.words("english"))


def clean_text(text: str):
    text = text.lower()
    text = re.sub(r"[^\w\s]", "", text)
    text = " ".join([word for word in text.split() if word not in stop_words])
    return text


def cleaner(input_topic, output_topic):
    # Configurar el consumidor de Kafka
    group_id = str(uuid.uuid4())

    print("Configurando consumidor de Kafka...")
    consumer = KafkaConsumer(
        bootstrap_servers=["c1:9092"],
        group_id=group_id,
        key_deserializer=lambda m: m.decode("utf-8"),
        value_deserializer=lambda m: m.decode("utf-8"),
        auto_offset_reset="earliest",
    )

    print("Configurando productor de Kafka...")
    # Send the cleaned text to the output topic
    producer = KafkaProducer(
        bootstrap_servers=["c1:9092"],
    )

    print("Subscribiendo al topic de Kafka...")
    consumer.subscribe([input_topic])

    print("Consumiendo mensajes de Kafka...")

    print()
    try:
        while True:
            # every ten milliseconds get all records in a batch
            records = consumer.poll(timeout_ms=10, max_records=1000)
            # for all records in the batch
            for _, messages in records.items():
                print(type(messages))
                for message in messages:
                    # The record is: "abstract;;class"
                    data = message.value.split(";;")
                    abstract = data[0]
                    categories = data[1]
                    # Clean the text
                    clean_abstract = clean_text(abstract)

                    # Separamos las categorias en una lista de categorias
                    categories_list = categories.split()
                    # Si alguna de las categorias EMPIEZA CON "cs." cambiamos
                    # esa por un 1, si no por un 0
                    categories_one_hot = [
                        1 if cat.startswith("cs.") else 0 for cat in categories_list
                    ]
                    # Si la lista de categorias contiene un 1, la etiqueta es 1
                    # Si no, la etiqueta es 0
                    label = 1 if 1 in categories_one_hot else 0

                    # Juntar el texto limpio con la etiqueta
                    final_msg = str(clean_abstract) + ";;" + str(label)
                    print("Mensaje final: ", final_msg)
                    encoded_msg = final_msg.encode("utf-8")
                    producer.send(output_topic, value=encoded_msg)
                    print("Mensaje enviado a Kafka")

    except KeyboardInterrupt:
        print("Interrupted by user")

    except Exception as e:
        print(f"An error occurred: " + str(e))

    finally:
        consumer.close()
        producer.close()


if __name__ == "__main__":
    args = sys.argv
    if len(args) != 3:
        print("Usage: python clean_text.py <input_topic> <output_topic>")
        sys.exit(1)
    input_topic = args[1]
    output_topic = args[2]
    cleaner(input_topic, output_topic)
