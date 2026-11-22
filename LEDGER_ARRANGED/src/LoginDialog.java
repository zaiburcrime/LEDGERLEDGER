import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /** ArrayList of all accounts, loaded from users.txt. */
    static final ArrayList<User> users =
            new ArrayList<User>();

    /** users.txt lives in the home folder, so it works no matter where we start. */
    private static File userFile() {
        return new File(System.getProperty("user.home"), "ledger-users.txt");
    }

    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private boolean loggedIn = false;
    private boolean accountStorageAvailable = true;
    private String username = "";

    public LoginDialog() {
        setTitle("Ledger - Log in");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(420, 390);
        setLocationRelativeTo(null);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);

        // Simple colored header. Looks like a school project, not a website.
        JPanel top = new JPanel();
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        top.setBackground(new Color(108, 78, 214));
        top.setBorder(new EmptyBorder(18, 20, 18, 20));
        JLabel title = new JLabel("LEDGER");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Student financial tracker  |  2nd year IT");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(200, 220, 235));
        subtitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        top.add(title);
        top.add(subtitle);
        contentPane.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 22, 16, 22));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 0, 2, 0);

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        userLabel.setForeground(new Color(40, 50, 65));
        form.add(userLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        usernameField = new JTextField();
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(320, 32));
        usernameField.setForeground(new Color(28, 32, 40));
        form.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 2, 0);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLabel.setForeground(new Color(40, 50, 65));
        form.add(passLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 4, 0);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(320, 32));
        passwordField.setForeground(new Color(28, 32, 40));
        form.add(passwordField, gbc);

        gbc.gridy = 4;
        JCheckBox showPass = new JCheckBox("Show password");
        showPass.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showPass.setOpaque(false);
        showPass.setForeground(new Color(40, 50, 65));
        showPass.addActionListener(e -> {
            passwordField.setEchoChar(showPass.isSelected() ? (char) 0 : '\u2022');
        });
        form.add(showPass, gbc);

        gbc.gridy = 5;
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(170, 30, 30));
        form.add(messageLabel, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(8, 0, 4, 0);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        JButton loginButton = new JButton("Log in");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginButton.setBackground(new Color(108, 78, 214));
        loginButton.setForeground(Color.WHITE);
        loginButton.setOpaque(true);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tryLogin();
            }
        });
        JButton createButton = new JButton("Create account");
        createButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        createButton.setForeground(new Color(28, 32, 40));
        createButton.setFocusPainted(false);
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createAccount();
            }
        });
        buttons.add(loginButton);
        buttons.add(createButton);
        form.add(buttons, gbc);
        getRootPane().setDefaultButton(loginButton);

        gbc.gridy = 7;
        JLabel hint = new JLabel("Try the demo account:  student / 1234");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(new Color(60, 70, 85));
        form.add(hint, gbc);

        contentPane.add(form, BorderLayout.CENTER);
        loadUsers();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        return username;
    }

    // ------------------------------------------------------------------
    // Logging in
    // ------------------------------------------------------------------

    private void tryLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.length() == 0 || pass.length() == 0) {
            showMessage("Please type your username and password.");
            return;
        }

        // Linear search through the ArrayList of users.
        for (int i = 0; i < users.size(); i++) {
            User account = users.get(i);
            if (account.username.equalsIgnoreCase(user) && account.verify(pass)) {
                loggedIn = true;
                username = account.username;
                dispose();
                return;
            }
        }

        showMessage("Wrong username or password.");
    }

    private void createAccount() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (!accountStorageAvailable) {
            showMessage("Account file unavailable. Please restart Ledger.");
            return;
        }
        if (!user.matches("[A-Za-z0-9_.-]{3,32}")) {
            showMessage("Use 3-32 letters, numbers, dots, _ or -.");
            return;
        }
        if (pass.length() < 4 || !pass.equals(pass.trim()) || pass.contains("\n") || pass.contains("\r")) {
            showMessage("Use 4+ characters without outer spaces.");
            return;
        }
        if (findUser(user) != null) {
            showMessage("That username is already taken.");
            return;
        }

        users.add(new User(user, pass));
        if (!saveUsers()) {
            users.remove(users.size() - 1);
            return;
        }

        messageLabel.setForeground(new Color(21, 128, 61));
        messageLabel.setText("Account created. You can log in now.");
    }

    private User findUser(String name) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).username.equalsIgnoreCase(name)) {
                return users.get(i);
            }
        }
        return null;
    }

    private void showMessage(String text) {
        messageLabel.setForeground(new Color(185, 28, 28));
        messageLabel.setText(text);
    }

    // ------------------------------------------------------------------
    // users.txt (one "username;password" per line)
    // ------------------------------------------------------------------

    private void loadUsers() {
        users.clear();

        File file = userFile();
        if (!file.exists()) {
            // First run: create two demo accounts.
            users.add(new User("student", "1234"));
            users.add(new User("admin", "admin"));
            saveUsers();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            while (line != null) {
                int split = line.indexOf(';');
                if (split > 0) {
                    String name = line.substring(0, split).trim();
                    String pass = line.substring(split + 1).trim();
                    if (name.length() > 0) {
                        users.add(new User(name, pass));
                    }
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            users.clear();
            accountStorageAvailable = false;
            showMessage("Could not read users.txt");
        }
    }

    private boolean saveUsers() {
        Path temp = null;
        try {
            Path file = userFile().toPath();
            temp = Files.createTempFile(file.toAbsolutePath().getParent(), "ledger-users-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (User account : users) {
                    writer.write(account.username + ";" + account.password + "\n");
                }
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            showMessage("Could not save users.txt");
            return false;
        } finally {
            if (temp != null) try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
        }
    }

    public static class User {
        String username;
        String password;

        User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        /** Previous editions stored PBKDF2 hashes; do not mistake a hash for a password. */
        boolean verify(String candidate) {
            if (!password.startsWith("pbkdf2$")) return password.equals(candidate);
            try {
                String[] parts = password.split("\\$", -1);
                if (parts.length != 4) return false;
                int rounds = Integer.parseInt(parts[1]);
                if (rounds < 100000 || rounds > 2000000) return false;
                byte[] salt = java.util.Base64.getDecoder().decode(parts[2]);
                byte[] expected = java.util.Base64.getDecoder().decode(parts[3]);
                if (salt.length < 8 || expected.length != 32) return false;
                javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(candidate.toCharArray(), salt, rounds, 256);
                try {
                    byte[] actual = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
                    return java.security.MessageDigest.isEqual(expected, actual);
                } finally { spec.clearPassword(); }
            } catch (java.security.GeneralSecurityException | RuntimeException e) { return false; }
        }
    }

}
