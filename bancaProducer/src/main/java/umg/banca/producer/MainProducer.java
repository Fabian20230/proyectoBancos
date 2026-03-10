package umg.banca.producer;

import umg.banca.model.Lote;
import umg.banca.model.Transaccion;
import umg.banca.producer.client.TransactionClient;
import umg.banca.producer.messaging.RabbitmqPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainProducer {
    private static final Logger logger = LogManager.getLogger(MainProducer.class);

    public static void main(String[] args) {
        // API Get del proyecto
        String apiUrl = "https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones";
        
        TransactionClient client = new TransactionClient();
        RabbitmqPublisher publisher = new RabbitmqPublisher();

        try {
            logger.info("--- INICIANDO PROCESO DE PRODUCCIÓN ---");
            
            // 1. Obtener el lote
            Lote lote = client.fetchTransactions(apiUrl);
            
            if (lote != null && lote.getTransacciones() != null) {
                logger.info("Lote {} recibido con {} transacciones.", 
                             lote.getLoteId(), lote.getTransacciones().size());

                // 2. Procesar y enviar cada transacción individualmente
                for (Transaccion t : lote.getTransacciones()) {
                    publisher.publish(t);
                    
                    Thread.sleep(200);
                }
                
                logger.info("--- PROCESO FINALIZADO EXITOSAMENTE ---");
            }

        } catch (Exception e) {
            logger.error("Error crítico en el Producer: {}", e.getMessage());
        }
    }
}
