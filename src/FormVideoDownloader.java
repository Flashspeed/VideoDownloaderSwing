import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FormVideoDownloader {
    private JTextField txtUrl;
    private JComboBox cmbDownloadType;
    private JPanel panelMain;
    private JComboBox<String> cmbAudioFormat;
    private JButton btnDownloadLocation;
    private JLabel lblDownloadLocation;
    private JPanel panelOptions;
    private JButton downloadButton;
    private JLabel lblDownloadProgress;
    private boolean isVideoDownloadSelected;
    private JFileChooser jFileChooserForDownloadLocation = new JFileChooser();
    private String saveDirectory = "";
    private String operatingSystem;
    private StringBuilder commandStringBuilder = new StringBuilder();
    private String startingCommand = "cmd.exe /c youtube-dl ";
    private String audioFormat; // e.g mp3, wav
    private String videoFormat; // e.g mp4, mov, flv

    public FormVideoDownloader()
    {
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
            JComboBox jComboBox = (JComboBox)e.getSource();
            audioFormat = jComboBox.getSelectedItem().toString();
            System.out.println(audioFormat);
        });

        cmbDownloadType.addActionListener(e -> {
            JComboBox jComboBox = (JComboBox)e.getSource();
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

                if(!isVideoDownloadSelected)
                {
                    commandStringBuilder.append("-x ");
                    commandStringBuilder.append("--audio-format ").append(audioFormat).append(" ");
                }

                if(!saveDirectory.isEmpty())
                {
                    commandStringBuilder.append("-o \"").append(saveDirectory).append("\" ");
                    commandStringBuilder.append("--ignore-config ");
                }

                // Final value in the command should be the url
                if (isUrl(txtUrl.getText()))
                {
                    commandStringBuilder.append(txtUrl.getText());
                }

                new Thread(new CMDRunnable(commandStringBuilder.toString())).start();
                System.out.println("Ran (" + commandStringBuilder.toString() + ")" );
                commandStringBuilder.delete(startingCommand.length(), commandStringBuilder.length());
                System.out.println("After StringBuilder reset (" + commandStringBuilder.toString() + ")" );
            }
        });
    }

    /**
     * Checks if a given string is in the format of a URL
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
     * @param oSNameToCheck Operating system name to check e.g "Windows"
     * @return True if the operating system name is the same as the name passed in
     */
    public boolean operatingSystemNameIs(String oSNameToCheck)
    {
        return operatingSystem.startsWith(oSNameToCheck.toLowerCase().trim());
    }

    public void load()
    {
        JFrame frame = new JFrame( "Video Downloader" );
        frame.setContentPane( new FormVideoDownloader().panelMain );
        frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible( true );
    }

    private void createUIComponents() {
        String[] audioFormats = {"mp3", "wav", "m4a"};
        String[] downloadTypes = {"Video", "Audio"};

        cmbAudioFormat = new JComboBox<>(audioFormats);
        cmbAudioFormat.setSelectedIndex(0);

        cmbDownloadType = new JComboBox<>(downloadTypes);
        cmbDownloadType.setSelectedIndex(0);
    }
}
