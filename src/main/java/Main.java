import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.core.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Main
{
    static String ffmpegBinLocation = "";
    static File ffmpegZipFile;
    static String ffmpegZipURL;

    public static void main(String[] args)
    {
        System.out.println("Installing required components...");

        StringBuilder programDownloadDir = new StringBuilder();

        String ffmpegZipFileName = "ffmpegZipDownload.zip";
        ffmpegZipURL = "https://ffmpeg.zeranoe.com/builds/win64/static/ffmpeg-20190126-d8ebfd1-win64-static.zip";
        String[] splitFFMPEG = ffmpegZipURL.split("/");
        String extractedFFMpegFileName = splitFFMPEG[6]; // ffmpeg-20190126-d8ebfd1-win64-static.zip
        String[] splitExtractedFFMpegFileName = extractedFFMpegFileName.split("\\."); // split .zip extension
        programDownloadDir
                .append(System.getProperty("user.home"))
                .append(System.getProperty("file.separator"))
                .append(".VideoDownloaderSwing")
                .append(System.getProperty("file.separator")
                );
        ffmpegBinLocation = programDownloadDir.toString()
                + "ffmpegZipExtracted"
                + System.getProperty("file.separator")
                + splitExtractedFFMpegFileName[0]
                + System.getProperty("file.separator")
                + "bin";
        System.out.println("ffmpegBinLocation: " + ffmpegBinLocation);

        ffmpegZipFile = new File(programDownloadDir.toString() + ffmpegZipFileName);

        createDirectory(programDownloadDir.toString());
        createDirectory(programDownloadDir.toString() + "ffmpegZipExtracted");
        downloadFFMpeg(ffmpegZipURL, ffmpegZipFile);
        unzipFFMpeg(ffmpegZipFile, programDownloadDir.toString() + "ffmpegZipExtracted");
        String commandSetFFMpegWindowsPath = "SETX /M PATH %PATH%;" + ffmpegBinLocation;
//        addFFMpegToWindowsPath(commandSetFFMpegWindowsPath);

//        System.out.println(programDownloadDir.toString());

//        if (new FormVideoDownloader().operatingSystemNameIs("windows"))
//        {
//            // Install stuff
//        }
        new FormVideoDownloader().load();
    }

    private static void createDirectory(String location)
    {
        File installDir = new File(location);

        if (!installDir.exists())
        {
            if (installDir.mkdirs())
            {
                System.out.println("\"" + installDir + "\" path created");
            }
            else
            {
                System.out.println("\"" + installDir + "\" path NOT created");
            }
        }
        else
        {
            System.out.println("\"" + installDir.toString() + "\" already exists");
        }
    }

    private static void downloadFFMpeg(String source, File destination)
    {
        if (destination != null)
        {
            // Only download the zip file if it does not already exist
            if (!destination.exists())
            {
                try
                {
                    System.out.println("Downloading ffmpeg...");
                    URL url = new URL(source);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
                    InputStream inputStream = urlConnection.getInputStream();
                    FileOutputStream fileOutputStream = new FileOutputStream(destination);
                    byte[] b = new byte[2024];
                    int byteCount;
                    while ((byteCount = inputStream.read(b)) >= 0)
                    {
                        fileOutputStream.write(b, 0, byteCount);
                    }
                    System.out.println("ffmpeg download complete");
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void unzipFFMpeg(File zipSource, String extractionDestination)
    {
        if (!new File(extractionDestination).exists())
        {
            // Unzip the file
            try
            {
                System.out.println("Unzipping FFMpeg...");
                ZipFile zipFile = new ZipFile(zipSource);
                zipFile.extractAll(extractionDestination);
                System.out.println("FFMpeg unzip complete");

            }
            catch (ZipException e)
            {
                e.printStackTrace();
            }
        }

    }

    private static void addFFMpegToWindowsPath(String addPathCommand)
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
                    new FormVideoDownloader().getOutputFromCommand(process);

                }
            }

        }
        new Thread(new CMDRunnable(addPathCommand)).start();
    }
}
