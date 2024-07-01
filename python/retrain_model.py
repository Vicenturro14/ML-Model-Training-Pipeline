import joblib
import pandas as pd
from kafka import KafkaConsumer
import sys
import uuid
from sklearn.metrics import accuracy_score


def retrain_and_test_model(new_data, model, vectorizer, X_test, y_test):
    # Vectorizar los nuevos datos usando el vectorizador existente
    X_new = vectorizer.transform(new_data["abstracts"])

    # Etiquetas para entrenamiento
    y_new = new_data["labels"]

    # Reentrenar el modelo
    model.fit(X_new, y_new)

    # Guardar el modelo reentrenado
    with open("pretrained_model.pkl", "wb") as f:
        joblib.dump(model, f)

    # Predecir las etiquetas y calcular la accuracy
    y_pred = model.predict(X_test)
    accuracy = accuracy_score(y_test, y_pred)

    print("Modelo reentrenado guardado correctamente.")
    print(f"Accuracy: {accuracy:.2f}")
    print("\n\n")


# Configurar el consumidor de Kafka
def consume_from_kafka(input_topic):
    group_id = str(uuid.uuid4())

    consumer = KafkaConsumer(
        bootstrap_servers=["c1:9092"],
        group_id=group_id,
        auto_offset_reset="earliest",
    )

    consumer.subscribe([input_topic])

    try:
        with open("pretrained_model.pkl", "rb") as f:
            model = joblib.load(f)
        print("Modelo cargado correctamente.")
        with open("vectorizer.pkl", "rb") as f:
            vectorizer = joblib.load(f)
        print("Vectorizer cargado correctamente.")
        # Abrimos el dataset de test
        test_data = pd.read_csv("test_data.csv")
        print("Test data cargado correctamente.")
        X_test = vectorizer.transform(test_data["abstract"])
        print("Test data vectorizado correctamente.")
        y_test = test_data["is_computer_science_paper"]

    except Exception as e:
        print(f"Error al cargar el modelo o vectorizador: {e}")
        sys.exit(1)

    new_data = []
    print("Consumiendo mensajes de Kafka...")
    try:
        while True:
            # The record is: "abstract;;class"
            # Every ten milliseconds get all records in a batch
            batch = consumer.poll(timeout_ms=10, max_records=1000)

            for _, records in batch.items():
                for record in records:
                    # Parsear el mensaje
                    message = record.value.decode("utf-8")
                    abstract, label = message.split(";;")

                    # Añadimos los datos nuevos hasta que tengamos 5 ejemplos
                    new_data.append({"abstracts": abstract, "labels": int(label)})

                    # Cada 20 ejemplos reentrenamos el modelo
                    if len(new_data) == 500:
                        new_data_df = pd.DataFrame(new_data)
                        retrain_and_test_model(
                            new_data_df, model, vectorizer, X_test, y_test
                        )
                        new_data = []

    except KeyboardInterrupt:
        print("Consumo de Kafka interrumpido.")

    finally:
        consumer.close()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Uso: python script.py <input_topic>")
        sys.exit(1)

    input_topic = sys.argv[1]
    consume_from_kafka(input_topic)
