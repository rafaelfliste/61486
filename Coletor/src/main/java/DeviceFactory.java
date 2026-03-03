public class DeviceFactory {
    public static Device create(DeviceConfig cfg) throws Exception {
        if (cfg == null) throw new IllegalArgumentException("cfg is null");
        
        // Suporte para classes customizadas via reflexão
        if (cfg.className != null && !cfg.className.isEmpty()) {
            Class<?> c = Class.forName(cfg.className);
            return (Device) c.getConstructor(String.class, int.class).newInstance(cfg.host, cfg.port);
        }
        
        String t = cfg.type == null ? "winding-analyser" : cfg.type.toLowerCase();
        
        switch (t) {
            case "agilent", "agilent-34401a" -> {
                return new AgilentDevice(cfg.host, cfg.port);
            }
            case "keysight", "keysight-34461a" -> {
                return new KeysightDevice(cfg.host, cfg.port);
            }
            
            case "yokogawa", "yokogawa-wt333e" -> {
                return new YokogawaDevice(cfg.host, cfg.port);
            }
            case "winding-analyser", "winding-analyser-2293" -> {
                return new WindingAnalyserDevice(cfg.host, cfg.port);
            }
            
            default -> throw new IllegalArgumentException("Unknown device type: " + cfg.type);
        }
    }
}
