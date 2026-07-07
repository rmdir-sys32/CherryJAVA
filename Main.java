public class Main {
    public static void main(String args[]) {
        int port = 6379;
        RedisServer server = new RedisServer(port);
        server.start();
    }
}
