# Recuperación de Información (RI)
# Práctica 1: Preprocesado de documentos
### Pre-Requisitos 📋

- _Visual Studio Code_
- _JDK Java 8_
- _Apache Tika https://www.apache.org/dyn/closer.lua/tika/2.1.0/tika-app-2.1.0.jar_

### Instalación 🔧

_Para poder ejecutar el proyecto:_

IMPORTANTE: copiar el archivo tika-app-2.1.0.jar en el directorio principal del proyecto

_Compilar con:_

```
javac -cp .\tika-app-2.1.0.jar .\src\App.java
```

_Ejecutar con:_

```
java -cp .\tika-app-2.1.0.jar .\src\App.java archivos
```

> Parser de documentos con TIKA

Programa que extrae información de distintos documentos que cuelgan de un directorio. Genera distintas salidas en función del parámetro de entrada.

* -d → Nombre de fichero, tipo, codificación e idioma

* -l → Enlaces del documento

* -t → CSV con la frecuencia de los términos en orden decreciente

## Autores ✒️

* **Francisco Lara Marín**
* **Álvaro Marín Pérez** 

