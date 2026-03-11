package umg.banca.consumer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Channel;
import umg.banca.model.Transaccion;
import umg.banca.consumer.client.StorageClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionHandler {
    private static final Logger logger = LogManager.getLogger(TransactionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StorageClient storageClient = new StorageClient();

    public DeliverCallback createCallback(Channel channel) {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            try {
                Transaccion t = objectMapper.readValue(message, Transaccion.class);
                
                //Concatenamos el usuario de GITHUB
                String idOriginal = t.getIdTransaccion();
                t.setIdTransaccion(idOriginal + "-Fabian20230");
                t.setNombre("Dimas Fabian Jimenez Lobos");
                t.setCarnet("0905-20-6300");
                
                logger.info("Procesando: {} del banco: {}", t.getIdTransaccion(),t.getNombre(), t.getCarnet(), t.getBancoDestino());

                int statusCode = storageClient.sendToStorage(t);
                
                if (statusCode == 200 || statusCode == 201) {
                    logger.info("Transacción enviada exitosamente: {}", t.getIdTransaccion());
                    Thread.sleep(400);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } else {
                    logger.error("Error en servidor. Status: {}", statusCode);
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                }
                
            } catch (Exception e) {
                logger.error("Error: {}. Reencolando...", e.getMessage());
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };
    }
}
