package service;

import model.User;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final List<User> users = new ArrayList<>();
    private int nextUserId = 1;

    public User registerUser(String name, String email, String password, String role) {
        if (findByEmail(email) != null) {
            return null; // Email already exists
        }
        User user = new User(nextUserId++, name, email, password, role.toUpperCase());
        users.add(user);
        return user;
    }

    public User loginUser(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public User findById(int id) {
        for (User u : users) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}
