public interface Device {
    String getName();
    void connect() throws Exception;
    void disconnect() throws Exception;
    String identify() throws Exception;
    void startMeasurement() throws Exception;
    String readMeasurement() throws Exception;
}
