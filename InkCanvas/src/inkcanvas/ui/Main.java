package inkcanvas.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // fallback to default
            }
            MainWindow window = new MainWindow();
            // Save all data to disk when the window is closed
            window.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    inkcanvas.service.FileManager.saveAll(inkcanvas.service.DataStore.get());
                }
            });
            window.setVisible(true);
        });
    }
}

