package umg.banca.producer.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.fasterxml.jackson.databind.ObjectMapper;
import umg.banca.model.Transaccion;
import umg.banca.producer.config.RabbitmqConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitmqPublisher implements MessagePublisher {
    private static final Logger logger = LogManager.getLogger(RabbitmqPublisher.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final double MONTO_LIMITE = 4000.00;

    
    @Override // El contrato de la interfaz
    public void publish(Transaccion t) throws Exception {
        try (Connection conn = RabbitmqConfig.getConnection(); 
             Channel channel = conn.createChannel()) {
            
        	 String queueName;
             if (t.getMonto() > MONTO_LIMITE) {
                 queueName = "cola_rechazados";
                 logger.warn("Transacción {} rechazada - monto {} excede el límite - estado: RECHAZADO", t.getIdTransaccion(), t.getMonto());
             } else {
                 // Lógica de cola dinámica por banco
                 queueName = t.getBancoDestino().toLowerCase().trim().replace(" ", "_");
                 logger.info("Transacción {} enrutada a cola por banco: {}", t.getIdTransaccion(), queueName);
             }
        	
            // Declaramos la cola (Durable para que no se borre)
            channel.queueDeclare(queueName, true, false, false, null);
            
            // Convertimos objeto a JSON
            String messageJson = objectMapper.writeValueAsString(t);
            
            // Publicamos
            channel.basicPublish("", queueName, null, messageJson.getBytes("UTF-8"));
            logger.info("Transacción {} enviada a la cola: {}", t.getIdTransaccion(), queueName);
        }
    }
}