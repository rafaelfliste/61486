import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Conexão genérica para equipamentos SCPI (Standard Commands for Programmable Instruments).
 * Suporta vários equipamentos: Agilent 34401A, Keysight 34461A, Yokogawa WT333E, etc.
 */
public class ScpiConnection {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String deviceName;

    public ScpiConnection(String deviceName) {
        this.deviceName = deviceName;
    }

    public void connect(String host, int port) throws Exception {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public String sendCommand(String cmd) throws Exception {
        if (writer == null) {
            throw new Exception("Not connected");
        }
        writer.write(cmd + "\n");
        writer.flush();
        String response = reader.readLine();
        return response != null ? response.trim() : "";
    }

    public String query(String cmd) throws Exception {
        return sendCommand(cmd);
    }

    public void close() throws Exception {
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
            throw new Exception("Erro ao fechar conexão SCPI: " + firstError.getMessage(), firstError);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // ===== SCPI Standard Commands =====

    public String identify() throws Exception {
        return query("*IDN?");
    }

    public String reset() throws Exception {
        sendCommand("*RST");
        return "OK";
    }

    public String clearStatus() throws Exception {
        sendCommand("*CLS");
        return "OK";
    }

    public String getStatus() throws Exception {
        return query("*STB?");
    }

    // ===== Measurement Commands =====

    public String measureResistance() throws Exception {
        return query("MEAS:RES?");
    }

    public String measureVoltage() throws Exception {
        return query("MEAS:VOLT?");
    }

    public String measureCurrent() throws Exception {
        return query("MEAS:CURR?");
    }

    public String measureFrequency() throws Exception {
        return query("MEAS:FREQ?");
    }

    // ===== Configuration Commands =====

    public void setResistanceRange(String range) throws Exception {
        sendCommand("CONF:RES " + range);
    }

    public void setVoltageRange(String range) throws Exception {
        sendCommand("CONF:VOLT " + range);
    }

    public String getDeviceName() {
        return deviceName;
    }
}
