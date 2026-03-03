import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Aplicação para leitura de múltiplos equipamentos com agendamento periódico.
 * Suporta configuração via devices.json.
 */
public class Main implements ReadingListener {
    
    private DeviceManager deviceManager;
    private ScheduledExecutorService scheduler;
    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_POOL_SIZE = 1;

    public static void main(String[] args) {
        System.out.println("Iniciando app-leitura ...");
        Main app = new Main();
        app.run();
    }

    private void run() {
        try {
            // Carrega configurações
            List<DeviceConfig> configs = loadConfigurations();
            
            if (configs.isEmpty()) {
                System.err.println("Nenhuma configuração de equipamento disponível!");
                System.exit(1);
                return;
            }

            // Calcula tamanho do pool baseado na quantidade de equipamentos
            int poolSize = Math.min(
                Math.max(configs.size(), MIN_POOL_SIZE), 
                MAX_POOL_SIZE
            );
            scheduler = Executors.newScheduledThreadPool(poolSize);

            // Inicializa gerenciador de equipamentos
            deviceManager = new DeviceManager(scheduler, this);

            // Adiciona todos os equipamentos
            int connectedCount = deviceManager.addDevices(configs);
            System.out.println("Equipamentos conectados: " + connectedCount + "/" + configs.size());

            if (connectedCount == 0) {
                System.err.println("Nenhum equipamento foi conectado com sucesso!");
                System.exit(1);
                return;
            }

            // Registra shutdown hook para limpeza de recursos
            setupShutdownHook();
            
            // Thread principal permanece ativa enquanto houver leituras agendadas
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Erro na aplicação: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Carrega configurações de devices.json
     */
    private List<DeviceConfig> loadConfigurations() throws Exception {
        Path devicesPath = Path.of("devices.json");
     
        if (!Files.exists(devicesPath)) {
                System.err.println("Arquivo devices.json não encontrado!");
                return new ArrayList<>();
        }

        // Carregar devices.json usando Jackson
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> root = om.readValue(devicesPath.toFile(), new TypeReference<Map<String, Object>>() {});
        
        if (root == null || !root.containsKey("devices")) {
            System.err.println("Nenhum equipamento configurado em devices.json!");
            return new ArrayList<>();
        }
        
        // Converter devices array para List<DeviceConfig>
        List<DeviceConfig> devices = om.convertValue(
            root.get("devices"), 
            new TypeReference<List<DeviceConfig>>() {}
        );
        
        if (devices == null || devices.isEmpty()) {
            System.err.println("Nenhum equipamento configurado em devices.json!");
            return new ArrayList<>();
        }
        
        System.out.println("Carregadas " + devices.size() + " configuração(ões) de devices.json");
        return devices;
    }

    /**
     * Registra rotina de desligamento para limpeza de recursos
     */
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nEncerrando aplicação...");
            if (deviceManager != null) {
                deviceManager.disconnectAll();
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            System.out.println("Desconexão concluída.");
        }));
    }

    // ===== Implementação de ReadingListener =====

    @Override
    public void onDeviceConnected(String deviceId, String identification) {
        System.out.println("[" + deviceId + "] Conectado: " + identification);
    }

    @Override
    public void onReadingSuccess(String deviceId, String value) {
        String json = String.format("{\"device\":\"%s\",\"value\":\"%s\",\"timestamp\":%d}", 
            deviceId, value, System.currentTimeMillis());
        System.out.println("[" + deviceId + "] " + json);
    }

    @Override
    public void onReadingError(String deviceId, String errorMessage) {
        System.err.println("[" + deviceId + "] Erro na leitura: " + errorMessage);
    }

    @Override
    public void onDeviceError(String deviceId, String errorMessage) {
        System.err.println("[" + deviceId + "] " + errorMessage);
    }
}
