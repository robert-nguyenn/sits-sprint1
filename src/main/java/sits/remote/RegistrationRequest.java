// RegistrationRequest is just the data object sent when a remote player registers.
// It contains name, IP, and port.
// TournamentRegistry is the server’s in-memory storage of tournaments.
// It keeps track of tournaments currently known to the server.
// So one is request data, the other is server storage.


package sits.remote;

public class RegistrationRequest {

    private String name;
    private String ip;
    private int port;

    public RegistrationRequest() {
    }

    public RegistrationRequest(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
