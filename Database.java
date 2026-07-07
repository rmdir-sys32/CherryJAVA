import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    public RespValue executeCommand(String commandName, List<RespValue> args) {
        switch (commandName.toUpperCase()) {
            case "PING":
                if (args.size() > 1) {
                    return new RespValue(Type.BULK_STRING, args.get(1).value);
                }
                return new RespValue(Type.SIMPLE_STRING, "PONG");
            case "ECHO":
                if (args.size() < 2) {
                    return new RespValue(Type.ERROR,
                            "ERR wrong number of argumnets for 'echo' command");
                }
                return new RespValue(Type.BULK_STRING, args.get(1).value);
            case "SET":
                if (args.size() < 3) {
                    return new RespValue(Type.ERROR,
                            "ERR wrong number of argumnets for 'GET' command");
                }
                String setKey = (String) args.get(1).value;
                String setValue = (String) args.get(2).value;

                store.put(setKey, setValue);

                return new RespValue(Type.SIMPLE_STRING, "OK");

            case "GET":
                if (args.size() < 2) {
                    return new RespValue(Type.ERROR,
                            "ERR wrong number of argumnets for 'GET' command");
                }
                String getKey = (String) args.get(1).value;
                String getValue = store.get(getKey);
                return new RespValue(Type.BULK_STRING, getValue);

            case "DEL":
                if (args.size() < 2) {
                    return new RespValue(Type.ERROR, "ERR wrong number of arguments for DEL");
                }

                String delKey = (String) args.get(1).value;
                store.remove(delKey);

                return new RespValue(Type.SIMPLE_STRING, "Value at: " + delKey + " deleted.");
            case "EXISTS":
                if (args.size() < 2) {
                    return new RespValue(Type.ERROR, "ERR wrong number of arguments for EXISTS");
                }

                String existsKey = (String) args.get(1).value;
                boolean exist = store.containsKey(existsKey) ? true : false;
                return new RespValue(Type.SIMPLE_STRING, exist ? "TRUE" : "FALSE");
            default:
                return new RespValue(Type.ERROR, "ERR unknown command '" + commandName + "'");
        }
    }
}
