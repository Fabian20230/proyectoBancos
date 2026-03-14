package umg.banca.consumer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Channel;
import umg.banca.model.Transaccion;
import umg.banca.consumer.client.StorageClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionHandler {
    private static final Logger logger = LogManager.getLogger(TransactionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StorageClient storageClient = new StorageClient();

    public DeliverCallback createCallback(Channel channel) {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();
            
            try {
                Transaccion t = objectMapper.readValue(message, Transaccion.class);
                
                String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String nuevoIdUnico = java.util.UUID.randomUUID().toString();
                t.setIdTransaccion(nuevoIdUnico);
                t.getDetalle().setFechaCreacion(fechaActual);
                t.setNombre("Dimas Fabian Jimenez Lobos");
                t.setCarnet("0905-20-6300");
                
                logger.info("Procesando TX: {} | Banco: {}", 
                            t.getIdTransaccion(), t.getBancoDestino());

                // Validacion de las respuestas del servidor
                int statusCode = storageClient.sendToStorage(t);
                
                if (statusCode == 200 || statusCode == 201) {
                    logger.info("Transacción guardada exitosamente: {}", t.getIdTransaccion());
                    Thread.sleep(200);
                    channel.basicAck(deliveryTag, false);
                    
                } else if (statusCode == 400) {
                	//En caso de datos invalidos evitamos la reencolacion :)
                    logger.error("Error 400: Datos inválidos enviados para la TX: {}", t.getIdTransaccion());
                    channel.basicNack(deliveryTag, false, false);
                    
                }else if (statusCode >= 500) {
                    logger.error("Error 500: Error interno en el servidor. Reintentando TX: {}", t.getIdTransaccion());
                    channel.basicNack(deliveryTag, false, true);
                }else {
                	 // En caso de otros errores Reencolamos
                    logger.warn("Respuesta inesperada del servidor ({}). Reencolando...", statusCode);
                    channel.basicNack(deliveryTag, false, true);
                }
                
                
        } catch (Exception e) {
                logger.error("Error crítico procesando mensaje: {}. Reencolando...", e.getMessage());
                channel.basicNack(deliveryTag, false, true);
            }
        };
    }
}