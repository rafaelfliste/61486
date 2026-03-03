/**
 * Driver para Agilent 34401A - Multímetro Digital
 * Se comunica via RS-232 ou GPIB usando protocolo SCPI
 * 
 * Recursos: Medição de resistência, tensão, corrente, frequência
 */
public class AgilentDevice implements Device {
    private final String host;
    private final int port;
    private ScpiConnection connection;

    public AgilentDevice(String host, int port) {
        this.host = host;
        this.port = port;
        this.connection = new ScpiConnection("Agilent 34401A");
    }

    @Override
    public String getName() {
        return "agilent-34401a@" + host + ":" + port;
    }

    @Override
    public void connect() throws Exception {
        connection.connect(host, port);
        // Verifica conexão com comando de identificação
        String id = identify();
        if (id == null || id.isEmpty()) {
            throw new Exception("Agilent 34401A não responde");
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
        return String.format("R=%s Ω, U=%s V", res, volt);
    }

    // ===== Custom methods for Agilent =====

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
}
