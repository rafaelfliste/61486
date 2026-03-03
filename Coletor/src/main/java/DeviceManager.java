import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Gerencia o ciclo de vida e leitura periódica de múltiplos equipamentos.
 * Responsável por conectar, desconectar e agendar leituras de equipamentos.
 */
public class DeviceManager {
    private final List<Device> devices = new ArrayList<>();
    private final Map<String, DeviceConfig> configMap = new HashMap<>();
    private final Map<String, Device> deviceMap = new HashMap<>();
    private final ScheduledExecutorService scheduler;
    private final ReadingListener listener;

    public DeviceManager(ScheduledExecutorService scheduler, ReadingListener listener) {
        this.scheduler = scheduler;
        this.listener = listener;
    }

    /**
     * Inicializa e conecta um equipamento, agendando leituras periódicas
     */
    public boolean addDevice(DeviceConfig config) {
        String deviceId = (config != null) ? config.id : "<unknown>";
        Device device = null;
        try {
            if (config == null) {
                throw new IllegalArgumentException("Config inválida: objeto nulo");
            }
            if (config.id == null || config.id.isBlank()) {
                throw new IllegalArgumentException("Config inválida: id é obrigatório");
            }
            if (config.intervalSec <= 0) {
                throw new IllegalArgumentException(
                    "Config inválida para " + config.id + ": intervalSec deve ser maior que zero"
                );
            }
            if (deviceMap.containsKey(config.id)) {
                throw new IllegalArgumentException("Config inválida: id duplicado '" + config.id + "'");
            }

            device = DeviceFactory.create(config);
            device.connect();
            
            String identification = device.identify();
            listener.onDeviceConnected(config.id, identification);

            // Garante inicialização antes do ciclo periódico de leituras.
            device.startMeasurement();
            
            configMap.put(config.id, config);
            deviceMap.put(config.id, device);
            devices.add(device);
            
            schedulePeriodicReading(device, config);
            return true;
            
        } catch (Exception ex) {
            listener.onDeviceError(deviceId, "Falha ao conectar: " + ex.getMessage());
            if (device != null) {
                try {
                    device.disconnect();
                } catch (Exception ignored) {
                    // Erro secundário durante rollback de conexão.
                }
            }
            return false;
        }
    }

    /**
     * Adiciona múltiplos equipamentos em lote
     */
    public int addDevices(List<DeviceConfig> configs) {
        int successCount = 0;
        for (DeviceConfig config : configs) {
            if (addDevice(config)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * Agenda leitura periódica para um equipamento
     */
    private void schedulePeriodicReading(Device device, DeviceConfig config) {
        Runnable readTask = () -> {
            try {
                String value = device.readMeasurement();
                listener.onReadingSuccess(config.id, value);
            } catch (Exception e) {
                listener.onReadingError(config.id, e.getMessage());
            }
        };

        // Primeira leitura imediata, depois a cada intervalSec
        scheduler.scheduleAtFixedRate(
            readTask, 
            0, 
            config.intervalSec, 
            TimeUnit.SECONDS
        );
    }

    /**
     * Desconecta todos os equipamentos
     */
    public void disconnectAll() {
        for (Device device : devices) {
            try {
                device.disconnect();
            } catch (Exception ignored) {
                // Log de desconexão silencioso
            }
        }
        devices.clear();
        configMap.clear();
        deviceMap.clear();
    }

    /**
     * Retorna lista de equipamentos conectados
     */
    public List<Device> getConnectedDevices() {
        return new ArrayList<>(devices);
    }

    /**
     * Retorna equipamento por ID
     */
    public Device getDevice(String deviceId) {
        return deviceMap.get(deviceId);
    }

    /**
     * Retorna quantidade de equipamentos conectados
     */
    public int getDeviceCount() {
        return devices.size();
    }
}
