package umg.banca.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import umg.banca.producer.config.RabbitmqConfig; // Importamos tu config de la imagen
import umg.banca.consumer.handler.TransactionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainConsumer {
    private static final Logger logger = LogManager.getLogger(MainConsumer.class);
    // Estos nombres deben coincidir con tus colas de la imagen (bac, banrural, bi, gyt)
    private static final String[] QUEUES = {"bac", "banrural", "bi", "gyt"};

    public static void main(String[] args) {
        TransactionHandler handler = new TransactionHandler();

        try {
            Connection connection = RabbitmqConfig.getConnection();
            Channel channel = connection.createChannel();

            logger.info("--- CONSUMER INICIADO: ESCUCHANDO COLAS ---");

            for (String queue : QUEUES) {
                // Escuchamos cada cola de tu RabbitMQ -> (false = ACK manual)
                channel.basicConsume(queue, false, handler.createCallback(channel), consumerTag -> {});
                
                logger.info("Suscrito a la cola: {}", queue);
            }

            // Mantiene el programa corriendo
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("Error en el Consumer: {}", e.getMessage());
        }
    }
}