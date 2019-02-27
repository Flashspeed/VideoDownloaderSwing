
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormVideoDownloader
{
    private JTextField        txtUrl;
    private JComboBox         cmbDownloadType;
    private JPanel            panelMain;
    private JComboBox<String> cmbAudioFormat;
    private JButton           btnDownloadLocation;
    private JLabel            lblDownloadLocation;
    private JPanel            panelOptions;
    private JButton           downloadButton;
    private JLabel            lblDownloadProgress;
    private boolean           isVideoDownloadSelected;
    private JFileChooser      jFileChooserForDownloadLocation = new JFileChooser();
    private String            saveDirectory                   = "";
    private String            operatingSystem;
    private StringBuilder     commandStringBuilder            = new StringBuilder();
    private String            startingCommand                 = "cmd.exe /c youtube-dl ";
    private String            audioFormat; // e.g mp3, wav
    private String            videoFormat; // e.g mp4, mov, flv

    public FormVideoDownloader()
    {
        $$$setupUI$$$();
        if (cmbDownloadType.getSelectedIndex() == 0)
        {
            cmbAudioFormat.setEnabled(false);
        }
        cmbAudioFormat.setSelectedIndex(0);
        isVideoDownloadSelected = true;
        //########################################################################
        operatingSystem = System.getProperty("os.name").toLowerCase();
        System.out.println(operatingSystem);
        System.out.println(System.getProperty("user.home"));

        commandStringBuilder.append(startingCommand);

        jFileChooserForDownloadLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        cmbAudioFormat.addActionListener(e -> {
            JComboBox jComboBox = (JComboBox) e.getSource();
            audioFormat = jComboBox.getSelectedItem().toString();
            System.out.println(audioFormat);
        });

        cmbDownloadType.addActionListener(e -> {
            JComboBox jComboBox = (JComboBox) e.getSource();
            if (jComboBox.getSelectedIndex() == 0)
            {
                cmbAudioFormat.setEnabled(false);
                isVideoDownloadSelected = true;
            }
            else
            {
                cmbAudioFormat.setEnabled(true);
                audioFormat = cmbAudioFormat.getSelectedItem().toString();
                isVideoDownloadSelected = false;
            }
        });

        btnDownloadLocation.addActionListener(e -> {

            int fileChooserReturnValue = jFileChooserForDownloadLocation.showOpenDialog(panelMain);

            if (fileChooserReturnValue == JFileChooser.APPROVE_OPTION)
            {
                File file = jFileChooserForDownloadLocation.getSelectedFile();

                saveDirectory = file.toString()
                        + File.separatorChar
                        + "Youtube-Downloader-Downloads"
                        + File.separatorChar
                        + "%(title)s.%(ext)s";

                lblDownloadLocation.setText(file.toString());
                System.out.println("Save Directory: " + saveDirectory);
            }
        });

        downloadButton.addActionListener(e -> {
            if (operatingSystemNameIs("windows"))
            {
                class CMDRunnable implements Runnable
                {
                    private String command;

                    /**
                     * @param cmd The command to be executed
                     */
                    CMDRunnable(String cmd)
                    {
                        command = cmd;
                    }

                    @Override
                    public void run()
                    {
                        Process process = null;

                        try
                        {
                            process = Runtime.getRuntime().exec(command);
                        }
                        catch (IOException e1)
                        {
                            e1.printStackTrace();
                        }

                        if (process != null)
                        {
                            getOutputFromCommand(process);
                        }
                    }

                }

                if (!isVideoDownloadSelected)
                {
                    commandStringBuilder.append("-x ")
                            .append("--audio-format ").append(audioFormat).append(" ");
                }

                if (!saveDirectory.isEmpty())
                {
                    commandStringBuilder.append("-o \"").append(saveDirectory).append("\" ")
                            .append("--ignore-config ")
                            .append("-f 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best' ");
                }

                // Final value in the command should be the url
                if (isUrl(txtUrl.getText()))
                {
                    commandStringBuilder.append(txtUrl.getText());
                }

                new Thread(new CMDRunnable(commandStringBuilder.toString())).start();
                System.out.println("Ran (" + commandStringBuilder.toString() + ")");
                commandStringBuilder.delete(startingCommand.length(), commandStringBuilder.length());
                System.out.println("After StringBuilder reset (" + commandStringBuilder.toString() + ")");
            }
        });
    }

    /**
     * Checks if a given string is in the format of a URL
     *
     * @param url String to check
     * @return True if the string is in a URL format
     */
    private boolean isUrl(String url)
    {
        final String REGEX_FOR_URL = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern pattern = Pattern.compile(REGEX_FOR_URL);
        Matcher matcher = pattern.matcher(url);
        return matcher.find();
    }

    /**
     * Returns string value representing the download progress
     *
     * @param str String to search for download percentage e.g "[download] 34.9% ..."
     * @return
     */
    private String findDownloadProgress(String str)
    {
        final String REGEX_PATTERN_PASS_1 = "^(\\[download])\\s+(\\d+(\\.)?\\d%)";
        Pattern pattern_pass_1 = Pattern.compile(REGEX_PATTERN_PASS_1);
        Matcher matcher_pass_1 = pattern_pass_1.matcher(str);

        String extractedStringFromPass1;

        if (matcher_pass_1.find())
        {
            extractedStringFromPass1 = matcher_pass_1.group(0);
        }
        else
        {
            return "No match";
        }

        final String REGEX_PATTERN_PASS_2 = "\\b(?<!\\.)(?!0+(?:\\.0+)?%)(?:\\d|[1-9]\\d|100)(?:(?<!100)\\.\\d+)?%";
        Pattern pattern_pass_2 = Pattern.compile(REGEX_PATTERN_PASS_2);
        Matcher matcher_pass_2 = pattern_pass_2.matcher(extractedStringFromPass1);

        if (matcher_pass_2.find())
        {
            return matcher_pass_2.group(0).replace("%", "");
        }
        else
        {
            return "No Match here too";
        }
    }

    /**
     * Prints the output returned from a shell command if any
     *
     * @param usingProcess Process to monitor for output
     */
    public void getOutputFromCommand(Process usingProcess)
    {
        System.out.println(usingProcess.getOutputStream());
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(usingProcess.getInputStream()));
        String outputFromCommand;

        try
        {
            while ((outputFromCommand = stdInput.readLine()) != null)
            {
                System.out.println(outputFromCommand);
                if (outputFromCommand.contains("[download]"))
                {
                    System.out.println("Download Progress: " + findDownloadProgress(outputFromCommand));
                    lblDownloadProgress.setText(findDownloadProgress(outputFromCommand) + "%");
                }
                if (outputFromCommand.contains("[download] 100%"))
                {
                    System.out.println("DOWNLOAD COMPLETE");
                    lblDownloadProgress.setText("DOWNLOAD COMPLETE");
                }
            }

            usingProcess.destroy();
        }
        catch (IOException e2)
        {
            e2.printStackTrace();
        }
    }

    /**
     * Checks what operating system the program is running on
     *
     * @param oSNameToCheck Operating system name to check e.g "Windows"
     * @return True if the operating system name is the same as the name passed in
     */
    public boolean operatingSystemNameIs(String oSNameToCheck)
    {
        return operatingSystem.startsWith(oSNameToCheck.toLowerCase().trim());
    }

    public void load()
    {
        JFrame frame = new JFrame("Video Downloader");
        frame.setContentPane(new FormVideoDownloader().panelMain);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUIComponents()
    {
        String[] audioFormats = {"mp3", "wav", "m4a"};
        String[] downloadTypes = {"Video", "Audio"};

        cmbAudioFormat = new JComboBox<>(audioFormats);
        cmbAudioFormat.setSelectedIndex(0);

        cmbDownloadType = new JComboBox<>(downloadTypes);
        cmbDownloadType.setSelectedIndex(0);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        createUIComponents();
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.setMinimumSize(new Dimension(-1, -1));
        panelMain.setPreferredSize(new Dimension(600, 400));
        txtUrl = new JTextField();
        Font txtUrlFont = this.$$$getFont$$$(null, -1, 18, txtUrl.getFont());
        if (txtUrlFont != null) txtUrl.setFont(txtUrlFont);
        panelMain.add(txtUrl, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 40), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(panel1, new GridConstraints(5, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Font cmbDownloadTypeFont = this.$$$getFont$$$(null, -1, 14, cmbDownloadType.getFont());
        if (cmbDownloadTypeFont != null) cmbDownloadType.setFont(cmbDownloadTypeFont);
        panel1.add(cmbDownloadType, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
        Font cmbAudioFormatFont = this.$$$getFont$$$(null, -1, 14, cmbAudioFormat.getFont());
        if (cmbAudioFormatFont != null) cmbAudioFormat.setFont(cmbAudioFormatFont);
        panel1.add(cmbAudioFormat, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
        panelOptions = new JPanel();
        panelOptions.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelOptions.setOpaque(true);
        panel1.add(panelOptions, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnDownloadLocation = new JButton();
        Font btnDownloadLocationFont = this.$$$getFont$$$(null, -1, 14, btnDownloadLocation.getFont());
        if (btnDownloadLocationFont != null) btnDownloadLocation.setFont(btnDownloadLocationFont);
        btnDownloadLocation.setText("Choose download location");
        panelMain.add(btnDownloadLocation, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 30), null, 0, false));
        lblDownloadLocation = new JLabel();
        lblDownloadLocation.setFocusTraversalPolicyProvider(true);
        Font lblDownloadLocationFont = this.$$$getFont$$$(null, -1, 14, lblDownloadLocation.getFont());
        if (lblDownloadLocationFont != null) lblDownloadLocation.setFont(lblDownloadLocationFont);
        lblDownloadLocation.setText("");
        panelMain.add(lblDownloadLocation, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(panel2, new GridConstraints(6, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        downloadButton = new JButton();
        Font downloadButtonFont = this.$$$getFont$$$(null, -1, 26, downloadButton.getFont());
        if (downloadButtonFont != null) downloadButton.setFont(downloadButtonFont);
        downloadButton.setText("Download");
        panel2.add(downloadButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(130, 60), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setFocusable(false);
        panel2.add(panel3, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblDownloadProgress = new JLabel();
        Font lblDownloadProgressFont = this.$$$getFont$$$(null, -1, 24, lblDownloadProgress.getFont());
        if (lblDownloadProgressFont != null) lblDownloadProgress.setFont(lblDownloadProgressFont);
        lblDownloadProgress.setOpaque(false);
        lblDownloadProgress.setRequestFocusEnabled(false);
        lblDownloadProgress.setText("");
        panel3.add(lblDownloadProgress, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 14, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("URL");
        panelMain.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelMain.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, new Dimension(10, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panelMain.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 30), null, 0, false));
        final Spacer spacer4 = new Spacer();
        panelMain.add(spacer4, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 0, false));
        final Spacer spacer5 = new Spacer();
        panelMain.add(spacer5, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final Spacer spacer6 = new Spacer();
        panelMain.add(spacer6, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont)
    {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {resultName = currentFont.getName();}
        else
        {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {resultName = fontName;}
            else {resultName = currentFont.getName();}
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() { return panelMain; }

}
