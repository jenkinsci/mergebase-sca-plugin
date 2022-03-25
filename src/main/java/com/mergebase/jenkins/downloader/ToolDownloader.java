package com.mergebase.jenkins.downloader;

import com.mergebase.jenkins.execption.MergebaseException;
import com.mergebase.jenkins.util.Util;
import io.jenkins.cli.shaded.org.slf4j.Logger;
import io.jenkins.cli.shaded.org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ToolDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(ToolDownloader.class);
    private static String JAR_URL = "https://mergebase.com/wrapper/mergebase.jar";

    /**
        Ensures the mergebase.jar wrapper has been downloaded and is available to the build step.

        @param wrapperPath The folder path where the tool downloader will check and download the tool
        if it's not present.
     */
    public static void ensureWrapperDownload(final String wrapperPath)  throws MergebaseException, IOException {
        File folder = new File(wrapperPath);
        File file = new File(wrapperPath + "/mergebase.jar");
        // set to actual file name
        if(file.exists()) {
            return;
        }
        try(InputStream is = httpGet(JAR_URL, false)) {
            folder.mkdirs();
            // set to actual file name
            streamToFile(is, file, false);
        } catch (Throwable e) {
            throw new MergebaseException(e);
        }
    }

    private static InputStream httpGet(String url, boolean debug) throws IOException {
        int httpCode;
        String httpMsg;
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setInstanceFollowRedirects(false);
        c.connect();
        httpCode = c.getResponseCode();
        httpMsg = c.getResponseMessage();
        if (httpCode == 200) {
            return c.getInputStream();
        } else {
            String msg = "Cannot connect to MergeBase server " + url + ", RESPONSE=" + httpCode + " - " + httpMsg;
            LOG.error(msg);
            throw new IOException(msg);
        }
    }

    public static long streamToFile(
            InputStream in, File file, boolean doClose
    ) throws IOException {
        return streamToFile(in, file, doClose, false);
    }

    public static long streamToFile(
            InputStream in, File file, boolean doClose, boolean doAppend
    ) throws IOException {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file, doAppend);
            return streamToOut(in, fout, doClose);
        } finally {
            if (fout != null) {
                fout.close();
            }
        }
    }

    public static long streamToOut(
            InputStream in, OutputStream out, boolean doClose
    ) throws IOException {
        byte[] buf = new byte[32768];
        long writeCount = 0;
        try {
            int read = -1;
            do {
                read = in.read(buf);
                if (read > 0) {
                    writeCount += read;
                    out.write(buf, 0, read);
                }
            } while (read >= 0);

        } finally {
            IOException ioe = null;
            try {
                out.flush();
                if (doClose) {
                    in.close();
                    out.close();
                    in = null;
                }
                out = null;
            } catch (IOException e) {
                ioe = e;
            }

            if (doClose) {
                Util.close(in, out);
            }
            if (ioe != null) {
                throw ioe;
            }
        }
        return writeCount;
    }

}
