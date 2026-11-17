import java.util.ArrayList;

// 1. БАЗОВЫЙ КЛАСС ДЛЯ ВСЕХ УСТРОЙСТВ
class Device {
    String id;
    String type;
    boolean isOn;

    public Device(String id, String type) {
        this.id = id;
        this.type = type;
        this.isOn = false; // По умолчанию устройство выключено
    }
}

// По要求ованию задачи: лампа наследуется от Device
class Lamp extends Device {
    public Lamp(String id) {
        super(id, "lamp");
    }
}

// По требованию задачи: термостат наследуется от Device и имеет температуру
class Thermostat extends Device {
    int temperature;

    public Thermostat(String id) {
        super(id, "thermostat");
        this.temperature = 20; // Начальная температура
    }
}

// 2. КЛАСС КОМНАТЫ
class Room {
    String id;
    String name;
    ArrayList<Device> devices; // Список устройств в этой комнате

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
        this.devices = new ArrayList<>();
    }
}

// 3. КОНТРОЛЛЕР УМНОГО ДОМА (Класс, который хранит всё состояние)
class SmartHomeController {
    boolean isInitialized = false; // Флаг: инициализирован ли дом
    ArrayList<Room> rooms = new ArrayList<>();
    ArrayList<Device> allDevices = new ArrayList<>();
    ArrayList<String> history = new ArrayList<>(); // История команд

    // Метод выполнения ОДНОЙ команды (как в условии задачи)
    public void executeCommand(String commandLine) {
        // Очищаем строку от лишних пробелов и комментариев
        String line = commandLine.trim();
        if (line.isEmpty() || line.startsWith("//")) return;
        if (line.contains("//")) {
            line = line.split("//")[0].trim();
        }

        // Записываем команду в историю
        history.add(line);

        // Разбиваем строку на слова
        String[] parts = line.split("\\s+");
        String action = parts[0];

        // 1. Проверяем команду initHome
        if (action.equals("initHome")) {
            isInitialized = true;
            System.out.println("Дом инициализирован.");
            return;
        }

        // 2. Если дом еще не инициализирован, другие команды выполнять нельзя!
        if (!isInitialized) {
            System.out.println("Ошибка: Сначала нужно инициализировать дом (initHome).");
            return;
        }

        // 3. Обработка остальных команд через простые if-else
        if (action.equals("addRoom")) {
            String roomId = parts[1];
            String name = parts[2];
            rooms.add(new Room(roomId, name));
            System.out.println("Добавлена комната: " + name);

        } else if (action.equals("addDevice")) {
            String deviceId = parts[1];
            String type = parts[2];
            String roomId = parts[3];

            // Ищем комнату, в которую нужно добавить устройство
            Room targetRoom = null;
            for (Room r : rooms) {
                if (r.id.equals(roomId)) {
                    targetRoom = r;
                    break;
                }
            }

            if (targetRoom != null) {
                Device newDevice;
                if (type.equals("lamp")) {
                    newDevice = new Lamp(deviceId);
                } else if (type.equals("thermostat")) {
                    newDevice = new Thermostat(deviceId);
                } else {
                    System.out.println("Ошибка: Неизвестный тип устройства " + type);
                    return;
                }

                targetRoom.devices.add(newDevice);
                allDevices.add(newDevice);
                System.out.println("Устройство " + deviceId + " добавлено в " + targetRoom.name);
            } else {
                System.out.println("Ошибка: Комната " + roomId + " не найдена.");
            }

        } else if (action.equals("turnOn")) {
            String deviceId = parts[1];
            Device d = findDevice(deviceId);
            if (d != null) {
                d.isOn = true;
                System.out.println("Устройство " + deviceId + " включено.");
            }

        } else if (action.equals("turnOff")) {
            String deviceId = parts[1];
            Device d = findDevice(deviceId);
            if (d != null) {
                d.isOn = false;
                System.out.println("Устройство " + deviceId + " выключено.");
            }

        } else if (action.equals("setTemperature")) {
            String deviceId = parts[1];
            int value = Integer.parseInt(parts[2]);

            Device d = findDevice(deviceId);
            if (d != null) {
                // Проверяем: является ли устройство термостатом?
                if (d instanceof Thermostat) {
                    Thermostat t = (Thermostat) d; // Приводим тип
                    t.temperature = value;
                    System.out.println("Для " + deviceId + " задана температура " + value);
                } else {
                    System.out.println("Ошибка: устройство " + deviceId + " не является термостатом!");
                }
            }

        } else if (action.equals("print")) {
            System.out.println("\n--- СОСТОЯНИЕ ДОМА ---");
            for (Room r : rooms) {
                System.out.println("Комната: " + r.name);
                for (Device d : r.devices) {
                    String status = d.isOn ? "ВКЛ" : "ВЫКЛ";
                    if (d instanceof Thermostat) {
                        Thermostat t = (Thermostat) d;
                        System.out.println("  - [" + d.type + "] " + d.id + " (" + status + ", " + t.temperature + "°C)");
                    } else {
                        System.out.println("  - [" + d.type + "] " + d.id + " (" + status + ")");
                    }
                }
            }
            System.out.println("----------------------\n");
        } else {
            System.out.println("Неизвестная команда: " + action);
        }
    }

    // Вспомогательный метод для поиска устройства по ID
    private Device findDevice(String id) {
        for (Device d : allDevices) {
            if (d.id.equals(id)) {
                return d;
            }
        }
        System.out.println("Ошибка: Устройство " + id + " не найдено.");
        return null;
    }
}

// 4. ОТДЕЛЬНЫЙ КЛАСС ДЛЯ ЗАПУСКА МНОЖЕСТВА КОМАНД (По требованию №3)
class CommandSequenceExecutor {
    SmartHomeController controller = new SmartHomeController();

    public void run(String[] commands) {
        for (String cmd : commands) {
            System.out.println("> " + cmd);
            controller.executeCommand(cmd);
        }
    }
}

// ТОЧКА ВХОДА
class Main {
    public static void main(String[] args) {
        CommandSequenceExecutor executor = new CommandSequenceExecutor();

        // Набор команд прямо из вашего примера
        String[] scenario = {
            "initHome                           // инициализируем дом",
            "addRoom room1 livingRoom           // добавляем гостиную",
            "addRoom room2 kitchen              // добавляем кухню",
            "addDevice light1 lamp room1        // ставим лампу в гостиную",
            "addDevice thermo1 thermostat room1 // ставим термостат в гостиную",
            "addDevice light2 lamp room2        // ставим лампу на кухню",
            "turnOn light1                      // включаем лампу в гостиной",
            "setTemperature thermo1 22          // задаём 22°C термостату",
            "setTemperature light1 22           // ошибка: лампа не термостат",
            "turnOff light2                     // выключаем лампу на кухне",
            "print                              // выводим состояние дома"
        };

        executor.run(scenario);
    }
}