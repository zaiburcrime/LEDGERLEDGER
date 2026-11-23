import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

/**
- Ledger - a simple student financial tracker.
 *
- <p>This program was written for a Data Structures class, so the data is kept in
- real data structures instead of plain variables. The three used here are:</p>
 *
- <ul>
-   <li><b>ArrayList</b> - the list of expenses, the list of goals, the list of
-       categories and the list of user accounts. It works like an array that can
-       grow and shrink.</li>
-   <li><b>HashMap</b> - the planned budget. The key is the category name and the
-       value is the amount planned for that category, so we can look a category
-       up in one step instead of searching the whole list.</li>
-   <li><b>Stack</b> - the recently deleted expenses. When you delete a row it is
-       pushed on the stack, and "Undo last delete" pops it back. Last in, first out.</li>
- </ul>
 *
- <p>Sorting is a bubble sort in {@link Expense#bubbleSort(java.util.ArrayList, int)}.
- Search is a linear check in {@link Expense#matches(Expense, String)}.</p>
 *
- <p>The screens are built in the same style Eclipse WindowBuilder uses: the frame
- has a {@code contentPane}, the components are fields, and the UI is assembled in
- {@code createContents()} and the {@code create...Panel()} methods.</p>
 *
- <p>Log in with the demo account <b>student / 1234</b> (or create your own account
- on the login window).</p>
 */
public class FinancialTracker extends JFrame {

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------
    // Colors and fonts used by the window (unchanged palette)
    // ------------------------------------------------------------------

    // Dark colors are the default so the window opens like the sample dashboard.
    // Light mode swaps these in toggleDarkMode().
    private static Color BG = new Color(232, 236, 242);
    private static Color CARD = Color.WHITE;
    private static Color INK = new Color(28, 32, 40);
    private static Color MUTED = new Color(70, 80, 95);
    private static final Color ACCENT = new Color(124, 92, 255);
    private static final Color GREEN = new Color(52, 199, 89);
    private static final Color RED = new Color(255, 99, 99);
    private static final Color BLUE = new Color(120, 170, 255);
    private static final Color SIDE = new Color(108, 78, 214);
    private static final Color SIDE_ON = new Color(88, 62, 190);
    private boolean darkMode = false;
    private static Color BORDER = new Color(190, 198, 210);

    // SansSerif is on every school PC. Helvetica is not.
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 21);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font BIG_FONT = new Font("SansSerif", Font.BOLD, 23);

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00",
            java.text.DecimalFormatSymbols.getInstance(Locale.US));
    private static final DateTimeFormatter GOAL_DATE = DateTimeFormatter
            .ofPattern("MM/dd/uuuu", Locale.US)
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter
            .ofPattern("MM/uuuu", Locale.US)
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);

    public static final String[] DEFAULT_CATEGORIES = {
        "Food", "Transport", "School", "Load", "Other"
    };

    // ------------------------------------------------------------------
    // THE DATA (data structures part)
    // ------------------------------------------------------------------

    /** ArrayList of all expenses. Each row of the table is one Expense object. */
    private final LedgerData data = new LedgerData();
    private final ArrayList<Expense> expenses = data.expenses;

    /** ArrayList of all savings goals. */
    private final ArrayList<Goal> goals = data.goals;

    /** ArrayList of category names. Starts with the six defaults, can grow. */
    private final ArrayList<String> categories = data.categories;

    /** Each month has its own category budget HashMap. */
    private HashMap<String, Double> planned() {
        return data.plan(YearMonth.now());
    }

    /** Stack of deleted expenses, used by "Undo last delete". */
    private final Stack<Expense> deleted = data.deleted;

    /** The expenses currently shown in the table, in table order. */
    private final ArrayList<Expense> shownExpenses = new ArrayList<Expense>();

    private String username = "";
    private Path dataFile;
    private boolean unsaved;

    // ------------------------------------------------------------------
    // COMPONENTS (declared as fields, the way WindowBuilder does it)
    // ------------------------------------------------------------------

    private JPanel contentPane;
    private JPanel sidebar;
    private JPanel contentArea;
    private java.awt.CardLayout cardLayout;
    private JButton[] navButtons;
    private JLabel pageTitleLabel;
    private JLabel pageSubtitleLabel;

    // Dashboard
    private JLabel allowanceValueLabel;
    private JLabel spentValueLabel;
    private JLabel leftValueLabel;
    private JLabel perDayValueLabel;
    private JTextArea summaryArea;
    private JComboBox<String> dashboardMonthBox;
    private YearMonth dashboardMonth;
    private boolean fillingDashboardMonths;

    // Expenses
    private JTextField descriptionField;
    private JTextField amountField;
    private JComboBox<String> categoryBox;
    private JSpinner dateSpinner;
    private LocalDate dateFloor = LocalDate.now();
    private YearMonth dateMonth = YearMonth.now();
    private javax.swing.Timer dateRefreshTimer;
    private JButton addExpenseButton;
    private JButton saveExpenseButton;
    private JButton cancelExpenseButton;
    private JTextField searchField;
    private JComboBox<String> sortBox;
    private JTable expenseTable;
    private DefaultTableModel expenseTableModel;
    private JLabel expenseCountLabel;
    private JButton undoButton;

    /** Keep the object, not a table index: sorting/filtering can move rows. */
    private Expense editingExpense;
    private YearMonth expenseMonth = YearMonth.now();
    private YearMonth activeMonth = YearMonth.now();
    private LocalDate expenseFrom;
    private LocalDate expenseTo;
    private String expenseCategory;
    private JLabel filterInfo;

    // Goals
    private JTextField goalNameField;
    private JTextField goalTargetField;
    private JTextField goalDeadlineField;
    private JButton addGoalButton;
    private JButton saveGoalButton;
    private JButton cancelGoalButton;
    private JTable goalTable;
    private DefaultTableModel goalTableModel;
    private JTextField depositField;
    private JLabel goalInfoLabel;
    private int editingGoal = -1;

    // Allowance / budget
    private JTextField allowanceField;
    private JLabel allowanceMessageLabel;
    private JComboBox<String> planCategoryBox;
    private JTextField planAmountField;
    private JTable planTable;
    private DefaultTableModel planTableModel;
    private JLabel monthInfoLabel;
    private JLabel totalBudgetValueLabel;
    private JLabel budgetBreakdownLabel;

    // Reports (the graph)
    private JPanel categoryBarsPanel;
    private JPanel dailyBarsPanel;
    private JLabel reportSummaryLabel;
    private JComboBox<String> reportMonthBox;
    private boolean fillingReportMonths;

    // ------------------------------------------------------------------
    // Start of the program
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Cross-platform look so our own colors actually show.
                    // Nimbus was painting over the text and numbers.
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    UIManager.put("Label.foreground", INK);
                    UIManager.put("TextField.foreground", INK);
                    UIManager.put("TextArea.foreground", INK);
                    UIManager.put("ComboBox.foreground", INK);
                } catch (Exception e) {
                    // If this fails we just keep the default look.
                }

                LoginDialog login = new LoginDialog();
                login.setVisible(true);

                if (login.isLoggedIn()) {
                    openTracker(login.getUsername());
                } else {
                    System.exit(0);
                }
            }
        });
    }

    /** Builds the main window for the user who just logged in. */
    public FinancialTracker(String username) {
        this.username = username;
        dataFile = dataPath(Paths.get(System.getProperty("user.home")), username);
        try {
            data.load(dataFile, username);
            if (data.loadedLegacyEdition) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "Your saved records are from an earlier Ledger edition.\n"
                        + "Import them into this version? An exact backup will be made first.\n\n"
                        + "Expenses, income, allowance, savings and budgets will be retained.\n"
                        + "Scheduled expenses become ordinary dated expenses; automatic repeats stop.\n"
                        + "Monthly balances use this version's rules (no automatic carry-over).\n"
                        + "The original undo history and settings remain in the backup.",
                        "Import previous Ledger data", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (choice != JOptionPane.OK_OPTION)
                    throw new IOException("Import cancelled. Your original file is unchanged.");
                Path backup = data.migrateLegacyEdition(dataFile);
                JOptionPane.showMessageDialog(null, "Previous records imported.\nOriginal file backed up to:\n" + backup,
                        "Ledger", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Could not open your Ledger data. No records were reset.\n"
                    + "Reason: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage())
                    + "\nFile: " + dataFile + "\nKeep this file and any backups; do not delete them.", e);
        }
        dashboardMonth = data.initialDashboardMonth(YearMonth.now());
        expenseMonth = dashboardMonth;

        setTitle("Ledger - Student Financial Tracker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (!unsaved || saveData()) dispose();
            }
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (dateRefreshTimer != null) dateRefreshTimer.stop();
            }
        });
        setBounds(60, 40, 1040, 740);
        setMinimumSize(new Dimension(920, 640));

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setBackground(BG);
        setContentPane(contentPane);

        createContents();
        // If the tracker stays open overnight, advance the minimum date automatically.
        dateRefreshTimer = new javax.swing.Timer(30000, e -> {
            if (!YearMonth.now().equals(dateMonth) || !LocalDate.now().equals(dateFloor)) {
                refreshDateFloor();
                refreshCurrentMonth();
                refreshAll();
            }
        });
        dateRefreshTimer.start();
        allowanceField.setText(MONEY.format(data.allowance(YearMonth.now())));
        refreshFilterLabel();
        rebuildExpenseTable();
        // Paint fields and labels with the dark colors before the window opens.
        applyTheme(this, BG, CARD);
        paintSidebar();
        refreshAll();
    }

    /** Short line under the page title. Basic text, not a real i18n file. */
    private static String pageBlurb(String page) {
        if ("Expenses".equals(page)) return "Add and search what you spent";
        if ("Allowance".equals(page)) return "Set this month's money and budget";
        if ("Savings Goals".equals(page)) return "Track what you are saving for";
        if ("Reports".equals(page)) return "See spending by category";
        return "Summary of your money";
    }

    private static void openTracker(String username) {
        try {
            new FinancialTracker(username).setVisible(true);
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Ledger", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Stable, safe and unique filename for each case-insensitive login. */
    static Path dataPath(Path home, String user) {
        StringBuilder key = new StringBuilder();
        for (byte b : user.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8))
            key.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        return home.resolve("LedgerData").resolve(key + ".properties");
    }

    private boolean saveData() {
        try {
            data.save(dataFile);
            unsaved = false;
            return true;
        } catch (IOException | RuntimeException e) {
            unsaved = true;
            showError("Could not save financial records. Keep Ledger open and retry. " + e.getMessage());
            return false;
        }
    }

    private boolean changed() {
        unsaved = true;
        boolean saved = saveData();
        refreshAll();
        return saved;
    }

    /** Full reset affects only the signed-in user; a backup is required first. */
    private void resetAllData() {
        String answer = JOptionPane.showInputDialog(this,
                "Reset ALL financial data for " + username + " across every month?\n"
                + "Expenses, goals, income, allowance, plans, custom categories and undo history will be cleared.\n"
                + "Your login and other accounts will not change. A backup will be saved first.\n\n"
                + "Type RESET to confirm:", "Reset all data", JOptionPane.WARNING_MESSAGE);
        if (!"RESET".equals(answer)) return;
        try {
            Path backup = data.backupAndReset(dataFile);
            unsaved = false;
            dashboardMonth = YearMonth.now();
            activeMonth = dashboardMonth;
            stopEditingExpense();
            stopEditingGoal();
            depositField.setText("");
            allowanceField.setText("0.00");
            allowanceMessageLabel.setText("Financial data reset.");
            planAmountField.setText("");
            fillCategoryBox();
            revealExpenseMonth(LocalDate.now());
            sortBox.setSelectedIndex(0);
            rebuildExpenseTable();
            refreshAll();
            reportMonthBox.setSelectedItem(dashboardMonth.format(MONTH_FORMAT));
            JOptionPane.showMessageDialog(this, "Financial data reset successfully.\nBackup: " + backup,
                    "Reset complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | RuntimeException e) {
            rebuildExpenseTable();
            refreshAll();
            showError("Reset could not be completed. Your financial data has not been intentionally discarded. "
                    + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Modern GUI helpers (borders, pills, inputs)
    // ------------------------------------------------------------------

    private static class RoundedBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(4, 8, 4, 8);
            return insets;
        }
    }

    private JPanel createCardContainer(int padV, int padH) {
        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, 12),
                new EmptyBorder(padV, padH, padV, padH)));
        return card;
    }

    private void styleModernButton(JButton btn, boolean primary) {
        btn.setFont(primary ? BOLD_FONT : LABEL_FONT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        if (primary) {
            btn.setBackground(ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new RoundedBorder(ACCENT, 8),
                    new EmptyBorder(6, 14, 6, 14)));
        } else {
            Color bg = darkMode ? new Color(70, 74, 84) : new Color(240, 243, 248);
            btn.setBackground(bg);
            btn.setForeground(INK);
            btn.setBorder(new CompoundBorder(
                    new RoundedBorder(BORDER, 8),
                    new EmptyBorder(5, 12, 5, 12)));
        }
    }

    private void styleModernField(JTextComponent field, Dimension prefSize) {
        if (prefSize != null) {
            field.setPreferredSize(prefSize);
        }
        field.setFont(LABEL_FONT);
        field.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, 8),
                new EmptyBorder(3, 6, 3, 6)));
    }

    // ------------------------------------------------------------------
    // Putting the window together
    // ------------------------------------------------------------------

    private void createContents() {
        contentPane.add(createHeader(), BorderLayout.NORTH);

        // Sidebar + content area (fills all space)
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setOpaque(false);

        sidebar = createSidebar();
        main.add(sidebar, BorderLayout.WEST);

        cardLayout = new java.awt.CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);
        contentArea.add(createDashboardPanel(), "Dashboard");
        contentArea.add(createExpensesPanel(), "Expenses");
        contentArea.add(createAllowancePanel(), "Allowance");
        contentArea.add(createGoalsPanel(), "Savings Goals");
        contentArea.add(createReportsPanel(), "Reports");
        main.add(contentArea, BorderLayout.CENTER);

        contentPane.add(main, BorderLayout.CENTER);
        contentPane.add(createStatusBar(), BorderLayout.SOUTH);

        showPage("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(SIDE);
        side.setPreferredSize(new Dimension(205, 0));
        side.setBorder(new EmptyBorder(20, 12, 20, 12));

        JLabel badge = new JLabel("WORKSPACE");
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setForeground(new Color(220, 212, 255));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.setBorder(new EmptyBorder(0, 12, 4, 8));
        side.add(badge);

        JLabel title = new JLabel("Ledger");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 12, 2, 8));
        side.add(title);

        JLabel course = new JLabel("Student money tracker");
        course.setFont(SMALL_FONT);
        course.setForeground(new Color(230, 224, 255));
        course.setAlignmentX(Component.LEFT_ALIGNMENT);
        course.setBorder(new EmptyBorder(0, 12, 20, 8));
        side.add(course);

        String[] pages = {"Dashboard", "Expenses", "Allowance", "Savings Goals", "Reports"};
        navButtons = new JButton[pages.length];

        for (int i = 0; i < pages.length; i++) {
            final String page = pages[i];
            JButton btn = new JButton(page);
            btn.putClientProperty("page", page);
            btn.setFont(BOLD_FONT);
            btn.setForeground(Color.WHITE);
            btn.setBackground(SIDE);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBorderPainted(true);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setBorder(new CompoundBorder(
                    new RoundedBorder(new Color(145, 122, 230), 10),
                    new EmptyBorder(8, 14, 8, 14)));
            btn.addActionListener(e -> showPage(page));
            navButtons[i] = btn;
            side.add(btn);
            side.add(Box.createVerticalStrut(6));
        }

        side.add(Box.createVerticalGlue());

        JLabel footerHint = new JLabel("Data Structures Edition");
        footerHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerHint.setForeground(new Color(220, 212, 255));
        footerHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        footerHint.setBorder(new EmptyBorder(8, 12, 0, 8));
        side.add(footerHint);

        return side;
    }

    private void showPage(String name) {
        cardLayout.show(contentArea, name);
        for (JButton btn : navButtons) {
            boolean active = name.equals(btn.getClientProperty("page"));
            btn.setBackground(active ? SIDE_ON : SIDE);
            btn.setBorder(new CompoundBorder(
                    new RoundedBorder(active ? Color.WHITE : new Color(145, 122, 230), 10),
                    new EmptyBorder(8, 14, 8, 14)));
            btn.setForeground(Color.WHITE);
        }
        if (pageTitleLabel != null) pageTitleLabel.setText(name);
        if (pageSubtitleLabel != null) pageSubtitleLabel.setText(pageBlurb(name));
        refreshDateFloor();
        refreshCurrentMonth();
        refreshAll();
    }

    private void refreshCurrentMonth() {
        YearMonth now = YearMonth.now();
        if (now.equals(activeMonth)) return;
        if (activeMonth.equals(expenseMonth)) {
            expenseMonth = now;
            refreshFilterLabel();
        }
        if (activeMonth.equals(dashboardMonth)) dashboardMonth = now;
        activeMonth = now;
        allowanceField.setText(MONEY.format(data.allowance(now)));
        rebuildExpenseTable();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout(12, 0));
        header.setBackground(CARD);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(8, 18, 8, 18)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel logo = new JLabel("Dashboard");
        logo.setFont(TITLE_FONT);
        logo.setForeground(INK);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pageTitleLabel = logo;

        JLabel subtitle = new JLabel("Summary of your money");
        subtitle.setFont(SMALL_FONT);
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pageSubtitleLabel = subtitle;

        left.add(logo);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        JPanel right = new JPanel();
        right.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        right.setOpaque(false);

        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pill.setBackground(BG);
        pill.setBorder(new RoundedBorder(BORDER, 14));
        JLabel userLabel = new JLabel("Signed in: " + username);
        userLabel.setFont(SMALL_FONT);
        userLabel.setForeground(INK);
        pill.add(userLabel);

        JButton logoutButton = new JButton("Log out");
        styleModernButton(logoutButton, false);
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logOut();
            }
        });

        right.add(pill);
        right.add(logoutButton);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JLabel hint = new JLabel("  ArrayList (records)  •  HashMap (monthly budget)  •  "
                + "Stack (undo)  •  Automatic local file saving");
        hint.setFont(SMALL_FONT);
        hint.setForeground(MUTED);
        hint.setBorder(new EmptyBorder(7, 10, 7, 10));

        bar.add(hint, BorderLayout.WEST);
        return bar;
    }

    // ------------------------------------------------------------------
    // Dashboard tab
    // ------------------------------------------------------------------

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);

        allowanceValueLabel = new JLabel("₱0.00");
        spentValueLabel = new JLabel("₱0.00");
        leftValueLabel = new JLabel("₱0.00");
        perDayValueLabel = new JLabel("₱0.00");

        stats.add(createStatCard("MONTHLY ALLOWANCE", allowanceValueLabel, ACCENT));
        stats.add(createStatCard("TOTAL SPENT", spentValueLabel, RED));
        stats.add(createStatCard("MONEY LEFT", leftValueLabel, GREEN));
        stats.add(createStatCard("LEFT PER DAY", perDayValueLabel, new Color(2, 132, 199)));

        panel.add(stats, BorderLayout.NORTH);

        JPanel middle = createCardContainer(14, 16);
        middle.setLayout(new BorderLayout(0, 10));

        JLabel summaryTitle = new JLabel("Summary of your data");
        summaryTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryTitle.setForeground(INK);
        JPanel summaryHeader = new JPanel(new BorderLayout(8, 0));
        summaryHeader.setOpaque(false);
        summaryHeader.add(summaryTitle, BorderLayout.WEST);

        JPanel monthChooser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        monthChooser.setOpaque(false);
        monthChooser.add(smallLabel("SHOW MONTH"));
        dashboardMonthBox = new JComboBox<String>();
        dashboardMonthBox.setPreferredSize(new Dimension(115, 30));
        dashboardMonthBox.addActionListener(e -> {
            if (!fillingDashboardMonths && dashboardMonthBox.getSelectedItem() != null) {
                dashboardMonth = parseFilterMonth(dashboardMonthBox.getSelectedItem().toString());
                refreshDashboard();
            }
        });
        monthChooser.add(dashboardMonthBox);
        JButton resetAllButton = new JButton("Reset all data...");
        styleModernButton(resetAllButton, false);
        resetAllButton.addActionListener(e -> resetAllData());
        monthChooser.add(resetAllButton);
        summaryHeader.add(monthChooser, BorderLayout.EAST);

        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        summaryArea.setBackground(BG);
        summaryArea.setForeground(INK);
        summaryArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(summaryArea);
        scroll.setBorder(new RoundedBorder(BORDER, 10));

        middle.add(summaryHeader, BorderLayout.NORTH);
        middle.add(scroll, BorderLayout.CENTER);

        panel.add(middle, BorderLayout.CENTER);
        return panel;
    }

    /** One small card with a caption and a big number. */
    private JPanel createStatCard(String caption, JLabel valueLabel, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD);
        card.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, 12),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        captionLabel.setForeground(MUTED);

        valueLabel.setFont(BIG_FONT);
        valueLabel.setForeground(valueColor);
        valueLabel.putClientProperty("keepColor", Boolean.TRUE);

        card.add(captionLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------------
    // Expenses tab
    // ------------------------------------------------------------------

    private JPanel createExpensesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        panel.add(createExpenseForm(), BorderLayout.NORTH);
        panel.add(createExpenseList(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createExpenseForm() {
        JPanel form = createCardContainer(14, 16);
        form.setLayout(new GridBagLayout());

        descriptionField = new JTextField();
        styleModernField(descriptionField, new Dimension(235, 32));

        amountField = new JTextField();
        styleModernField(amountField, new Dimension(115, 32));

        categoryBox = new JComboBox<String>();
        categoryBox.setPreferredSize(new Dimension(145, 32));
        categoryBox.setFont(LABEL_FONT);
        fillCategoryBox();

        YearMonth currentMonth = YearMonth.now();
        dateSpinner = new JSpinner(expenseDateModel(LocalDate.now(), currentMonth));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
        ((JSpinner.DateEditor) dateSpinner.getEditor()).getFormat().setLenient(false);
        dateSpinner.setToolTipText("Select any date within the current month ("
                + currentMonth.atDay(1).format(GOAL_DATE) + " - "
                + currentMonth.atEndOfMonth().format(GOAL_DATE) + ").");
        dateSpinner.setPreferredSize(new Dimension(145, 32));
        dateSpinner.setFont(LABEL_FONT);
        dateSpinner.setBorder(new RoundedBorder(BORDER, 8));

        addExpenseButton = new JButton("Add expense");
        styleModernButton(addExpenseButton, true);
        addExpenseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addExpense();
            }
        });

        saveExpenseButton = new JButton("Save changes");
        styleModernButton(saveExpenseButton, true);
        saveExpenseButton.setVisible(false);
        saveExpenseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveExpenseChanges();
            }
        });

        cancelExpenseButton = new JButton("Cancel");
        styleModernButton(cancelExpenseButton, false);
        cancelExpenseButton.setVisible(false);
        cancelExpenseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopEditingExpense();
            }
        });

        JButton newCategoryButton = new JButton("New category...");
        styleModernButton(newCategoryButton, false);
        newCategoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewCategory();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 12);

        gbc.gridy = 0;
        gbc.gridx = 0;
        form.add(smallLabel("DESCRIPTION"), gbc);
        gbc.gridx = 1;
        form.add(smallLabel("AMOUNT"), gbc);
        gbc.gridx = 2;
        form.add(smallLabel("CATEGORY"), gbc);
        gbc.gridx = 3;
        form.add(smallLabel("DATE"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 12);
        gbc.gridx = 0;
        form.add(descriptionField, gbc);
        gbc.gridx = 1;
        form.add(amountField, gbc);
        gbc.gridx = 2;
        form.add(categoryBox, gbc);
        gbc.gridx = 3;
        form.add(dateSpinner, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(addExpenseButton);
        buttons.add(saveExpenseButton);
        buttons.add(cancelExpenseButton);
        JButton clearExpenseButton = new JButton("Clear");
        styleModernButton(clearExpenseButton, false);
        clearExpenseButton.addActionListener(e -> {
            stopEditingExpense();
            if (categoryBox.getItemCount() > 0) categoryBox.setSelectedIndex(0);
            descriptionField.requestFocusInWindow();
        });
        buttons.add(clearExpenseButton);
        buttons.add(newCategoryButton);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 0, 12);
        form.add(buttons, gbc);

        // An empty, stretchy cell on the right keeps the fields on the left.
        gbc.gridy = 1;
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        form.add(spacer, gbc);
        return form;
    }

    private JPanel createExpenseList() {
        JPanel panel = createCardContainer(14, 16);
        panel.setLayout(new BorderLayout(0, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        searchField = new JTextField();
        styleModernField(searchField, new Dimension(180, 30));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                rebuildExpenseTable();
            }

            public void removeUpdate(DocumentEvent e) {
                rebuildExpenseTable();
            }

            public void changedUpdate(DocumentEvent e) {
                rebuildExpenseTable();
            }
        });

        sortBox = new JComboBox<String>(new String[] {
            "Newest first", "Oldest first", "Amount: high to low",
            "Amount: low to high", "Description A-Z"
        });
        sortBox.setFont(LABEL_FONT);
        sortBox.setPreferredSize(new Dimension(170, 30));
        sortBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rebuildExpenseTable();
            }
        });

        JButton editButton = new JButton("Edit");
        styleModernButton(editButton, false);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startEditingExpense();
            }
        });

        JButton deleteButton = new JButton("Delete");
        styleModernButton(deleteButton, false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteExpense();
            }
        });

        undoButton = new JButton("Undo delete");
        styleModernButton(undoButton, false);
        undoButton.setEnabled(false);
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                undoDelete();
            }
        });

        expenseCountLabel = new JLabel("No expenses yet");
        expenseCountLabel.setFont(SMALL_FONT);
        expenseCountLabel.setForeground(MUTED);

        toolbar.add(smallLabel("SEARCH"));
        toolbar.add(searchField);
        toolbar.add(smallLabel("SORT BY"));
        toolbar.add(sortBox);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(undoButton);
        toolbar.add(expenseCountLabel);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        JButton filterButton = new JButton("Filters / history...");
        styleModernButton(filterButton, false);
        filterButton.addActionListener(e -> showFilters());
        filterInfo = smallLabel("Current month");
        filterRow.add(filterButton);
        JButton clearFilters = new JButton("Clear filters");
        styleModernButton(clearFilters, false);
        clearFilters.addActionListener(e -> {
            expenseMonth = YearMonth.now();
            expenseFrom = null;
            expenseTo = null;
            expenseCategory = null;
            searchField.setText("");
            sortBox.setSelectedIndex(0);
            refreshFilterLabel();
            rebuildExpenseTable();
        });
        filterRow.add(clearFilters);
        filterRow.add(filterInfo);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 6));
        top.setOpaque(false);
        top.add(toolbar);
        top.add(filterRow);

        expenseTableModel = new DefaultTableModel(new Object[][] {},
                new String[] {"Date", "Description", "Category", "Amount"}) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        expenseTable = new JTable(expenseTableModel);
        expenseTable.setFont(LABEL_FONT);
        expenseTable.setRowHeight(30);
        expenseTable.setShowGrid(true);
        expenseTable.setGridColor(BORDER);
        expenseTable.setFillsViewportHeight(true);
        expenseTable.getTableHeader().setFont(BOLD_FONT);
        expenseTable.getTableHeader().setPreferredSize(new Dimension(0, 32));
        expenseTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        expenseTable.getColumnModel().getColumn(3).setCellRenderer(right);
        expenseTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        expenseTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        expenseTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        expenseTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(expenseTable);
        scroll.setBorder(new RoundedBorder(BORDER, 10));

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------------
    // Savings goals tab
    // ------------------------------------------------------------------

    private JPanel createGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        panel.add(createGoalForm(), BorderLayout.NORTH);
        panel.add(createGoalList(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGoalForm() {
        JPanel form = createCardContainer(14, 16);
        form.setLayout(new GridBagLayout());

        goalNameField = new JTextField();
        styleModernField(goalNameField, new Dimension(225, 32));

        goalTargetField = new JTextField();
        styleModernField(goalTargetField, new Dimension(115, 32));

        goalDeadlineField = new JTextField();
        styleModernField(goalDeadlineField, new Dimension(125, 32));
        goalDeadlineField.setToolTipText("Enter MM/DD/YYYY, for example 05/31/2028.");

        addGoalButton = new JButton("Add goal");
        styleModernButton(addGoalButton, true);
        addGoalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addGoal();
            }
        });

        saveGoalButton = new JButton("Save changes");
        styleModernButton(saveGoalButton, true);
        saveGoalButton.setVisible(false);
        saveGoalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveGoalChanges();
            }
        });

        cancelGoalButton = new JButton("Cancel");
        styleModernButton(cancelGoalButton, false);
        cancelGoalButton.setVisible(false);
        cancelGoalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopEditingGoal();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 12);

        gbc.gridy = 0;
        gbc.gridx = 0;
        form.add(smallLabel("GOAL NAME"), gbc);
        gbc.gridx = 1;
        form.add(smallLabel("TARGET AMOUNT"), gbc);
        gbc.gridx = 2;
        form.add(smallLabel("SAVE BY (MM/DD/YYYY)"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 12);
        gbc.gridx = 0;
        form.add(goalNameField, gbc);
        gbc.gridx = 1;
        form.add(goalTargetField, gbc);
        gbc.gridx = 2;
        form.add(goalDeadlineField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(addGoalButton);
        buttons.add(saveGoalButton);
        buttons.add(cancelGoalButton);
        JButton clearGoalButton = new JButton("Clear");
        styleModernButton(clearGoalButton, false);
        clearGoalButton.addActionListener(e -> {
            stopEditingGoal();
            goalNameField.requestFocusInWindow();
        });
        buttons.add(clearGoalButton);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 0, 12);
        form.add(buttons, gbc);

        gbc.gridy = 1;
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        form.add(spacer, gbc);
        return form;
    }

    private JPanel createGoalList() {
        JPanel panel = createCardContainer(14, 16);
        panel.setLayout(new BorderLayout(0, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        depositField = new JTextField();
        styleModernField(depositField, new Dimension(115, 30));

        JButton depositButton = new JButton("Add Savings");
        styleModernButton(depositButton, true);
        depositButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                depositToGoal();
            }
        });

        JButton editButton = new JButton("Edit");
        styleModernButton(editButton, false);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startEditingGoal();
            }
        });

        JButton deleteButton = new JButton("Delete");
        styleModernButton(deleteButton, false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteGoal();
            }
        });
        JButton adviceButton = new JButton("Savings advice");
        styleModernButton(adviceButton, false);
        adviceButton.addActionListener(e -> showSavingsAdvice());

        goalInfoLabel = new JLabel("Select a goal to add money to it");
        goalInfoLabel.setFont(SMALL_FONT);
        goalInfoLabel.setForeground(MUTED);

        toolbar.add(depositField);
        toolbar.add(depositButton);
        JButton clearDepositButton = new JButton("Clear amount");
        styleModernButton(clearDepositButton, false);
        clearDepositButton.addActionListener(e -> depositField.setText(""));
        toolbar.add(clearDepositButton);

        JPanel goalActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        goalActions.setOpaque(false);
        goalActions.add(editButton);
        goalActions.add(deleteButton);
        JButton resetGoalButton = new JButton("Reset progress...");
        styleModernButton(resetGoalButton, false);
        resetGoalButton.addActionListener(e -> resetGoalProgress());
        goalActions.add(resetGoalButton);
        goalActions.add(adviceButton);
        goalActions.add(goalInfoLabel);

        goalTableModel = new DefaultTableModel(new Object[][] {},
                new String[] {"Goal", "Target", "Saved", "Progress", "Save by"}) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        goalTable = new JTable(goalTableModel);
        goalTable.setFont(LABEL_FONT);
        goalTable.setRowHeight(30);
        goalTable.setShowGrid(true);
        goalTable.setGridColor(BORDER);
        goalTable.setFillsViewportHeight(true);
        goalTable.getTableHeader().setFont(BOLD_FONT);
        goalTable.getTableHeader().setPreferredSize(new Dimension(0, 32));
        goalTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        goalTable.getColumnModel().getColumn(1).setCellRenderer(right);
        goalTable.getColumnModel().getColumn(2).setCellRenderer(right);
        goalTable.getColumnModel().getColumn(3).setCellRenderer(right);
        goalTable.getColumnModel().getColumn(0).setPreferredWidth(240);
        goalTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane scroll = new JScrollPane(goalTable);
        scroll.setBorder(new RoundedBorder(BORDER, 10));

        JPanel goalTools = new JPanel(new GridLayout(2, 1, 0, 6));
        goalTools.setOpaque(false);
        goalTools.add(toolbar);
        goalTools.add(goalActions);
        panel.add(goalTools, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------------
    // Allowance tab
    // ------------------------------------------------------------------

    private JPanel createAllowancePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel form = createCardContainer(14, 16);
        form.setLayout(new GridBagLayout());

        allowanceField = new JTextField();
        allowanceField.addActionListener(e -> saveAllowance());
        styleModernField(allowanceField, new Dimension(180, 32));
        allowanceField.setMinimumSize(new Dimension(180, 32));

        JButton saveButton = new JButton("Save allowance");
        styleModernButton(saveButton, true);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAllowance();
            }
        });

        allowanceMessageLabel = new JLabel(" ");
        allowanceMessageLabel.setFont(SMALL_FONT);

        monthInfoLabel = new JLabel(" ");
        monthInfoLabel.setFont(LABEL_FONT);
        monthInfoLabel.setForeground(MUTED);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 6, 10);

        // Row 0: Label
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        form.add(smallLabel("MONTHLY ALLOWANCE"), gbc);

        // Row 1: Wide input + Save + Extra income
        gbc.gridy = 1;
        gbc.gridx = 0;
        form.add(allowanceField, gbc);

        gbc.gridx = 1;
        form.add(saveButton, gbc);

        gbc.gridx = 2;
        JButton incomeButton = new JButton("Add extra income");
        styleModernButton(incomeButton, false);
        incomeButton.addActionListener(e -> addIncome());
        form.add(incomeButton, gbc);

        // Row 2: Secondary action buttons
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(2, 0, 6, 10);
        JPanel allowanceActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        allowanceActions.setOpaque(false);
        JButton clearAllowanceButton = new JButton("Clear input");
        styleModernButton(clearAllowanceButton, false);
        clearAllowanceButton.addActionListener(e -> {
            allowanceField.setText("");
            allowanceMessageLabel.setText("Input cleared; saved allowance unchanged.");
            allowanceMessageLabel.setForeground(MUTED);
        });
        JButton resetAllowanceButton = new JButton("Reset allowance...");
        styleModernButton(resetAllowanceButton, false);
        resetAllowanceButton.addActionListener(e -> resetAllowance());
        JButton manageIncomeButton = new JButton("Manage income...");
        styleModernButton(manageIncomeButton, false);
        manageIncomeButton.addActionListener(e -> manageIncome());
        allowanceActions.add(clearAllowanceButton);
        allowanceActions.add(resetAllowanceButton);
        allowanceActions.add(manageIncomeButton);
        form.add(allowanceActions, gbc);

        // Row 3: Status message
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 4, 10);
        form.add(allowanceMessageLabel, gbc);

        // Row 4: Month info
        gbc.gridy = 4;
        form.add(monthInfoLabel, gbc);

        // Right side: Total budget card absorbs extra horizontal space
        gbc.gridy = 0;
        gbc.gridheight = 5;
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 8, 0, 0);
        form.add(createTotalBudgetPanel(), gbc);

        panel.add(form, BorderLayout.NORTH);
        panel.add(createPlanPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTotalBudgetPanel() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(BG);
        box.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, 12),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel title = smallLabel("TOTAL MONTHLY BUDGET");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalBudgetValueLabel = new JLabel("₱0.00");
        totalBudgetValueLabel.setFont(BIG_FONT);
        totalBudgetValueLabel.setForeground(ACCENT);
        totalBudgetValueLabel.putClientProperty("keepColor", Boolean.TRUE);
        totalBudgetValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        budgetBreakdownLabel = new JLabel("Allowance: ₱0.00  +  Extra income: ₱0.00");
        budgetBreakdownLabel.setFont(SMALL_FONT);
        budgetBreakdownLabel.setForeground(MUTED);
        budgetBreakdownLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(title);
        box.add(Box.createVerticalStrut(4));
        box.add(totalBudgetValueLabel);
        box.add(Box.createVerticalStrut(4));
        box.add(budgetBreakdownLabel);
        return box;
    }

    /** Planned budget per category, kept in a HashMap. */
    private JPanel createPlanPanel() {
        JPanel panel = createCardContainer(14, 16);
        panel.setLayout(new BorderLayout(0, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        planCategoryBox = new JComboBox<String>();
        planCategoryBox.setFont(LABEL_FONT);
        planCategoryBox.setPreferredSize(new Dimension(145, 30));

        planAmountField = new JTextField();
        styleModernField(planAmountField, new Dimension(115, 30));

        JButton setButton = new JButton("Set plan");
        styleModernButton(setButton, true);
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                savePlanForCategory();
            }
        });

        JButton clearButton = new JButton("Reset monthly plan...");
        styleModernButton(clearButton, false);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearPlan();
            }
        });

        toolbar.add(smallLabel("CATEGORY"));
        toolbar.add(planCategoryBox);
        toolbar.add(smallLabel("PLANNED AMOUNT"));
        toolbar.add(planAmountField);
        toolbar.add(setButton);
        JButton clearPlanInput = new JButton("Clear input");
        styleModernButton(clearPlanInput, false);
        clearPlanInput.addActionListener(e -> planAmountField.setText(""));
        toolbar.add(clearPlanInput);

        JPanel planActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        planActions.setOpaque(false);
        JButton deletePlanButton = new JButton("Delete selected plan...");
        styleModernButton(deletePlanButton, false);
        deletePlanButton.addActionListener(e -> deleteSelectedPlan());
        planActions.add(deletePlanButton);
        planActions.add(clearButton);

        planTableModel = new DefaultTableModel(new Object[][] {},
                new String[] {"Category", "Planned Budget", "Spent", "Budget Left"}) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        planTable = new JTable(planTableModel);
        planTable.setFont(LABEL_FONT);
        planTable.setRowHeight(30);
        planTable.setShowGrid(true);
        planTable.setGridColor(BORDER);
        planTable.setFillsViewportHeight(true);
        planTable.getTableHeader().setFont(BOLD_FONT);
        planTable.getTableHeader().setPreferredSize(new Dimension(0, 32));
        planTable.getTableHeader().setReorderingAllowed(false);
        planTable.getSelectionModel().addListSelectionListener(e -> {
            int row = planTable.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) {
                String category = planTable.getValueAt(row, 0).toString();
                planCategoryBox.setSelectedItem(category);
                Double amount = planned().get(category);
                planAmountField.setText(amount == null ? "" : MONEY.format(amount));
            }
        });

        JScrollPane scroll = new JScrollPane(planTable);
        scroll.setBorder(new RoundedBorder(BORDER, 10));

        JPanel planTools = new JPanel(new GridLayout(2, 1, 0, 6));
        planTools.setOpaque(false);
        planTools.add(toolbar);
        planTools.add(planActions);
        panel.add(planTools, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        fillCategoryBox();
        return panel;
    }

    // ------------------------------------------------------------------
    // Expenses: add, edit, delete
    // ------------------------------------------------------------------

    private void addExpense() {
        String description = descriptionField.getText().trim();
        if (description.length() == 0) {
            showError("Please type what you bought.");
            return;
        }

        double amount = readAmount(amountField.getText());
        if (amount <= 0) {
            showError("Please type an amount greater than zero.");
            return;
        }

        String category = (String) categoryBox.getSelectedItem();
        if (category == null) {
            showError("Please choose a category.");
            return;
        }

        LocalDate date = validatedExpenseDate();
        if (date == null) return;
        Expense expense = new Expense(date, description, category, amount);
        expenses.add(expense);
        revealExpenseMonth(expense.date);

        descriptionField.setText("");
        amountField.setText("");
        resetExpenseDate();
        rebuildExpenseTable();
        changed();
    }

    private void startEditingExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) {
            showError("Please click a row in the table first.");
            return;
        }

        Expense expense = shownExpenses.get(row);
        editingExpense = expense;

        descriptionField.setText(expense.description);
        amountField.setText(MONEY.format(expense.amount));
        categoryBox.setSelectedItem(expense.category);
        configureExpenseDate(expense.date, YearMonth.now());

        addExpenseButton.setVisible(false);
        saveExpenseButton.setVisible(true);
        cancelExpenseButton.setVisible(true);
        descriptionField.requestFocusInWindow();
    }

    private void saveExpenseChanges() {
        if (editingExpense == null) {
            return;
        }

        String description = descriptionField.getText().trim();
        double amount = readAmount(amountField.getText());
        String category = (String) categoryBox.getSelectedItem();

        if (description.length() == 0 || amount <= 0 || category == null) {
            showError("Please complete the description, amount and category.");
            return;
        }

        LocalDate date = validatedExpenseDate();
        if (date == null) return;
        Expense expense = editingExpense;
        expense.description = description;
        expense.amount = amount;
        expense.category = category;
        expense.date = date;
        revealExpenseMonth(expense.date);

        stopEditingExpense();
        rebuildExpenseTable();
        changed();
    }

    private void stopEditingExpense() {
        editingExpense = null;
        descriptionField.setText("");
        amountField.setText("");
        resetExpenseDate();
        addExpenseButton.setVisible(true);
        saveExpenseButton.setVisible(false);
        cancelExpenseButton.setVisible(false);
    }

    /** Removes the selected expense and pushes it on the undo stack. */
    private void deleteExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) {
            showError("Please click a row in the table first.");
            return;
        }

        Expense expense = shownExpenses.get(row);
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete \"" + expense.description + "\"?", "Delete expense",
                JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        expenses.remove(expense);
        dashboardMonth = YearMonth.from(expense.date);
        deleted.push(expense);
        stopEditingExpense();
        rebuildExpenseTable();
        changed();
    }

    /** Takes the last deleted expense off the Stack and puts it back. */
    private void undoDelete() {
        if (deleted.isEmpty()) {
            return;
        }
        Expense expense = deleted.pop();
        expenses.add(expense);
        revealExpenseMonth(expense.date);
        rebuildExpenseTable();
        changed();
    }

    /** Show the month of the saved record, even if it differs from today's month. */
    private void revealExpenseMonth(LocalDate date) {
        dashboardMonth = YearMonth.from(date);
        // Clear only the temporary display filters, not any saved financial records.
        expenseMonth = dashboardMonth;
        expenseFrom = null;
        expenseTo = null;
        expenseCategory = null;
        if (searchField != null) searchField.setText("");
        if (filterInfo != null) refreshFilterLabel();
    }

    private void addNewCategory() {
        String name = JOptionPane.showInputDialog(this, "New category name:");
        if (name == null) {
            return;
        }
        name = name.trim();

        if (name.length() == 0) {
            showError("The category name cannot be empty.");
            return;
        }
        if (categories.stream().anyMatch(name::equalsIgnoreCase)) {
            showError("That category already exists.");
            return;
        }

        categories.add(name);
        fillCategoryBox();
        categoryBox.setSelectedItem(name);
        changed();
    }

    /** History and optional date/category filtering, without changing the tab layout. */
    private void showFilters() {
        JTextField month = new JTextField(expenseMonth == null ? "" : expenseMonth.format(MONTH_FORMAT));
        JTextField from = new JTextField(expenseFrom == null ? "" : expenseFrom.format(GOAL_DATE));
        JTextField to = new JTextField(expenseTo == null ? "" : expenseTo.format(GOAL_DATE));
        JComboBox<String> category = new JComboBox<String>();
        category.addItem("All categories");
        for (String name : categories) category.addItem(name);
        if (expenseCategory != null) category.setSelectedItem(expenseCategory);
        Object[] fields = {"Month (MM/YYYY, blank = all history)", month,
                "From date (MM/DD/YYYY, optional)", from,
                "To date (MM/DD/YYYY, optional)", to, "Category", category};
        while (JOptionPane.showConfirmDialog(this, fields, "Expense filters",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                YearMonth m = parseFilterMonth(month.getText().trim());
                LocalDate f = parseFilterDate(from.getText().trim());
                LocalDate t = parseFilterDate(to.getText().trim());
                if (f != null && t != null && f.isAfter(t)) throw new IllegalArgumentException();
                expenseMonth = m;
                expenseFrom = f;
                expenseTo = t;
                expenseCategory = category.getSelectedIndex() == 0 ? null : (String) category.getSelectedItem();
                refreshFilterLabel();
                rebuildExpenseTable();
                return;
            } catch (RuntimeException e) {
                showError("Use MM/YYYY for the month and MM/DD/YYYY for dates.\nThe start date must not be later than the end date.");
            }
        }
    }

    public static YearMonth parseFilterMonth(String input) {
        if (input.isEmpty()) return null;
        if (!input.matches("[0-9]{2}/[0-9]{4}")) throw new IllegalArgumentException("Use MM/YYYY");
        YearMonth result = YearMonth.parse(input, MONTH_FORMAT);
        if (result.getYear() < 1) throw new IllegalArgumentException("Invalid year");
        return result;
    }

    public static LocalDate parseFilterDate(String input) {
        if (input.isEmpty()) return null;
        if (!input.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) throw new IllegalArgumentException("Use MM/DD/YYYY");
        LocalDate result = LocalDate.parse(input, GOAL_DATE);
        if (result.getYear() < 1) throw new IllegalArgumentException("Invalid year");
        return result;
    }

    private void refreshFilterLabel() {
        filterInfo.setText((expenseMonth == null ? "All months" : expenseMonth.format(MONTH_FORMAT))
                + (expenseFrom == null ? "" : " from " + expenseFrom.format(GOAL_DATE))
                + (expenseTo == null ? "" : " to " + expenseTo.format(GOAL_DATE))
                + (expenseCategory == null ? "" : " | " + expenseCategory));
    }

    // ------------------------------------------------------------------
    // Showing the expenses in the table
    // ------------------------------------------------------------------

    /**
- Rebuilds the table from the ArrayList.
- First we keep only the rows that match the search box, then we sort them,
- then we copy them into the table model.
     */
    private void rebuildExpenseTable() {
        if (expenseTableModel == null) {
            return;
        }

        ArrayList<Expense> result = new ArrayList<Expense>();
        String search = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            if ((expenseMonth == null || YearMonth.from(expense.date).equals(expenseMonth))
                    && (expenseFrom == null || !expense.date.isBefore(expenseFrom))
                    && (expenseTo == null || !expense.date.isAfter(expenseTo))
                    && (expenseCategory == null || expenseCategory.equals(expense.category))
                    && (search.length() == 0 || Expense.matches(expense, search))) {
                result.add(expense);
            }
        }

        // DATA STRUCTURE: bubble sort the filtered ArrayList before filling the table.
        Expense.bubbleSort(result, sortBox == null ? 0 : sortBox.getSelectedIndex());

        expenseTableModel.setRowCount(0);
        shownExpenses.clear();

        for (int i = 0; i < result.size(); i++) {
            Expense expense = result.get(i);
            shownExpenses.add(expense);
            expenseTableModel.addRow(new Object[] {
                expense.date.format(GOAL_DATE),
                expense.description,
                expense.category,
                "₱" + MONEY.format(expense.amount)
            });
        }

        if (expenseCountLabel != null) {
            if (result.size() == 0) {
                expenseCountLabel.setText("No expenses to show");
            } else {
                expenseCountLabel.setText(result.size() + " expense(s)");
            }
        }
    }

    // ------------------------------------------------------------------
    // Goals: add, edit, delete, deposit
    // ------------------------------------------------------------------

    private void addGoal() {
        String name = goalNameField.getText().trim();
        double target = readAmount(goalTargetField.getText());

        if (name.length() == 0) {
            showError("Please type a goal name.");
            return;
        }
        if (target <= 0) {
            showError("The target amount must be greater than zero.");
            return;
        }

        String deadline = readDeadline(goalDeadlineField.getText().trim(), LocalDate.now());
        if (deadline == null) {
            showError("Enter a valid deadline as MM/DD/YYYY (today or later).");
            return;
        }

        goals.add(new Goal(name, target, 0, deadline));

        goalNameField.setText("");
        goalTargetField.setText("");
        goalDeadlineField.setText("");
        rebuildGoalTable();
        changed();
    }

    private void startEditingGoal() {
        int row = goalTable.getSelectedRow();
        if (row < 0) {
            showError("Please click a goal in the table first.");
            return;
        }

        editingGoal = row;
        Goal goal = goals.get(row);

        goalNameField.setText(goal.name);
        goalTargetField.setText(MONEY.format(goal.target));
        goalDeadlineField.setText(goal.deadline == null ? "" : formatDeadline(goal.deadline));

        addGoalButton.setVisible(false);
        saveGoalButton.setVisible(true);
        cancelGoalButton.setVisible(true);
        goalNameField.requestFocusInWindow();
    }

    private void saveGoalChanges() {
        if (editingGoal < 0) {
            return;
        }

        String name = goalNameField.getText().trim();
        double target = readAmount(goalTargetField.getText());

        if (name.length() == 0 || target <= 0) {
            showError("Please type a name and a target above zero.");
            return;
        }

        Goal goal = goals.get(editingGoal);
        String entered = goalDeadlineField.getText().trim();
        String deadline = readDeadline(entered, LocalDate.now());
        // Existing overdue goals can still be edited without changing their deadline.
        if (deadline == null && entered.equals(formatDeadline(goal.deadline))) deadline = goal.deadline;
        if (deadline == null) {
            showError("Enter a valid deadline as MM/DD/YYYY (today or later).");
            return;
        }

        if (target < goal.saved) {
            showError("Target cannot be less than the amount already saved.");
            return;
        }
        goal.name = name;
        goal.target = target;
        goal.deadline = deadline;

        stopEditingGoal();
        rebuildGoalTable();
        changed();
    }

    private void stopEditingGoal() {
        editingGoal = -1;
        goalNameField.setText("");
        goalTargetField.setText("");
        goalDeadlineField.setText("");
        addGoalButton.setVisible(true);
        saveGoalButton.setVisible(false);
        cancelGoalButton.setVisible(false);
    }

    private void deleteGoal() {
        int row = goalTable.getSelectedRow();
        if (row < 0) {
            showError("Please click a goal in the table first.");
            return;
        }

        Goal goal = goals.get(row);
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete the goal \"" + goal.name + "\"?"
                + (goal.saved > 0 ? "\n₱" + MONEY.format(goal.saved)
                + " will return to this month's available balance. Deposit history is kept." : ""), "Delete goal",
                JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        data.resetGoal(goal, LocalDate.now());
        goals.remove(goal);
        dashboardMonth = YearMonth.now();
        depositField.setText("");
        stopEditingGoal();
        rebuildGoalTable();
        changed();
    }

    private void resetGoalProgress() {
        int row = goalTable.getSelectedRow();
        if (row < 0) { showError("Select a savings goal first."); return; }
        Goal goal = goals.get(row);
        if (goal.saved == 0) { showError("This goal already has zero progress."); return; }
        if (JOptionPane.showConfirmDialog(this,
                "Reset progress for \"" + goal.name + "\"?\n₱" + MONEY.format(goal.saved)
                + " will return to this month's available balance.\nThe goal and its deposit history will be kept.",
                "Reset savings progress", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.OK_OPTION) return;
        data.resetGoal(goal, LocalDate.now());
        dashboardMonth = YearMonth.now();
        depositField.setText("");
        stopEditingGoal();
        changed();
    }

    private void depositToGoal() {
        int row = goalTable.getSelectedRow();
        if (row < 0) {
            showError("Please click a goal in the table first.");
            return;
        }

        double amount = readAmount(depositField.getText());
        if (amount <= 0) {
            showError("Please type an amount greater than zero.");
            return;
        }

        Goal goal = goals.get(row);
        if (amount > data.balance(YearMonth.now()) + 0.000001) {
            showError("Not enough money left this month. Add allowance or income first.");
            return;
        }
        if (amount > goal.target - goal.saved + 0.000001) {
            showError("The deposit exceeds the amount still needed for this goal.");
            return;
        }
        goal.saved = LedgerData.cents(goal.saved + amount);
        data.deposits.add(new MoneyEntry(LocalDate.now(), goal.name, amount));
        dashboardMonth = YearMonth.now();
        depositField.setText("");

        rebuildGoalTable();
        changed();

        if (goal.saved >= goal.target) {
            JOptionPane.showMessageDialog(this,
                    "Goal reached! " + goal.name + " is fully saved.",
                    "Congratulations", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showSavingsAdvice() {
        int row = goalTable.getSelectedRow();
        if (row < 0) {
            showError("Select a savings goal first.");
            return;
        }
        Goal goal = goals.get(row);
        LocalDate today = LocalDate.now();
        LocalDate deadline = storedDeadline(goal.deadline);
        double remaining = Math.max(0, goal.target - goal.saved);
        long days = daysUntilDeadline(today, deadline);
        String advice = remaining == 0 ? "Goal complete!"
                : days <= 0 ? "Deadline passed. Update the goal deadline."
                : "Still needed: ₱" + MONEY.format(remaining)
                + "\nSave about ₱" + MONEY.format(Math.ceil(remaining / days * 100) / 100)
                + " per day for " + days + " day(s).";
        JOptionPane.showMessageDialog(this, advice, goal.name, JOptionPane.INFORMATION_MESSAGE);
    }

    private void rebuildGoalTable() {
        if (goalTableModel == null) {
            return;
        }

        goalTableModel.setRowCount(0);
        for (int i = 0; i < goals.size(); i++) {
            Goal goal = goals.get(i);
            int percent = goal.percentSaved();
            goalTableModel.addRow(new Object[] {
                goal.name,
                "₱" + MONEY.format(goal.target),
                "₱" + MONEY.format(goal.saved),
                percent + "%",
                goal.deadline == null ? "-" : formatDeadline(goal.deadline)
            });
        }

        if (goalInfoLabel != null) {
            goalInfoLabel.setText(goals.size() == 0
                    ? "No goals yet"
                    : goals.size() + " goal(s)");
        }
    }

    // ------------------------------------------------------------------
    // Allowance and planned budget
    // ------------------------------------------------------------------

    private void saveAllowance() {
        double value = readAmount(allowanceField.getText());
        if (value <= 0) {
            allowanceMessageLabel.setForeground(RED);
            allowanceMessageLabel.setText("Please type an amount greater than zero.");
            return;
        }

        data.allowances.put(YearMonth.now(), value);
        dashboardMonth = YearMonth.now();
        boolean saved = changed();
        allowanceMessageLabel.setForeground(saved ? GREEN : RED);
        allowanceMessageLabel.setText(saved ? "Allowance saved." : "Not yet saved to disk.");
    }

    private void addIncome() {
        MoneyEntry entry = incomeForm(null);
        if (entry == null) return;
        data.incomes.add(entry);
        dashboardMonth = YearMonth.now();
        changed();
    }

    private MoneyEntry incomeForm(MoneyEntry existing) {
        JTextField source = new JTextField(existing == null ? "" : existing.description);
        JTextField value = new JTextField(existing == null ? "" : MONEY.format(existing.amount));
        while (true) {
            int action = JOptionPane.showOptionDialog(this,
                    new Object[] {"Income source", source, "Amount", value},
                    existing == null ? "Add extra income" : "Edit income (" + existing.date.format(GOAL_DATE) + ")",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[] {"Save", "Clear", "Cancel"}, "Save");
            if (action == 1) { source.setText(""); value.setText(""); continue; }
            if (action != 0) return null;
            double amount = readAmount(value.getText());
            if (source.getText().trim().isEmpty() || amount <= 0) {
                showError("Enter a source and a valid positive amount.");
                continue;
            }
            return new MoneyEntry(existing == null ? LocalDate.now() : existing.date,
                    source.getText().trim(), amount);
        }
    }

    private void manageIncome() {
        while (true) {
            if (data.incomes.isEmpty()) { showError("There are no saved extra income records."); return; }
            DefaultTableModel model = new DefaultTableModel(new String[] {"Date", "Source", "Amount"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (MoneyEntry entry : data.incomes)
                model.addRow(new Object[] {entry.date.format(GOAL_DATE), entry.description, "₱" + MONEY.format(entry.amount)});
            JTable table = new JTable(model);
            table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(510, 200));
            int action = JOptionPane.showOptionDialog(this, scroll, "Extra income history (all months)",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[] {"Edit", "Delete", "Close"}, "Close");
            if (action < 0 || action > 1) return;
            int row = table.getSelectedRow();
            if (row < 0) { showError("Select an income record first."); continue; }
            MoneyEntry entry = data.incomes.get(row);
            if (action == 0) {
                MoneyEntry edited = incomeForm(entry);
                if (edited == null) continue;
                data.incomes.set(row, edited);
            } else {
                if (JOptionPane.showConfirmDialog(this, "Delete \"" + entry.description + "\" (₱"
                        + MONEY.format(entry.amount) + ") from " + entry.date.format(GOAL_DATE) + "?\nIts month's balance will be recalculated.",
                        "Delete extra income", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)
                        != JOptionPane.OK_OPTION) continue;
                data.incomes.remove(row);
            }
            dashboardMonth = YearMonth.from(entry.date);
            changed();
        }
    }

    private void resetAllowance() {
        YearMonth month = YearMonth.now();
        if (JOptionPane.showConfirmDialog(this, "Set the allowance for " + month.format(MONTH_FORMAT) + " to ₱0.00?\n"
                + "Expenses, extra income, savings and other months will stay unchanged.",
                "Reset allowance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.OK_OPTION) return;
        data.allowances.remove(month);
        allowanceField.setText("0.00");
        dashboardMonth = month;
        boolean saved = changed();
        allowanceMessageLabel.setForeground(saved ? GREEN : RED);
        allowanceMessageLabel.setText(saved ? "Allowance reset." : "Not yet saved to disk.");
    }

    private double totalPlannedExcluding(String excludedCategory) {
        double total = 0;
        for (Map.Entry<String, Double> entry : planned().entrySet()) {
            if (!entry.getKey().equals(excludedCategory) && entry.getValue() != null) {
                total = LedgerData.cents(total + entry.getValue().doubleValue());
            }
        }
        return total;
    }

    private void enforceBudgetCap() {
        YearMonth month = YearMonth.now();
        double totalBudget = LedgerData.cents(data.allowance(month) + data.extraIncome(month));
        HashMap<String, Double> monthPlan = planned();

        if (totalBudget <= 0) {
            if (!monthPlan.isEmpty()) {
                monthPlan.clear();
                showError("Not enough remaining budget: total monthly budget is ₱0.00, so existing planned amounts were cleared.");
            }
            return;
        }

        double totalPlanned = 0;
        for (String category : categories) {
            Double val = monthPlan.get(category);
            if (val != null && val > 0) {
                totalPlanned = LedgerData.cents(totalPlanned + val.doubleValue());
            }
        }

        double excess = LedgerData.cents(totalPlanned - totalBudget);
        if (excess <= 0) {
            return;
        }

        double trimmed = excess;
        for (int i = categories.size() - 1; i >= 0 && excess > 0; i--) {
            String category = categories.get(i);
            Double val = monthPlan.get(category);
            if (val == null || val <= 0) {
                continue;
            }
            double current = val.doubleValue();
            if (current <= excess) {
                monthPlan.remove(category);
                excess = LedgerData.cents(excess - current);
            } else {
                monthPlan.put(category, Double.valueOf(LedgerData.cents(current - excess)));
                excess = 0;
            }
        }

        showError("Not enough remaining budget: planned amounts were reduced by ₱" + MONEY.format(trimmed)
                + " to fit your ₱" + MONEY.format(totalBudget) + " total monthly budget.");
    }

    private void savePlanForCategory() {
        String category = (String) planCategoryBox.getSelectedItem();
        if (category == null) {
            return;
        }

        double amount = readAmount(planAmountField.getText());
        if (amount <= 0) {
            showError("Please type a planned amount greater than zero.");
            return;
        }

        YearMonth month = YearMonth.now();
        double totalBudget = LedgerData.cents(data.allowance(month) + data.extraIncome(month));
        double usedByOthers = totalPlannedExcluding(category);
        double remainingBudget = LedgerData.cents(totalBudget - usedByOthers);

        if (amount > remainingBudget + 0.000001) {
            showError("Not enough remaining budget.\n"
                    + "Total monthly budget: ₱" + MONEY.format(totalBudget) + "\n"
                    + "Already planned in other categories: ₱" + MONEY.format(usedByOthers) + "\n"
                    + "Remaining budget available: ₱" + MONEY.format(Math.max(0, remainingBudget)));
            return;
        }

        // HashMap put: if the key is already there, the value is replaced.
        planned().put(category, Double.valueOf(amount));
        dashboardMonth = month;
        planAmountField.setText("");
        rebuildPlanTable();
        changed();
    }

    private void clearPlan() {
        if (planned().isEmpty()) { showError("No budget plans for this month."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete all category budget plans for " + YearMonth.now().format(MONTH_FORMAT)
                + "?\nExpenses and other months will stay unchanged.", "Reset monthly plan",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) return;
        planned().clear();
        planAmountField.setText("");
        dashboardMonth = YearMonth.now();
        rebuildPlanTable();
        changed();
    }

    private void deleteSelectedPlan() {
        int row = planTable.getSelectedRow();
        if (row < 0) { showError("Select a category row first."); return; }
        String category = planTable.getValueAt(row, 0).toString();
        if (!planned().containsKey(category)) { showError("This category has no saved plan."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete the " + category + " plan for " + YearMonth.now().format(MONTH_FORMAT)
                + "?\nIts actual expenses will remain.", "Delete budget plan",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) return;
        planned().remove(category);
        planAmountField.setText("");
        dashboardMonth = YearMonth.now();
        changed();
    }

    private void rebuildPlanTable() {
        if (planTableModel == null) {
            return;
        }

        planTableModel.setRowCount(0);
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            Double plannedAmount = planned().get(category);
            double plan = plannedAmount == null ? 0 : plannedAmount.doubleValue();
            double actual = spentInCategory(category);

            String difference;
            if (plan <= 0) {
                difference = "-";
            } else if (actual > plan) {
                difference = "-₱" + MONEY.format(actual - plan);
            } else {
                difference = "₱" + MONEY.format(plan - actual);
            }

            planTableModel.addRow(new Object[] {
                category,
                plan <= 0 ? "-" : "₱" + MONEY.format(plan),
                actual <= 0 ? "-" : "₱" + MONEY.format(actual),
                difference
            });
        }
    }

    // ------------------------------------------------------------------
    // Refresh every screen after the data changed
    // ------------------------------------------------------------------

    private void refreshAll() {
        updateDashboardMonths();
        refreshDashboard();
        rebuildGoalTable();
        enforceBudgetCap();
        rebuildPlanTable();
        updateReportMonths();
        rebuildReports();
        updateAllowanceInfo();
        updateTotalBudgetCard();

        if (undoButton != null) {
            undoButton.setEnabled(!deleted.isEmpty());
        }
    }

    private void updateDashboardMonths() {
        if (dashboardMonthBox == null) return;
        if (dashboardMonth == null) dashboardMonth = data.initialDashboardMonth(YearMonth.now());
        ArrayList<YearMonth> choices = data.months();
        if (!choices.contains(dashboardMonth)) choices.add(dashboardMonth);
        fillingDashboardMonths = true;
        dashboardMonthBox.removeAllItems();
        for (YearMonth choice : choices) dashboardMonthBox.addItem(choice.format(MONTH_FORMAT));
        dashboardMonthBox.setSelectedItem(dashboardMonth.format(MONTH_FORMAT));
        fillingDashboardMonths = false;
    }

    private void refreshDashboard() {
        if (allowanceValueLabel == null) {
            return;
        }

        YearMonth month = dashboardMonth == null ? YearMonth.now() : dashboardMonth;
        DashboardTotals totals = data.dashboardTotals(month, LocalDate.now());
        double allowance = totals.allowance;
        double extra = totals.income;
        double savings = totals.savings;
        double spent = totals.spent;
        double left = totals.balance;
        double perDay = totals.dailyBudget;

        allowanceValueLabel.setText("₱" + MONEY.format(allowance));
        spentValueLabel.setText("₱" + MONEY.format(spent));
        leftValueLabel.setText("₱" + MONEY.format(left));
        leftValueLabel.setForeground(left < 0 ? RED : GREEN);
        perDayValueLabel.setText(totals.remainingDays == 0 ? "—" : "₱" + MONEY.format(perDay));
        perDayValueLabel.setForeground(perDay < 0 ? RED : new Color(2, 132, 199));

        StringBuilder text = new StringBuilder();
        int count = totals.expenseCount;
        text.append("Month             : ").append(month.format(MONTH_FORMAT)).append("\n");
        if (!data.hasActivity(month)) {
            text.append("No saved financial records for this month.\n")
                    .append("Select another month above, or save an allowance / add an expense.\n\n");
        }
        text.append("Extra income      : ₱").append(MONEY.format(extra)).append("\n");
        text.append("Savings deposits  : ₱").append(MONEY.format(savings)).append("\n");
        text.append("Savings returned  : ₱").append(MONEY.format(data.savingsReturned(month))).append("\n");
        text.append("Expenses recorded : ").append(count).append("\n");
        text.append("Total spent      : ₱").append(MONEY.format(spent)).append("\n");

        if (count > 0) {
            text.append("Average expense  : ₱").append(MONEY.format(spent / count)).append("\n");
        }

        Expense biggest = biggestExpense(month);
        if (biggest != null) {
            text.append("Biggest expense  : ").append(biggest.description)
                    .append(" (₱").append(MONEY.format(biggest.amount)).append(")\n");
        }

        double availableFunds = allowance + extra + data.savingsReturned(month);
        if (availableFunds > 0) {
            long used = Math.round((spent + savings) / availableFunds * 100);
            text.append("Available funds used: ").append(used).append("%\n");
        }
        if (left < 0) text.append("WARNING: Spending and savings exceed this month's funds.\n");
        else if (availableFunds > 0 && left <= availableFunds * 0.1)
            text.append("WARNING: 10% or less of this month's funds remain.\n");

        text.append("\nSpending per category\n");
        text.append("---------------------\n");
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            double amount = data.spent(month, category);
            if (amount > 0) {
                text.append(String.format("%-12s ₱%s%n", category, MONEY.format(amount)));
            }
        }

        text.append("\nPlanned budget (HashMap)\n");
        text.append("------------------------\n");
        Map<String, Double> monthPlan = data.plans.get(month);
        if (monthPlan == null || monthPlan.isEmpty()) {
            text.append("No plan set yet. Open the Allowance tab.\n");
        } else {
            for (int i = 0; i < categories.size(); i++) {
                String category = categories.get(i);
                Double amount = monthPlan.get(category);
                if (amount != null) {
                    text.append(String.format("%-12s ₱%s%n", category, MONEY.format(amount.doubleValue())));
                    if (data.spent(month, category) > amount)
                        text.append("  WARNING: category budget exceeded.\n");
                }
            }
        }

        if (!deleted.isEmpty()) {
            text.append("\nUndo stack       : ").append(deleted.size()).append(" deleted expense(s)\n");
        }

        summaryArea.setText(text.toString());
    }

    private void updateAllowanceInfo() {
        if (monthInfoLabel == null) {
            return;
        }

        YearMonth month = YearMonth.now();
        int daysInMonth = month.lengthOfMonth();
        int daysLeft = daysInMonth - LocalDate.now().getDayOfMonth() + 1;
        if (daysLeft < 1) {
            daysLeft = 1;
        }

        String monthName = month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        monthInfoLabel.setText(monthName + "  -  " + daysInMonth + " days in the month  -  "
                + daysLeft + " day(s) left");
    }

    private void updateTotalBudgetCard() {
        if (totalBudgetValueLabel == null || budgetBreakdownLabel == null) {
            return;
        }
        YearMonth month = YearMonth.now();
        double baseAllowance = data.allowance(month);
        double extra = data.extraIncome(month);
        double totalBudget = baseAllowance + extra;

        totalBudgetValueLabel.setText("₱" + MONEY.format(totalBudget));
        budgetBreakdownLabel.setText("Allowance: ₱" + MONEY.format(baseAllowance)
                + "  +  Extra income: ₱" + MONEY.format(extra));
    }

    // ------------------------------------------------------------------
    // Small calculations
    // ------------------------------------------------------------------

    /** Loops over the ArrayList and adds every amount. */
    private double totalSpent() {
        return data.spent(YearMonth.now(), null);
    }

    /** Loops again, but only adds the rows of one category. */
    private double spentInCategory(String category) {
        return data.spent(YearMonth.now(), category);
    }

    /** Linear search for the largest expense. */
    private Expense biggestExpense(YearMonth month) {
        Expense biggest = null;
        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            if (YearMonth.from(expense.date).equals(month)
                    && (biggest == null || expense.amount > biggest.amount)) {
                biggest = expense;
            }
        }
        return biggest;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private JLabel smallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(MUTED);
        return label;
    }

    /**
- Copies the category names into both combo boxes.
- The two boxes are created at different times, so each one is checked first.
     */
    private void fillCategoryBox() {
        if (categoryBox != null) {
            categoryBox.removeAllItems();
            for (int i = 0; i < categories.size(); i++) {
                categoryBox.addItem(categories.get(i));
            }
        }
        if (planCategoryBox != null) {
            planCategoryBox.removeAllItems();
            for (int i = 0; i < categories.size(); i++) {
                planCategoryBox.addItem(categories.get(i));
            }
        }
    }

    private LocalDate getDateFromSpinner() {
        Date value = (Date) dateSpinner.getValue();
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static Date spinnerDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Allow dates anywhere in {@code month}, from its 1st day through its last day. */
    static SpinnerDateModel expenseDateModel(LocalDate initial, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Date minDate = spinnerDate(start);
        Date maxDate = spinnerDate(end);
        LocalDate clampedInitial = clampToMonth(initial == null ? LocalDate.now() : initial, month);
        return new SpinnerDateModel(spinnerDate(clampedInitial), minDate, maxDate, Calendar.DAY_OF_MONTH) {
            private static final long serialVersionUID = 1L;

            @Override
            public void setCalendarField(int ignored) {
                super.setCalendarField(Calendar.DAY_OF_MONTH);
            }
        };
    }

    static LocalDate clampToMonth(LocalDate date, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        if (date.isBefore(start)) return start;
        if (date.isAfter(end)) return end;
        return date;
    }

    static boolean allowedExpenseDate(LocalDate date, YearMonth month) {
        if (date == null || month == null) return false;
        return YearMonth.from(date).equals(month);
    }

    private void configureExpenseDate(LocalDate value, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        SpinnerDateModel model = (SpinnerDateModel) dateSpinner.getModel();
        model.setStart(spinnerDate(start));
        model.setEnd(spinnerDate(end));

        LocalDate target = value == null ? LocalDate.now() : value;
        LocalDate selected = allowedExpenseDate(target, month) ? target : clampToMonth(target, month);
        dateSpinner.setValue(spinnerDate(selected));
        dateSpinner.setEnabled(true);
        ((JSpinner.DateEditor) dateSpinner.getEditor()).getTextField().setEditable(true);
        dateSpinner.setToolTipText("Select any date within the current month ("
                + start.format(GOAL_DATE) + " - " + end.format(GOAL_DATE) + ").");
        dateMonth = month;
        dateFloor = LocalDate.now();
    }

    private void resetExpenseDate() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate defaultDate = clampToMonth(LocalDate.now(), currentMonth);
        configureExpenseDate(defaultDate, currentMonth);
        // Discard even an uncommitted date typed directly into the editor.
        ((JSpinner.DateEditor) dateSpinner.getEditor()).getTextField().setValue(spinnerDate(defaultDate));
    }

    private void refreshDateFloor() {
        if (dateSpinner == null) return;
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();
        if (!currentMonth.equals(dateMonth) || !today.equals(dateFloor)) {
            configureExpenseDate(getDateFromSpinner(), currentMonth);
        }
    }

    /** Reads "1,250.50" or "1250" and returns 0 when it is not a number. */
    static double readAmount(String text) {
        try {
            String raw = text.trim();
            if (!raw.matches("([0-9]+|[0-9]{1,3}(,[0-9]{3})+)(\\.[0-9]{1,2})?")) return 0;
            String cleaned = raw.replace(",", "");
            double value = Double.parseDouble(cleaned);
            return Double.isFinite(value) && value <= 1000000000 ? value : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate validatedExpenseDate() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        try {
            dateSpinner.commitEdit();
            LocalDate date = getDateFromSpinner();
            if (!allowedExpenseDate(date, currentMonth)) throw new IllegalArgumentException();
            return date;
        } catch (Exception e) {
            LocalDate fallback = clampToMonth(LocalDate.now(), currentMonth);
            configureExpenseDate(fallback, currentMonth);
            showError("Choose a date within the current month ("
                    + start.format(GOAL_DATE) + " to " + end.format(GOAL_DATE) + ").");
            return null;
        }
    }

    /** Strict MM/DD/YYYY input, saved as an unambiguous ISO date. */
    static String readDeadline(String text, LocalDate today) {
        try {
            if (!text.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) return null;
            LocalDate date = LocalDate.parse(text, GOAL_DATE);
            return date.isBefore(today) ? null : date.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /** Old month-only goals keep the final-day deadline used by the previous advice. */
    public static LocalDate storedDeadline(String stored) {
        if (stored.matches("[0-9]{4}-[0-9]{2}")) return YearMonth.parse(stored).atEndOfMonth();
        return storedDate(stored);
    }

    /** Accept ISO storage and valid slash-format dates from older/custom builds. */
    public static LocalDate storedDate(String stored) {
        return stored.indexOf('/') >= 0 ? parseFilterDate(stored) : LocalDate.parse(stored);
    }

    public static YearMonth storedMonth(String stored) {
        return stored.indexOf('/') >= 0 ? parseFilterMonth(stored) : YearMonth.parse(stored);
    }

    public static String formatDeadline(String stored) {
        return storedDeadline(stored).format(GOAL_DATE);
    }

    static long daysUntilDeadline(LocalDate today, LocalDate deadline) {
        return ChronoUnit.DAYS.between(today, deadline) + 1;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }

    private void paintSidebar() {
        contentPane.setBackground(BG);

        refreshAll();

        if (allowanceValueLabel != null)
            allowanceValueLabel.setForeground(ACCENT);

        if (spentValueLabel != null)
            spentValueLabel.setForeground(RED);

        if (sidebar != null) {
            sidebar.setBackground(SIDE);

            for (Component c : sidebar.getComponents()) {
                if (c instanceof JButton) {
                    JButton b = (JButton) c;
                    boolean on = SIDE_ON.equals(b.getBackground());

                    b.setOpaque(true);
                    b.setContentAreaFilled(true);
                    b.setBackground(on ? SIDE_ON : SIDE);
                    b.setForeground(Color.WHITE);

                } else if (c instanceof JLabel) {
                    ((JLabel) c).setForeground(Color.WHITE);
                }
            }
        }

        repaint();
    }

    /** Force readable colors on every component. The look-and-feel ignores setForeground unless we also set opaque. */
    private void applyTheme(Component root, Color oldBg, Color oldCard) {
        if (root == null) return;

        Color fieldBg = darkMode ? new Color(50, 54, 62) : Color.WHITE;
        Color fieldFg = darkMode ? new Color(240, 242, 245) : new Color(28, 32, 40);
        Color btnBg = darkMode ? new Color(70, 74, 84) : new Color(240, 243, 248);

        if (root instanceof JPanel) {
            JPanel panel = (JPanel) root;
            if (panel == sidebar) {
                panel.setBackground(SIDE);
                panel.setOpaque(true);
            } else {
                Color current = panel.getBackground();
                if (current != null && (sameColor(current, oldBg) || sameColor(current, oldCard)
                        || sameColor(current, Color.WHITE) || sameColor(current, new Color(24, 24, 28))
                        || sameColor(current, new Color(36, 36, 42)))) {
                    panel.setBackground(sameColor(current, oldBg) || sameColor(current, new Color(24, 24, 28)) ? BG : CARD);
                    panel.setOpaque(true);
                }
            }
        } else if (root instanceof JLabel) {
            JLabel label = (JLabel) root;
            if (!Boolean.TRUE.equals(label.getClientProperty("keepColor"))) {
                label.setForeground(fieldFg);
                label.setOpaque(false);
            }
        } else if (root instanceof JTextField || root instanceof JTextArea || root instanceof JPasswordField) {
            JTextComponent tc = (JTextComponent) root;
            tc.setOpaque(true);
            tc.setBackground(fieldBg);
            tc.setForeground(fieldFg);
            tc.setCaretColor(fieldFg);
            tc.setDisabledTextColor(fieldFg);
        } else if (root instanceof JSpinner) {
            JSpinner spinner = (JSpinner) root;
            spinner.setOpaque(true);
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
                tf.setOpaque(true);
                tf.setBackground(fieldBg);
                tf.setForeground(fieldFg);
                tf.setCaretColor(fieldFg);
            }
        } else if (root instanceof JTable) {
            JTable table = (JTable) root;
            table.setBackground(CARD);
            table.setForeground(fieldFg);
            table.setGridColor(BORDER);
            table.setSelectionBackground(darkMode ? new Color(80, 90, 120) : new Color(200, 215, 240));
            table.setSelectionForeground(fieldFg);
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                public Component getTableCellRendererComponent(JTable t, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    if (isSelected) {
                        c.setBackground(darkMode ? new Color(80, 90, 120) : new Color(200, 215, 240));
                        c.setForeground(fieldFg);
                    } else {
                        c.setBackground(CARD);
                        c.setForeground(fieldFg);
                    }
                    return c;
                }
            });
            if (table.getTableHeader() != null) {
                table.getTableHeader().setBackground(darkMode ? new Color(50, 54, 62) : new Color(220, 228, 238));
                table.getTableHeader().setForeground(fieldFg);
                table.getTableHeader().setOpaque(true);
            }
        } else if (root instanceof JComboBox) {
            root.setBackground(fieldBg);
            root.setForeground(fieldFg);
            ((JComboBox<?>) root).setOpaque(true);
        } else if (root instanceof JButton) {
            JButton button = (JButton) root;
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            if (button.getParent() == sidebar) {
                button.setForeground(Color.WHITE);
                button.setBackground(SIDE);
            } else if (ACCENT.equals(button.getBackground()) || "Add goal".equals(button.getText())
                    || "Add expense".equals(button.getText()) || "Save allowance".equals(button.getText())) {
                button.setBackground(ACCENT);
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(btnBg);
                button.setForeground(fieldFg);
            }
        } else if (root instanceof JCheckBox) {
            ((JCheckBox) root).setForeground(fieldFg);
            ((JCheckBox) root).setOpaque(false);
        } else if (root instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) root;
            scroll.getViewport().setBackground(CARD);
            scroll.getViewport().setOpaque(true);
            scroll.setBorder(new RoundedBorder(BORDER, 10));
        }

        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                applyTheme(child, oldBg, oldCard);
            }
        }
    }

    private static boolean sameColor(Color a, Color b) {
        return a.getRed() == b.getRed() && a.getGreen() == b.getGreen() && a.getBlue() == b.getBlue();
    }

    private void logOut() {
        int answer = JOptionPane.showConfirmDialog(this, "Log out now?",
                "Log out", JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        if (unsaved && !saveData()) return;
        dispose();
        LoginDialog login = new LoginDialog();
        login.setVisible(true);
        if (login.isLoggedIn()) {
            openTracker(login.getUsername());
        } else {
            System.exit(0);
        }
    }

    // ------------------------------------------------------------------
    // Reports tab (the graph)
    // ------------------------------------------------------------------

    /**
- Two simple bar charts built from Swing panels.
     */
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        categoryBarsPanel = new JPanel();
        categoryBarsPanel.setBackground(CARD);
        categoryBarsPanel.setLayout(new BoxLayout(categoryBarsPanel, BoxLayout.Y_AXIS));

        dailyBarsPanel = new JPanel();
        dailyBarsPanel.setBackground(CARD);
        dailyBarsPanel.setLayout(new BoxLayout(dailyBarsPanel, BoxLayout.Y_AXIS));

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0));
        charts.setOpaque(false);
        charts.add(createChartCard("Spending per category", categoryBarsPanel));
        charts.add(createChartCard("Spending per day (selected month)", dailyBarsPanel));

        JPanel summaryCard = createCardContainer(8, 14);
        summaryCard.setLayout(new BorderLayout());
        reportSummaryLabel = new JLabel(" ");
        reportSummaryLabel.setFont(LABEL_FONT);
        reportSummaryLabel.setForeground(MUTED);
        summaryCard.add(reportSummaryLabel, BorderLayout.CENTER);

        JPanel toolbarCard = createCardContainer(10, 14);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        reportMonthBox = new JComboBox<String>();
        reportMonthBox.setPreferredSize(new Dimension(115, 30));
        reportMonthBox.addActionListener(e -> {
            if (!fillingReportMonths) rebuildReports();
        });
        JButton exportButton = new JButton("Export CSV...");
        styleModernButton(exportButton, true);
        exportButton.addActionListener(e -> exportReport());
        toolbar.add(smallLabel("REPORT MONTH"));
        toolbar.add(reportMonthBox);
        toolbar.add(exportButton);
        toolbarCard.setLayout(new BorderLayout());
        toolbarCard.add(toolbar, BorderLayout.WEST);

        panel.add(toolbarCard, BorderLayout.NORTH);
        panel.add(charts, BorderLayout.CENTER);
        panel.add(summaryCard, BorderLayout.SOUTH);
        return panel;
    }

    /** A white card with a title and a column of bars inside. */
    private JPanel createChartCard(String title, JPanel bars) {
        JPanel card = createCardContainer(14, 16);
        card.setLayout(new BorderLayout(0, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(INK);

        card.add(titleLabel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(bars);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private YearMonth reportMonth() {
        Object selected = reportMonthBox == null ? null : reportMonthBox.getSelectedItem();
        return selected == null ? YearMonth.now() : parseFilterMonth(selected.toString());
    }

    private void updateReportMonths() {
        if (reportMonthBox == null) return;
        YearMonth selected = reportMonth();
        fillingReportMonths = true;
        reportMonthBox.removeAllItems();
        for (YearMonth month : data.months()) reportMonthBox.addItem(month.format(MONTH_FORMAT));
        reportMonthBox.setSelectedItem(selected.format(MONTH_FORMAT));
        if (reportMonthBox.getSelectedItem() == null) reportMonthBox.setSelectedItem(YearMonth.now().format(MONTH_FORMAT));
        fillingReportMonths = false;
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("ledger-" + reportMonth() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv"))
            file = new File(file.getParentFile(), file.getName() + ".csv");
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Replace this CSV file?",
                "Export report", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            data.exportCsv(file.toPath(), reportMonth());
            JOptionPane.showMessageDialog(this, "Report exported to " + file.getName());
        } catch (IOException e) {
            showError("Could not export the report: " + e.getMessage());
        }
    }

    /** One bar: a label on the left, the bar in the middle, the amount on the right. */
    private void addBar(JPanel target, String labelText, double value, double max, String amountText, Color color) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setBorder(new EmptyBorder(3, 0, 3, 0));

        JLabel name = new JLabel(labelText);
        name.setFont(SMALL_FONT);
        name.setForeground(INK);
        name.setPreferredSize(new Dimension(92, 18));

        ReportBar bar = new ReportBar(value, max, color);

        JLabel amount = new JLabel(amountText);
        amount.setFont(SMALL_FONT);
        amount.setForeground(MUTED);
        amount.setHorizontalAlignment(SwingConstants.RIGHT);
        amount.setPreferredSize(new Dimension(76, 18));

        row.add(name, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(amount, BorderLayout.EAST);
        target.add(row);
    }

    /** Stable color by category's saved ArrayList position, not its sorted rank. */
    private static final Color[] CATEGORY_COLORS = {
        new Color(22, 163, 74), new Color(37, 99, 235), new Color(234, 88, 12),
        new Color(124, 58, 237), new Color(13, 148, 136), new Color(190, 24, 93),
        new Color(161, 98, 7), new Color(8, 145, 178), new Color(220, 38, 38),
        new Color(71, 85, 105)
    };

    public static Color categoryColor(int index) {
        if (index < 0) return ACCENT;
        if (index < CATEGORY_COLORS.length) return CATEGORY_COLORS[index];
        // Custom categories beyond the palette get a new hue automatically.
        return Color.getHSBColor((float) ((index * 0.61803398875) % 1), 0.78f, 0.75f);
    }

    /** Draw the actual color rather than letting Nimbus override a progress bar. */

    private JLabel chartNote(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL_FONT);
        label.setForeground(MUTED);
        label.setBorder(new EmptyBorder(6, 2, 0, 0));
        return label;
    }

    /** Fills both charts from the ArrayList of expenses. */
    private void rebuildReports() {
        if (categoryBarsPanel == null) {
            return;
        }

        categoryBarsPanel.removeAll();
        dailyBarsPanel.removeAll();

        YearMonth month = reportMonth();
        ArrayList<Slice> byCategory = sortedCategoryTotals(month);
        double maxCategory = 0;
        for (int i = 0; i < byCategory.size(); i++) {
            maxCategory = Math.max(maxCategory, byCategory.get(i).amount);
        }
        if (byCategory.size() == 0) {
            categoryBarsPanel.add(chartNote("No expenses yet."));
        } else {
            for (int i = 0; i < byCategory.size(); i++) {
                Slice slice = byCategory.get(i);
                addBar(categoryBarsPanel, slice.name, slice.amount, maxCategory,
                        "\u20b1" + MONEY.format(slice.amount), categoryColor(categories.indexOf(slice.name)));
            }
        }

        ArrayList<Slice> byDay = dailyTotals(month);
        double maxDay = 0;
        for (int i = 0; i < byDay.size(); i++) {
            maxDay = Math.max(maxDay, byDay.get(i).amount);
        }
        for (int i = 0; i < byDay.size(); i++) {
            Slice slice = byDay.get(i);
            addBar(dailyBarsPanel, slice.name, slice.amount, maxDay,
                    slice.amount > 0 ? "\u20b1" + MONEY.format(slice.amount) : "-", ACCENT);
        }

        double total = data.spent(month, null);
        reportSummaryLabel.setText("<html>" + month.format(MONTH_FORMAT) + " | Income: ₱" + MONEY.format(data.allowance(month) + data.extraIncome(month))
                + " | Expenses: ₱" + MONEY.format(total)
                + " | Savings: ₱" + MONEY.format(data.savings(month))
                + " | Savings returned: ₱" + MONEY.format(data.savingsReturned(month))
                + " | Balance: ₱" + MONEY.format(data.balance(month)) + "</html>");

        categoryBarsPanel.revalidate();
        categoryBarsPanel.repaint();
        dailyBarsPanel.revalidate();
        dailyBarsPanel.repaint();
    }

    /** One slice of the graph: a name and its amount. */

    /** Totals per category, biggest first (bubble sort again). */
    private ArrayList<Slice> sortedCategoryTotals(YearMonth month) {
        ArrayList<Slice> list = new ArrayList<Slice>();
        for (int i = 0; i < categories.size(); i++) {
            double amount = data.spent(month, categories.get(i));
            if (amount > 0) {
                list.add(new Slice(categories.get(i), amount));
            }
        }
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                if (list.get(j).amount < list.get(j + 1).amount) {
                    Slice temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
        return list;
    }

    /** Totals for each day of the selected month, oldest first. */
    private ArrayList<Slice> dailyTotals(YearMonth month) {
        ArrayList<Slice> list = new ArrayList<Slice>();

        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            double total = 0;
            for (int j = 0; j < expenses.size(); j++) {
                if (expenses.get(j).date.equals(day)) {
                    total = total + expenses.get(j).amount;
                }
            }
            list.add(new Slice(day.format(GOAL_DATE), total));
        }
        return list;
    }
}