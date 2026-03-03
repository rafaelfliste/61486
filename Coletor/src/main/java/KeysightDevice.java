/**
 * Driver para Keysight 34461A - Multímetro Digital
 * Se comunica via USB e LAN usando protocolo SCPI (porta 5025)
 * 
 * Substitui o Agilent 34401A com mais recursos
 * Recursos: Medição de resistência, tensão, corrente, frequência, temperatura
 */
public class KeysightDevice implements Device {
    private final String host;
    private final int port;
    private ScpiConnection connection;

    public KeysightDevice(String host, int port) {
        this.host = host;
        this.port = port;
        this.connection = new ScpiConnection("Keysight 34461A");
    }

    @Override
    public String getName() {
        return "keysight-34461a@" + host + ":" + port;
    }

    @Override
    public void connect() throws Exception {
        connection.connect(host, port);
        String id = identify();
        if (id == null || id.isEmpty()) {
            throw new Exception("Keysight 34461A não responde");
        }
    }

    @Override
    public void disconnect() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public String identify() throws Exception {
        return connection.identify();
    }

    @Override
    public void startMeasurement() throws Exception {
        connection.reset();
    }

    @Override
    public String readMeasurement() throws Exception {
        String res = connection.measureResistance();
        String volt = connection.measureVoltage();
        String curr = connection.measureCurrent();
        return String.format("R=%s Ω, U=%s V, I=%s A", res, volt, curr);
    }

    // ===== Custom methods for Keysight =====

    public String readResistance() throws Exception {
        return connection.measureResistance();
    }

    public String readVoltage() throws Exception {
        return connection.measureVoltage();
    }

    public String readCurrent() throws Exception {
        return connection.measureCurrent();
    }

    public String readFrequency() throws Exception {
        return connection.measureFrequency();
    }

    public void setMeasurementRange(String type, String range) throws Exception {
        if ("resistance".equalsIgnoreCase(type)) {
            connection.setResistanceRange(range);
        } else if ("voltage".equalsIgnoreCase(type)) {
            connection.setVoltageRange(range);
        }
    }
}
