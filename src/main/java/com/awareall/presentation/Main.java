
package com.awareall.presentation;

import com.awareall.core.location.LocationManagerSingleton;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class Main {

    private static JLabel riskLabel;
    private static JLabel statusLabel;
    private static JTextArea threatArea;
    private static JTable incidentTable;
    private static DefaultTableModel tableModel;

    public static void main(String[] args) {       
        System.out.println("AwareAll app is running");

        // 1. Launch real UI Dashboard
        SwingUtilities.invokeLater(() -> createAndShowDashboard());

        // 2. Initialize your logic
        LocationManagerSingleton locationManager = LocationManagerSingleton.getInstance();
        
        // Simulating data updates into the UI
        sleep(1000);
        updateDashboardData();
    }

    private static void createAndShowDashboard() {
        JFrame frame = new JFrame("AwareAll Desktop Security v2.0");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(5, 5));

        // --- HEADER PANEL ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        headerPanel.setBackground(new Color(25, 35, 45));
        
        riskLabel = new JLabel(" RISK INDICATOR: 25.0 ");
        riskLabel.setOpaque(true);
        riskLabel.setBackground(new Color(40, 167, 69));
        riskLabel.setForeground(Color.WHITE);
        riskLabel.setFont(new Font("Arial", Font.BOLD, 12));

        statusLabel = new JLabel(" CONNECTION STATUS: NETWORK ACTIVE (UDP) ");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(23, 162, 184));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));

        headerPanel.add(riskLabel);
        headerPanel.add(statusLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        // --- LEFT PANEL (Threats & Adjustments) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        leftPanel.add(new JLabel("ACTIVE THREATS LIST:"));
        threatArea = new JTextArea(5, 20);
        threatArea.setEditable(false);
        threatArea.setText("MURDER | Weight: 1.0\nANIMAL_ATTACK | Weight: 0.99\nRAPE | Weight: 0.95\nPEDOPHILIA | Weight: 0.90\nKNIFE_ASSAULT | Weight: 0.85\nMANIACS | 0.80\nROBBERY | Weight: 0.75\nCELEBRITY_HARASSMENT | Weight: 0.70\nBEAT_DOWN | Weight: 0.65\nBODILY_HARM | Weight: 0.60\nSTALKING | Weight: 0.55\nRACIAL_INSULT | Weight: 0.50\nHATE_SPEECH | Weight: 0.45\nTHEFT | Weight: 0.40");
        leftPanel.add(new JScrollPane(threatArea));

        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(new JLabel("MANUAL ADJUSTMENT:"));
        leftPanel.add(new JCheckBox("Account for night time (+15.5)"));
        leftPanel.add(new JCheckBox("Ignore minor incidents"));

        frame.add(leftPanel, BorderLayout.WEST);

        // --- CENTER PANEL (Map Simulator) ---
        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(240, 240, 240));
                g.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw grid lines for the map
                g.setColor(Color.LIGHT_GRAY);
                for(int i = 0; i < getWidth(); i += 30) g.drawLine(i, 0, i, getHeight());
                for(int j = 0; j < getHeight(); j += 30) g.drawLine(0, j, getWidth(), j);

                // Emulate a dangerous geozone (orange circle overlay)
                g.setColor(new Color(255, 140, 0, 100));
                g.fillOval(getWidth() / 2 - 100, getHeight() / 2 - 100, 200, 200);
                
                g.setColor(Color.RED);
                g.drawString("Central Risk Zone (Tel Aviv)", getWidth() / 2 - 80, getHeight() / 2);
            }
        };
        mapPanel.setBorder(BorderFactory.createTitledBorder("INTERACTIVE RISK MAP"));
        frame.add(mapPanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL (Incident Table & SOS Button) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        // Incident Data Table Setup
        String[] columns = {"ID", "Time", "Threat Type", "Weight (Severity)", "Incident Status"};
        tableModel = new DefaultTableModel(columns, 0);
        incidentTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(incidentTable);
        tableScroll.setPreferredSize(new Dimension(0, 150));
        
        bottomPanel.add(new JLabel(" Warning! Emergency assistance required. My coordinates sent to dispatcher."), BorderLayout.NORTH);
        bottomPanel.add(tableScroll, BorderLayout.CENTER);

        // Huge SOS Action Button
        JButton sosButton = new JButton("SOS");
        sosButton.setBackground(new Color(230, 55, 55));
        sosButton.setForeground(Color.WHITE);
        sosButton.setFont(new Font("Arial", Font.BOLD, 20));
        sosButton.setFocusPainted(false);
        bottomPanel.add(sosButton, BorderLayout.SOUTH);

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static void updateDashboardData() {
        // Simulating row injections into the table component matching your screenshot
        SwingUtilities.invokeLater(() -> {
            for (int i = 12; i >= 6; i--) {
                tableModel.addRow(new Object[]{
                    String.valueOf(i), "15-08-2026 05:15", "PSC System Test Launch", "Δ Attention (Level 1)", "INFO"
                });
            }
        });
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}


//перейти к написанию тестового сценария в Main.java, чтобы запустить симуляцию ходьбы и проверить, как срабатывает автоматика!
//Теперь, когда у нас полностью готовы LocationManagerSingleton, SosEventBus, SosEvent, SosObserver и SafetyViewModel, все части пазла MVVM-архитектуры соединены!
// создайте тестовый класс Main.java, в котором мы пропишем пару реальных шагов пользователя на карте (смена координат) и увидим в консоли VS Code, как автоматически оживает вся цепочка безопасности

// написать Main.java, чтобы запустить симуляцию перемещения по координатам и увидеть, 
// как все созданные нами архитектурные слои работают вместе.

// написать эмуляцию движения пользователя в Main.java, чтобы сымитировать, как при изменении координат автоматически включается точный GPS и // написать эмуляцию движения пользователя в Main.java, чтобы сымитировать, как при изменении координат автоматически включается точный GPS и 

// запускается таймер «Убийцы»? Или проверим работу triggerManualSos?
// Если у вас возникнут вопросы по работе SosEventBus или по проектированию реальной базы данных, дайте знать.
// написать быстрый тест для проверки нашего LocationManagerSingleton прямо в методе main
// Куда дальше двигаетесь по логике — займётесь шиной событий SosEventBus или перейдёте к реализации самой базы данных

// Как закрыть программу: Просто кликните по терминалу VS Code, где выполнялась 
// команда, и нажмите клавишу ENTER (или нажмите Ctrl + C). Процесс Java завершится, 
// и окно закроется.

// Теперь вы можете спокойно дописывать графический интерфейс в вашем проекте. 
// Для любого нового запуска просто используйте эти две команды в терминале:Теперь вы можете спокойно дописывать графический интерфейс в вашем проекте. Для любого нового запуска просто используйте эти две команды в терминале:

// javac -sourcepath . $(find . -name "*.java")
// java com.awareall.presentation.Main


// Важно: Если вы хотите, чтобы окно оставалось открытым, пока вы не закроете его вручную, закомментируйте строки с System.in.read() и оставьте только код для отображения окна.

