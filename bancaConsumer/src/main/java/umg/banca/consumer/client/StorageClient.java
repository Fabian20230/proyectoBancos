package umg.banca.consumer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import umg.banca.model.Transaccion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StorageClient {
    private static final Logger logger = LogManager.getLogger(StorageClient.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
   private static final String STORAGE_URL = "https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones";
   
    //Api de Pruebas de envio
//    private static final String STORAGE_URL = "https://webhook.site/e77dcbf2-d907-4c5a-ab47-38cb9cbb48d0";
    
    public int sendToStorage(Transaccion transaccion) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(transaccion);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STORAGE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        int statusCode = response.statusCode();
        logger.info("Respuesta del servidor: {}", statusCode);
        
        return statusCode;
    }
}
