﻿# MDP Project G10: ML model training pipeline

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
