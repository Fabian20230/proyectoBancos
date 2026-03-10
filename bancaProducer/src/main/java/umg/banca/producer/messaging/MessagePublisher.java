package umg.banca.producer.messaging;

import umg.banca.model.Transaccion;

public interface MessagePublisher {
    // Esto solo define que cualquier publicador debe tener el método publish
    void publish(Transaccion transaccion) throws Exception;
}
