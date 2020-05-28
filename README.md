# Integrazione di TensorFlow Lite con dispositivi MQTT virtuali

Questo progetto consiste in un’applicazione Android che prevede:
• un client MQTT che pubblica le immagini classificate da un modello preconfigurato [TensorFlow Lite](https://tensorflow.org/lite),
• un broker MQTT in esecuzione sullo stesso dispositivo Android,
• un altro client MQTT che si iscrive presso il broker e salva le immagini ricevute sul disco.
Per la parte di acquisizione e classificazione delle immagini, quest’applicazione si basa sull’[esempio per classificazione](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android) presente nel repository ufficiale di TensorFlow Lite.
Il broker è realizzato facendo uso della libreria [Moquette per Android](https://github.com/technocreatives/moquette).
I client MQTT sono invece realizzati sfruttando la libreria open-source [Eclipse Paho](https://github.com/eclipse/paho.mqtt.android).
messaggistica standard dell’ambiente Machine-to-Machine e Internet of Things. [20]
Le mie classi sono relegate al package org.tensorflow.lite.examples.classification.mqtt e il relativo codice è descritto in dettaglio nel file report.pdf.