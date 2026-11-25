package util;

import model.User;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static User currentUser;
    private static List<String> permissions = new ArrayList<>();

    public static void setSession(User user, List<String> userPermissions) {
        currentUser = user;
        permissions = userPermissions;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean hasPermission(String permissionName) {
        return permissions != null && permissions.contains(permissionName);
    }

    public static void clearSession() {
        currentUser = null;
        permissions.clear();
    }
}