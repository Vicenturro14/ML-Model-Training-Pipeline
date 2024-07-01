# MDP Project G10: ML model training pipeline

## Uso de g10-pipeline.jar
Este archivo compilado se debe ejecutar por consola entregando como argumentos la clase a ejecutar y los argumentos que recibe la clase.
```
java -jar [clase_por_ejecutar] [argumento_de_clase_1] ... [argumento_de_clase_n]
```

Las clases disponibles para ejecutar son ConsumerTest, ProducerTest, ArxivSimulator y PropertiesSelector.

### ConsumerTest
#### Descripción
Es un consumidor de Kafka de prueba, que imprime en la consola la concatenación de la llave y el valor del registro consumido. Tiene el objetivo de probar que un productor de Kafka esté creando y enviando los registros deseados. 
#### Uso
La clase recibe como argumento el nombre de topic desde el cual consumirá registros.

```
java -jar ConsumerTest [input_topic]
```

### ProducerTest
#### Descripción
Es un productor de Kafka de prueba, que envía registstros con llave correspondiente al número de registros envíados (un contador) y el timestamp del envío como valor. Tiene como objetivo crear registros de prueba.
#### Uso
La clase recibe como argumento el nombre del topic al que enviará los registros

```
java -jar ProducerTest [output_topic]
```

### ArxivSimulator
#### Descripción
Se encarga de generar un streaming a partir de un archivo JSON con datos. Tiene el objetivo de simular un streaming de papers de arXiv.
### Uso
La clase recibe como argumentos la ruta del archivo JSON con datos para generar el streaming y el nombre del topic al cual enviar los registrsos. El archivo recibido debe tener un objeto JSON por línea y cada uno debe contener las propiedades 'id' y 'update_date'.

```
java -jar ArxivSimulator [JSON_file_path] [output_topic]
```

### PropertiesSelector
#### Descripción
Recibe registros con objetos JSON y extrae las propiedades 'abstract' y 'categories' de estos. Luego envía una concatenación de las propiedades extraidas con los caracteres ";;" como separador ("abstract;;categories").
#### Uso
La clase recibe como argumentos los nombres de los topics de entrada y salida respectivamente. Se espera que los registros recibidos sean objetos JSON que contengan las propiedades 'abstract' y 'categories'.
```
java -jar PropertiesSelector [input_topic] [output_topic]
```

### clean_text.py
#### Descripción
Recibe el abstract y la categoria en formato de texto y concatenados con ";;", y realiza 2 tareas principales de procesamiento:
1. Procesa el texto sacando las stopwords (un conjunto de palabras muy comunmente usadas en un lenguaje), los caracteres especiales y dejando todo en minúsculas.
2. Procesa las categorías de tal forma que si alguna de estas es de ciencias de la computación (ie. tiene una categoría que empieza por "cs."), entonces deja una variable label con un 1. En caso contrario dejará label con un 0.
Luego de procesar el abstract y las categorías, las concatena como texto, y las envía al topico de salida con el formato "abstract_limpio;;label".
#### Uso
La clase recibe como argumentos los nombres de los topics de entrada y salida respectivamente. Se espera que los records recibidos traigan como valor un texto de la forma "abstract;;categorias", donde estos deben estar asociados una misma publicación.
```
python clean_text.py [input_topic] [output_topic]
```

### retrain_model.py
#### Descripción
Recibe el abstract limpio y el label enviados por clean_text.py con el formato "abstract;;label", recolecta varios de estos records en una lista, y al cumplir un número N de ejemplos los junta formando un dataframe con el que se reentrena un modelo de ML, un DecisionTreeClassifier que predice si una publicación es o no de computer science.
#### Uso
La clase recibe como argumento el tópico de input por donde se enviarán los datos. Se asume que existe un dataset con datos de testing en la ruta ./test_data.csv, un vectorizador en ./vectorizer.pkl, y un modelo de machine learning en ./pretrained_model.pkl.
```
python retrain_model.py [input_topic]
```
