package session;

public class Session {
    private static int loggedInUserId;
    private static int loggedInClientId;

    public static void setLoggedInUserId(int userId) {
        loggedInUserId = userId;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInClientId(int clientId) {
        loggedInClientId = clientId;
    }

    public static int getLoggedInClientId() {
        return loggedInClientId;
    }
}
