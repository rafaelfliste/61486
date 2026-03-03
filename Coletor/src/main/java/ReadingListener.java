/**
 * Interface para capturar eventos de leitura de equipamentos.
 * Permite desacoplamento entre DeviceManager e consumidores de dados.
 */
public interface ReadingListener {
    
    /**
     * Chamado quando um equipamento se conecta com sucesso
     */
    void onDeviceConnected(String deviceId, String identification);
    
    /**
     * Chamado em cada leitura bem-sucedida
     */
    void onReadingSuccess(String deviceId, String value);
    
    /**
     * Chamado quando há erro em uma leitura
     */
    void onReadingError(String deviceId, String errorMessage);
    
    /**
     * Chamado quando há erro ao conectar um equipamento
     */
    void onDeviceError(String deviceId, String errorMessage);
}
