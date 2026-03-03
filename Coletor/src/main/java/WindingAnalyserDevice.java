import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Driver para Winding Analyser 2293 - Analisador de Enrolamentos
 * Se comunica via Ethernet usando protocolo TCP customizado na porta 50000
 *
 * Recursos: Medição de resistência de enrolamentos, índice de absorção,
 *           fator de potência de perda, tensão de ruptura
 */
public class WindingAnalyserDevice implements Device {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public WindingAnalyserDevice(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getName() {
        return "winding-analyser-2293@" + host + ":" + port;
    }

    @Override
    public void connect() throws Exception {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Testa conexão
        String response = sendCommand("*IDN?");
        if (response == null || response.isEmpty()) {
            throw new Exception("Winding Analyser 2293 não responde");
        }
    }

    @Override
    public void disconnect() throws Exception {
        Exception firstError = null;

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            firstError = e;
        }

        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            if (firstError == null) {
                firstError = e;
            }
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            if (firstError == null) {
                firstError = e;
            }
        } finally {
            writer = null;
            reader = null;
            socket = null;
        }

        if (firstError != null) {
            throw new Exception("Erro ao desconectar Winding Analyser: " + firstError.getMessage(), firstError);
        }
    }

    @Override
    public String identify() throws Exception {
        return sendCommand("*IDN?");
    }

    @Override
    public void startMeasurement() throws Exception {
        sendCommand("INIT:IMM");
    }

    @Override
    public String readMeasurement() throws Exception {
        String resistance = readWindingResistance();
        String absorptionIndex = readAbsorptionIndex();
        return String.format("R=%s Ω, AI=%s", resistance, absorptionIndex);
    }

    // ===== Custom protocol commands for Winding Analyser =====

    private String sendCommand(String cmd) throws Exception {
        writer.write(cmd + "\n");
        writer.flush();
        String response = reader.readLine();
        return response != null ? response.trim() : "";
    }

    public String readWindingResistance() throws Exception {
        return sendCommand("MEAS:RES?");
    }

    public String readAbsorptionIndex() throws Exception {
        return sendCommand("MEAS:AI?");
    }

    public String readDissipationFactor() throws Exception {
        return sendCommand("MEAS:DF?");
    }

    public String readCapacitance() throws Exception {
        return sendCommand("MEAS:CAP?");
    }

    public String readBreakdownVoltage() throws Exception {
        return sendCommand("MEAS:BDV?");
    }

    public String readPhaseAngle() throws Exception {
        return sendCommand("MEAS:PHAS?");
    }

    public String performCompleteTest() throws Exception {
        sendCommand("INIT:ALL");
        // Aguarda resultado (timeout 60 segundos)
        Thread.sleep(2000);
        return sendCommand("FETCH:ALL?");
    }

    public void setTestVoltage(double voltage) throws Exception {
        sendCommand("VOLT " + voltage);
    }

    public void setTestFrequency(double frequency) throws Exception {
        sendCommand("FREQ " + frequency);
    }
}
