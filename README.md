
* **Nombre:** Dimas Fabian Jimenez Lobos
* **Carnet:** 0905-20-6300
* **Curso:** Programación III
* **Link Drive:** https://drive.google.com/drive/folders/1FUAnwkwZ5bTMnZ7w27NwA08FCTmBFgHU?usp=sharing

## Arquitectura del Sistema

La solución se basa en el desacoplamiento de responsabilidades mediante un Broker de Mensajería (**RabbitMQ**), permitiendo que la captura de datos y su procesamiento final ocurran de forma independiente.

### Infraestructura con Docker
Para este proyecto, se decidió desplegar **RabbitMQ** mediante un contenedor Docker (`rabbitmq:3-management`).
* **Por qué Docker:** Garantiza un entorno aislado y reproducible. Evita conflictos de configuración local y permite monitorear el flujo de mensajes en tiempo real a través de la interfaz de administración, facilitando la depuración del flujo entre el Producer y el Consumer.


## 1. Componente: Producer (Emisor)
El Producer es el punto de entrada de la información al sistema.

* **Función:** Realiza peticiones `GET` de forma periódica a la API para obtener lotes de transacciones.
* **Lógica de Mensajería:** Implementa una estrategia de **Routing Key** basada en el nombre del banco destino. Esto permite que RabbitMQ clasifique y distribuya los mensajes en colas específicas automáticamente.
* **Detalle:** Utiliza un cliente HTTP asíncrono configurado con *timeouts* de conexión para evitar bloqueos si la API de origen presenta latencia.

## 2. Componente: Consumer (Procesador)
Es donde reside la lógica de negocio y transformación de datos.

* **Procesamiento Multihilo (Paralelismo):** - Implementa un `ExecutorService` con un **Fixed Thread Pool**. 
  - **Razón:** Permite procesar múltiples colas (bancos) simultáneamente. Cada hilo gestiona el ciclo de vida de un mensaje de forma independiente, optimizando el rendimiento del CPU.
* **Transformación de Datos:**
  - **UUID:** Se sustituye el ID original por un identificador único universal de 128 bits. Esto garantiza **idempotencia** y evita colisiones en la base de datos de destino.
  - **Identificación:** Se inyectan los campos obligatorios `nombre` y `carnet` en la raíz del objeto.
  - **Timestamp:** Se agrega el campo `fechaCreacion` dentro del objeto `detalle` para trazabilidad cronológica.
* **Resiliencia y Fiabilidad:**
  - **Manual Acks:** El sistema no confirma la lectura del mensaje a RabbitMQ hasta recibir un `200 OK` del Storage final.
  - **Gestión de Errores HTTP:** - **400 (Bad Request):** Descarte automático para evitar bucles infinitos de datos corruptos.
    - **500 (Internal Error):** Reencolamiento (`requeue=true`) para intentar el procesamiento cuando el servidor de destino se estabilice.

## Stack Tecnológico
* **Lenguaje:** Java 11 (Uso de `java.net.http.HttpClient` y `java.time`).
* **Broker:** RabbitMQ operado sobre **Docker**.
* **Serialización:** Jackson Databind para el manejo eficiente de JSON.
* **Logging:** Log4j2 para monitoreo de eventos en consola.
* **Gestión:** Maven.

## Guía de Ejecución
1. **Levantar RabbitMQ en Docker** 
2. **Configuración:** Revisar las URLs de los servicios AWS en las clases `Client`.
3. **Ejecución del Consumer:** Iniciar primero para asegurar que el Exchange y las colas estén declarados.
4. **Ejecución del Producer:** Iniciar para comenzar la ingesta de datos desde la API de origen. En caso de no haber datos en las colas este se queda escuchando por si se ingresa nuevos datos para procesarlos.