package umg.banca.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import umg.banca.producer.config.RabbitmqConfig; // Importamos tu config de la imagen
import umg.banca.consumer.handler.TransactionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainConsumer {
    private static final Logger logger = LogManager.getLogger(MainConsumer.class);
    private static final String[] QUEUES = {"bac", "banrural", "bi", "gyt"};

    public static void main(String[] args) {
    	
    	ExecutorService threadPool = Executors.newFixedThreadPool(QUEUES.length);
       

        try {
            Connection connection = RabbitmqConfig.getConnection();
            
            logger.info("--- CONSUMER INICIADO: ESCUCHANDO COLAS ---");

            for (String queue : QUEUES) {
                
            	threadPool.submit(() -> {

            		try {
                        // Importante: Cada hilo tiene su propio Canal
                        Channel channel = connection.createChannel();
                        
                        //Contador para que un hilo no se sature
                        channel.basicQos(1); 

                        TransactionHandler handler = new TransactionHandler();
                        
                        channel.basicConsume(queue, false, handler.createCallback(channel), consumerTag -> {});
                        logger.info("Hilo dedicado suscrito a la cola: {}", queue);
                        
                    } catch (Exception e) {
                        logger.error("Error al suscribir la cola {}: {}", queue, e.getMessage());
                    }
                }); 
            }
            
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("Error en el Consumer: {}", e.getMessage());
        }
    }
}