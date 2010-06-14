/* DiplayVideoPanel.java
 * Created on 16. Mai 2007, 12:27
 * @author Christian Gruber Bakk.techn.
 */

package ttt.gui.wizard.recorder;

import java.awt.Component;
import java.io.IOException;

import javax.media.CaptureDeviceInfo;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;

/**
 * Shows video, error messages and progress status
 */
public class DiplayVideoPanel extends javax.swing.JPanel implements ControllerListener {

    private Player videoPlayer = null;

    private String noStream_EN = "Could not get the videostream at the capture "
            + "device! Make sure the device is connected, turned on and installed.";

    private String noStream_DE = "Es konnte keine Verbindung zu der Videoquelle hergestellt werden. "
            + "Versichern Sie sich, dass die Quelle angeschlossen, eingeschalten und installiert ist";

    private String searching_EN = "Searching videostream. This might take several minutes!";

    private String searching_DE = "Suche Videostream! Dies kann einige Minuten dauern!";

    private String noDevice_EN = "No registered video device was found! You can try to detect "
            + "one - this might take up to 15 minutes without response.";

    private String noDevice_DE = "Es wurde keine registrierte Videoquelle gefunden. "
            + "Sie k�nnen versuchen eine Quelle zu registrieren - dies kann bis zu 15 Minuten dauern.";

    private String noDeviceButton_EN = "Detect and Register Videodevice";

    private String noDeviceButton_DE = "Videoquelle erfassen und registrieren";

    private String retryButtonText_EN = "Retry";

    private String retryButtonText_DE = "Wiederholen";

    public DiplayVideoPanel() {
        initComponents();
    }

    public void setLanguage(String language) {
        if (language == "EN") {
            messageArea.setText(noStream_EN);
            retryButton.setText(retryButtonText_EN);
            searchingLabel.setText(searching_EN);
            noDeviceTextArea.setText(noDevice_EN);
            detectVideoDevicesButton.setText(noDeviceButton_EN);
        } else {
            messageArea.setText(noStream_DE);
            retryButton.setText(retryButtonText_DE);
            searchingLabel.setText(searching_DE);
            noDeviceTextArea.setText(noDevice_DE);
            detectVideoDevicesButton.setText(noDeviceButton_DE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        noStreamPanel = new javax.swing.JPanel();
        retryButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
        showingPanel = new javax.swing.JPanel();
        workingPanel = new javax.swing.JPanel();
        searchingLabel = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        noDevicePanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        noDeviceTextArea = new javax.swing.JTextArea();
        detectVideoDevicesButton = new javax.swing.JButton();

        setLayout(new java.awt.CardLayout());

        noStreamPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.insets = new java.awt.Insets(19, 0, 19, 0);
        noStreamPanel.add(retryButton, gridBagConstraints);

        jScrollPane1.setBorder(null);
        messageArea.setBackground(java.awt.SystemColor.control);
        messageArea.setColumns(20);
        messageArea.setEditable(false);
        messageArea.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        messageArea.setLineWrap(true);
        messageArea.setRows(5);
        messageArea.setWrapStyleWord(true);
        messageArea.setMinimumSize(new java.awt.Dimension(300, 200));
        jScrollPane1.setViewportView(messageArea);

        noStreamPanel.add(jScrollPane1, new java.awt.GridBagConstraints());

        add(noStreamPanel, "card3");

        showingPanel.setLayout(new java.awt.GridLayout(1, 0));

        showingPanel.setMinimumSize(new java.awt.Dimension(300, 200));
        add(showingPanel, "card4");

        workingPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 80));

        workingPanel.setMinimumSize(new java.awt.Dimension(381, 179));
        searchingLabel.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        workingPanel.add(searchingLabel);

        jProgressBar1.setEnabled(false);
        jProgressBar1.setIndeterminate(true);
        workingPanel.add(jProgressBar1);

        add(workingPanel, "card4");

        noDevicePanel.setLayout(new java.awt.GridBagLayout());

        noDevicePanel.setMinimumSize(new java.awt.Dimension(300, 200));
        jScrollPane3.setBorder(null);
        noDeviceTextArea.setBackground(java.awt.SystemColor.control);
        noDeviceTextArea.setColumns(20);
        noDeviceTextArea.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        noDeviceTextArea.setLineWrap(true);
        noDeviceTextArea.setRows(5);
        noDeviceTextArea.setWrapStyleWord(true);
        jScrollPane3.setViewportView(noDeviceTextArea);

        noDevicePanel.add(jScrollPane3, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 14, 0);
        noDevicePanel.add(detectVideoDevicesButton, gridBagConstraints);

        add(noDevicePanel, "card5");

    }// </editor-fold>//GEN-END:initComponents

    /**
     * Tries to create a videoplayer with the given capture device info; if successful the controllerUpdate method will
     * be informed and publishes the video on the showingPanel
     * 
     * @param cdi
     *            a capture device info object from which the videostream can be taken
     */
    public void showVideo(CaptureDeviceInfo cdi) {

        callProcessingPanel();

        givePlayerResourcesFree();

        // default video players are created as heavy componentes - we have to change this
        Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, Boolean.TRUE);

        // FIXME: This workaround tries to get a video player two times. That's because if you connect
        // the video device the returned component from the player has no content. If the player is
        // dealocated and alocated anew, the returned component consists a video. Maybe this happens
        // only with my Tevion Web-Cam - other models should be tested
        try {
            videoPlayer = Manager.createPlayer(cdi.getLocator());
            videoPlayer.realize();
            videoPlayer.prefetch();
            videoPlayer.start();
            givePlayerResourcesFree();

        } catch (NoPlayerException ex) {
            callNoStreamPanel(); // "Could not connect to capture device! make sure the device is connected, turned
                                    // on and installed.");
        } catch (IOException ex) {
            callNoStreamPanel(); // "Could not read data from selected device! Try another one from the combobox.");
        }

        try {
            videoPlayer = Manager.createPlayer(cdi.getLocator());
            videoPlayer.realize();
            videoPlayer.prefetch();
            videoPlayer.start();
            videoPlayer.addControllerListener(this);

        } catch (NoPlayerException ex) {
            callNoStreamPanel(); // "Could not connect to capture device! Make sure the device is connected, turned
                                    // on and installed.");
        } catch (IOException ex) {
            callNoStreamPanel(); // "Could not read data from selected device! Retry or try another one from the
                                    // combobox.");
        }
    }

    /**
     * Deallocates all resources of the current video player
     */
    public void givePlayerResourcesFree() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.deallocate();
            videoPlayer.close();
        }
    }

    /**
     * Automatically called if the video has loaded
     */
    public void controllerUpdate(ControllerEvent controllerEvent) {
        if (controllerEvent instanceof RealizeCompleteEvent) {

            // remove video before adding a new one
            this.showingPanel.removeAll();
            Component comp = videoPlayer.getVisualComponent();
            // resize the video
            comp.setBounds(0, 0, 320, 240);
            this.showingPanel.add(comp);
            // processing finished -> give controls back to the user
            callShowingPanel();
        }
    }

    /**
     * Brings the showing panel to the front
     */
    public void callShowingPanel() {
        jProgressBar1.setEnabled(false);
        this.workingPanel.setVisible(false);
        this.noStreamPanel.setVisible(false);
        this.noDevicePanel.setVisible(false);
        this.showingPanel.setVisible(true);
        this.validate();
    }

    /**
     * Brings the processing panel to the front
     */
    public void callProcessingPanel() {
        this.jProgressBar1.setEnabled(true);
        this.workingPanel.setVisible(true);
        this.noStreamPanel.setVisible(false);
        this.noDevicePanel.setVisible(false);
        this.showingPanel.setVisible(false);
        this.validate();
    }

    /**
     * Brings the error panel for no video stream to the front
     */
    public void callNoStreamPanel() {
        this.jProgressBar1.setEnabled(false);
        this.workingPanel.setVisible(false);
        this.noStreamPanel.setVisible(true);
        this.noDevicePanel.setVisible(false);
        this.showingPanel.setVisible(false);
        this.validate();
    }

    /**
     * Brings the error panel for no device found to the front
     */
    public void callNoDevicePanel() {
        this.jProgressBar1.setEnabled(false);
        this.workingPanel.setVisible(false);
        this.noStreamPanel.setVisible(false);
        this.noDevicePanel.setVisible(true);
        this.showingPanel.setVisible(false);
        this.validate();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton detectVideoDevicesButton;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JPanel noDevicePanel;
    private javax.swing.JTextArea noDeviceTextArea;
    public javax.swing.JPanel noStreamPanel;
    public javax.swing.JButton retryButton;
    private javax.swing.JLabel searchingLabel;
    public javax.swing.JPanel showingPanel;
    private javax.swing.JPanel workingPanel;
    // End of variables declaration//GEN-END:variables

}