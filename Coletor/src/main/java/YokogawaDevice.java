/**
 * Driver para Yokogawa WT333E - Medidor de Potência
 * Se comunica via RS-232, USB ou Ethernet
 * Suporta SCPI (porta padrão 5025) e Modbus TCP (porta 502)
 * 
 * Recursos: Medição de potência, energia, harmônicos, fator de potência
 */
public class YokogawaDevice implements Device {
    private final String host;
    private final int port;
    private ScpiConnection connection;

    public YokogawaDevice(String host, int port) {
        this.host = host;
        this.port = port;
        this.connection = new ScpiConnection("Yokogawa WT333E");
    }

    @Override
    public String getName() {
        return "yokogawa-wt333e@" + host + ":" + port;
    }

    @Override
    public void connect() throws Exception {
        connection.connect(host, port);
        String id = identify();
        if (id == null || id.isEmpty()) {
            throw new Exception("Yokogawa WT333E não responde");
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
        String power = readActivePower();
        String voltage = connection.measureVoltage();
        String current = connection.measureCurrent();
        return String.format("P=%s W, U=%s V, I=%s A", power, voltage, current);
    }

    // ===== Custom methods for Yokogawa =====

    public String readActivePower() throws Exception {
        return connection.query("MEAS:POW:REAL? 1");
    }

    public String readReactivePower() throws Exception {
        return connection.query("MEAS:POW:REACTIVE? 1");
    }

    public String readApparentPower() throws Exception {
        return connection.query("MEAS:POW:APPARENT? 1");
    }

    public String readPowerFactor() throws Exception {
        return connection.query("MEAS:POW:FACTOR? 1");
    }

    public String readEnergy() throws Exception {
        return connection.query("MEAS:ENER? 1");
    }

    public String readRmsVoltage() throws Exception {
        return connection.measureVoltage();
    }

    public String readRmsCurrent() throws Exception {
        return connection.measureCurrent();
    }

    public String readFrequency() throws Exception {
        return connection.measureFrequency();
    }

    public String readHarmonics() throws Exception {
        return connection.query("MEAS:THD? 1");
    }
}
