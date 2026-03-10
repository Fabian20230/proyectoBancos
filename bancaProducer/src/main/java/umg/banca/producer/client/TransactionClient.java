package umg.banca.producer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import umg.banca.model.Lote;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionClient {
	
    private static final Logger logger = LogManager.getLogger(TransactionClient.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Lote fetchTransactions(String url) throws Exception {
        logger.info("Solicitando lote de transacciones a la API...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Jackson convierte el String a Objeto Lote automáticamente
            return objectMapper.readValue(response.body(), Lote.class);
        } else {
            logger.error("La API falló con código: {}", response.statusCode());
            throw new RuntimeException("Error al conectar con la API central");
        }
    }
}
