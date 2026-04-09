package sits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sits.remote.RegistrationRequest;

// Tests for the small data object sent when a remote player registers.
class RegistrationRequestTest {

    @Test
    void defaultConstructorAndSettersWork() {
        // Checks that the empty constructor plus setters store the values correctly.
        RegistrationRequest request = new RegistrationRequest();
        request.setName("RobotC");
        request.setIp("10.0.0.9");
        request.setPort(8089);

        assertEquals("RobotC", request.getName());
        assertEquals("10.0.0.9", request.getIp());
        assertEquals(8089, request.getPort());
    }

    @Test
    void storesRegistrationDetails() {
        // Checks that the full constructor and later updates both work as expected.
        RegistrationRequest request = new RegistrationRequest("RobotA", "127.0.0.1", 8081);

        assertEquals("RobotA", request.getName());
        assertEquals("127.0.0.1", request.getIp());
        assertEquals(8081, request.getPort());

        request.setName("RobotB");
        request.setIp("192.168.1.10");
        request.setPort(9000);

        assertEquals("RobotB", request.getName());
        assertEquals("192.168.1.10", request.getIp());
        assertEquals(9000, request.getPort());
    }
}
