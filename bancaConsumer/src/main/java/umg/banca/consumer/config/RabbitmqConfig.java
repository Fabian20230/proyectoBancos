package umg.banca.consumer.config;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitmqConfig {

   
    private static final String HOST = "localhost"; 
    private static final int PORT = 5672;
    private static final String USER = "Fabian";
    private static final String PASS = "Fabian1234";

    public static Connection getConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USER);
        factory.setPassword(PASS);
        
        // Retorna una nueva conexión física al Docker
        return factory.newConnection();
    }
}
