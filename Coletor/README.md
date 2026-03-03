# 📊 Sistema de Leitura de Múltiplos Equipamentos de Medição

Aplicação Java para leitura de equipamentos de medição (Agilent, Keysight, Yokogawa, Winding Analyser) com suporte a múltiplos dispositivos, agendamento periódico e comunicação via TCP/IP.

**Java 21 LTS | Maven | Multiplexação com Thread Pool**

---

## 🚀 Quick Start

### 1. Compilar
```bash
mvn clean compile
```

### 2. Executar
```bash
#mvn exec:java -Dexec.mainClass="Main"
mvn "-Dexec.mainClass=Main" exec:java
```

### 3. Configurar (opcional)
Edite `devices.json` na raiz do projeto para adicionar/remover equipamentos.

---

## 📋 Características

✅ **Múltiplos Equipamentos**: Conecte até 10 dispositivos simultaneamente  
✅ **Thread Pool Automático**: Dimensionado conforme quantidade de equipamentos  
✅ **Leitura Periódica**: Agendamento independente por dispositivo  
✅ **Fácil Extensão**: Padrão factory para adicionar novos tipos de equipamentos  
✅ **Listener Events**: Capture eventos de conexão, leitura e erro  
✅ **Protocolo SCPI**: Suporte para equipamentos TCP/IP com SCPI  

---

## 📁 Estrutura do Projeto

```
src/main/java/
├── Main.java                     # Entrada
├── Device.java                   # Interface padrão
├── DeviceConfig.java             # Configuração
├── DeviceManager.java            # Gerenciador
├── DeviceFactory.java            # Factory
├── ReadingListener.java          # Listener
├── AgilentDevice.java            # Multímetro 34401A
├── KeysightDevice.java           # Multímetro 34461A
├── YokogawaDevice.java           # Medidor WT333E
├── WindingAnalyserDevice.java    # Analisador 2293
├── ScpiConnection.java           # Conexão SCPI genérica
└── HttpClientHelper.java         # Utils HTTP
```

**Total: 12 arquivos Java**
**Estrutura plana em `src/main/java`**

---

## ⚙️ Configuração

Edite `devices.json` na raiz do projeto para adicionar/remover equipamentos.

```json
{
  "devices": [
    {
      "id": "agilent-34401a",
      "type": "agilent",
      "host": "192.168.1.100",
      "port": 5025,
      "intervalSec": 15
    },
    {
      "id": "keysight-34461a",
      "type": "keysight",
      "host": "192.168.1.101",
      "port": 5025,
      "intervalSec": 12
    },
    {
      "id": "yokogawa-wt333e",
      "type": "yokogawa",
      "host": "192.168.1.102",
      "port": 5025,
      "intervalSec": 20
    },
    {
      "id": "winding-analyser-2293",
      "type": "winding-analyser",
      "host": "192.168.1.103",
      "port": 50000,
      "intervalSec": 60
    }
  ]
}
```

**Parâmetros:**
- `id`: Identificador único do equipamento (usado em logs)
- `type`: Tipo de equipamento (`agilent`, `keysight`, `yokogawa`, `winding-analyser`)
- `host`: Endereço IP do equipamento
- `port`: Porta TCP (default SCPI: 5025, Winding Analyser: 50000)
- `intervalSec`: Intervalo entre leituras em segundos
- `className` (opcional): Nome completo da classe customizada para tipos não mapeados

---

## 🔧 Adicionar Novo Equipamento (Driver)

### Passo 1: Implementar a interface `Device`

```java
public class MeuDispositivoDevice implements Device {
    private final String host;
    private final int port;
    
    public MeuDispositivoDevice(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    @Override
    public String getName() {
        return "meu-dispositivo@" + host + ":" + port;
    }
    
    @Override
    public void connect() throws Exception {
        // Conectar ao equipamento
    }
    
    @Override
    public void disconnect() throws Exception {
        // Desconectar
    }
    
    @Override
    public String identify() throws Exception {
        return "MeuDispositivo v1.0";
    }
    
    @Override
    public void startMeasurement() throws Exception {
        // Iniciar medição
    }
    
    @Override
    public String readMeasurement() throws Exception {
        return "medição_aqui";
    }
}
```

### Passo 2: Registrar na `DeviceFactory`

Edite `DeviceFactory.java`:

```java
case "meudispositivo":
    return new MeuDispositivoDevice(cfg.host, cfg.port);
```

### Passo 3: Adicionar em `devices.json`

```json
{
  "devices": [
    {
      "id": "meu-dispositivo-1",
      "type": "meudispositivo",
      "host": "192.168.1.100",
      "port": 1234,
      "intervalSec": 10
    }
  ]
}
```

---

## 📡 Equipamentos Suportados

#### Agilent 34401A - Multímetro Digital
- **Tipo**: `agilent` ou `agilent-34401a`
- **Interface**: RS-232 ou GPIB (suportado via TCP)
- **Protocolo**: SCPI
- **Porta default**: 5025
- **Classe**: `AgilentDevice.java`
- **Medições**: Resistência, Tensão, Corrente, Frequência
- **Exemplo**:
```json
{
  "id": "agilent-34401a",
  "type": "agilent",
  "host": "192.168.1.100",
  "port": 5025,
  "intervalSec": 15
}
```

#### Keysight 34461A - Multímetro Digital (Successor to Agilent)
- **Tipo**: `keysight` ou `keysight-34461a`
- **Interface**: USB e LAN
- **Protocolo**: SCPI
- **Porta default**: 5025
- **Classe**: `KeysightDevice.java`
- **Medições**: Resistência, Tensão, Corrente, Frequência, com configuração de range avançada
- **Exemplo**:
```json
{
  "id": "keysight-34461a",
  "type": "keysight",
  "host": "192.168.1.101",
  "port": 5025,
  "intervalSec": 12
}
```

#### Yokogawa WT333E - Medidor de Potência
- **Tipo**: `yokogawa` ou `yokogawa-wt333e`
- **Interface**: RS-232, USB ou Ethernet
- **Protocolo**: SCPI (porta 5025) e Modbus TCP (porta 502)
- **Porta default**: 5025
- **Classe**: `YokogawaDevice.java`
- **Medições**: Potência ativa/reativa/aparente, Energia, Fator de potência, Harmônicos, Tensão RMS, Corrente RMS, Frequência
- **Exemplo**:
```json
{
  "id": "yokogawa-wt333e",
  "type": "yokogawa",
  "host": "192.168.1.102",
  "port": 5025,
  "intervalSec": 20
}
```

#### Winding Analyser 2293 - Analisador de Enrolamentos
- **Tipo**: `winding-analyser` ou `winding-analyser-2293`
- **Interface**: Ethernet
- **Protocolo**: TCP customizado
- **Porta default**: 50000
- **Classe**: `WindingAnalyserDevice.java`
- **Medições**: Resistência de enrolamentos, Índice de absorção, Fator de dissipação, Capacitância, Tensão de ruptura, Ângulo de fase
- **Exemplo**:
```json
{
  "id": "winding-analyser-2293",
  "type": "winding-analyser",
  "host": "192.168.1.103",
  "port": 50000,
  "intervalSec": 60
}
```

#### Protocolo SCPI (Agilent, Keysight, Yokogawa)

| Comando | Descrição | Exemplo |
|---------|-----------|---------|
| `*IDN?` | Identificação | `AGILENT 34401A SN12345` |
| `MEAS:RES?` | Resistência | `1234.56` |
| `MEAS:VOLT?` | Tensão | `230.5` |
| `MEAS:CURR?` | Corrente | `4.25` |
| `MEAS:FREQ?` | Frequência | `50.0` |
| `*RST` | Reset | OK |
| `*CLS` | Clear status | OK |

---

## 💻 Exemplos de Uso

### Usar DeviceManager

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
ReadingListener listener = new Main();
DeviceManager manager = new DeviceManager(scheduler, listener);

DeviceConfig config = new DeviceConfig();
config.id = "meu-equipamento";
config.type = "agilent";
config.host = "192.168.1.10";
config.port = 5025;
config.intervalSec = 5;

manager.addDevice(config);
// ... usar ...
manager.disconnectAll();
```

### Implementar ReadingListener Customizado

```java
class MeuListener implements ReadingListener {
    @Override
    public void onDeviceConnected(String deviceId, String identification) {
        System.out.println("[" + deviceId + "] Conectado: " + identification);
    }
    
    @Override
    public void onReadingSuccess(String deviceId, String value) {
        System.out.println("[" + deviceId + "] Leitura: " + value);
    }
    
    @Override
    public void onReadingError(String deviceId, String errorMessage) {
        System.err.println("[" + deviceId + "] Erro: " + errorMessage);
    }
    
    @Override
    public void onDeviceError(String deviceId, String errorMessage) {
        System.err.println("[" + deviceId + "] Erro de conexão: " + errorMessage);
    }
}
```

---

## 🏗️ Arquitetura

### Padrões de Design

- **Factory Pattern**: `DeviceFactory`
- **Listener Pattern**: `ReadingListener`
- **Strategy Pattern**: `Device` interface
- **Thread Pool Pattern**: `ScheduledExecutorService`

### Separação de Responsabilidades

| Class | Responsabilidade |
|--------|-----------------|
| `Main` | Orquestração, carregamento de config |
| `DeviceManager` | Gerência de ciclo de vida |
| `DeviceFactory` | Criação de dispositivos |
| `Device` | Interface padrão |
| `ReadingListener` | Captura de eventos |
| Device Classes | Implementações específicas (Agilent, Keysight, Yokogawa, Winding Analyser) |

---

## 🔧 Troubleshooting

| Problema | Solução |
|----------|---------|
| Nenhuma configuração | Verifique `devices.json` existe e JSON é válido |
| Equipamento não responde | `ping <host>`, verificar firewall, porta 5025 aberta |
| Leituras intermitentes | Aumentar `intervalSec` para 15-30s, verificar latência |

---

## 🛠️ Comando Maven

```bash
mvn clean compile                          # Compilar
mvn exec:java -Dexec.mainClass="Main"     # Executar
mvn exec:java "-Dexec.mainClass=Main"     #Executar no powershell
mvn clean package                          # JAR executável
java -jar target/app-leitura-1.0.0.jar    # Rodar JAR
```

---

**Última atualização:** Fevereiro 2026  
**Versão Java:** 21 LTS  
**Build Tool:** Maven 3.x
