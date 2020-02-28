package com.cn.wavetop.dataone.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

import java.io.*;

/**
 * @Author yongz
 * @Date 2019/12/31、10:01
 */
public class SCPClientUtil extends SCPClient {


    Connection conn;

    class LenNamePair {
        long length;
        String filename;
    }

    public SCPClientUtil(Connection conn) {
        super(conn);
        this.conn = conn;
    }

    public void get(String remoteFile, PrintWriter target) throws IOException {
        get(new String[]{remoteFile}, new PrintWriter[]{target});
    }

    private void get(String remoteFiles[], PrintWriter[] targets) throws IOException {
        Session sess = null;

        if ((remoteFiles == null) || (targets == null))
            throw new IllegalArgumentException("Null argument.");

        if (remoteFiles.length != targets.length)
            throw new IllegalArgumentException("Length of arguments does not match.");

        if (remoteFiles.length == 0)
            return;

        String cmd = "scp -f";

        for (int i = 0; i < remoteFiles.length; i++) {
            if (remoteFiles[i] == null)
                throw new IllegalArgumentException("Cannot accept null filename.");

            String tmp = remoteFiles[i].trim();

            if (tmp.length() == 0)
                throw new IllegalArgumentException("Cannot accept empty filename.");

            cmd += (" " + tmp);
        }

        try {
            sess = conn.openSession();
            sess.execCommand(cmd);
            receiveFiles(sess, targets);
        } catch (IOException e) {
            throw (IOException) new IOException("Error during SCP transfer.").initCause(e);
        } finally {
            if (sess != null)
                sess.close();
        }
    }

    private void receiveFiles(Session sess, PrintWriter[] targets) throws IOException {
        byte[] buffer = new byte[8192];

        OutputStream os = new BufferedOutputStream(sess.getStdin(), 512);
        InputStream is = new BufferedInputStream(sess.getStdout(), 40000);

        os.write(0x0);
        os.flush();

        for (int i = 0; i < targets.length; i++) {
            LenNamePair lnp = null;

            while (true) {
                int c = is.read();
                if (c < 0)
                    throw new IOException("Remote scp terminated unexpectedly.");

                String line = receiveLine(is);

                if (c == 'T') {
                    /* Ignore modification times */

                    continue;
                }

                if ((c == 1) || (c == 2))
                    throw new IOException("Remote SCP error: " + line);

                if (c == 'C') {
                    lnp = parseCLine(line);
                    break;

                }
                throw new IOException("Remote SCP error: " + ((char) c) + line);
            }

            os.write(0x0);
            os.flush();
            File f = new File("temporary.log");
            FileOutputStream fop = null;
            long remain = lnp.length;
            if (lnp.length>800000){
                remain = 800000;
            }

            fop = new FileOutputStream(f);

            while (remain > 0) {
                int trans;
                if (remain > buffer.length)
                    trans = buffer.length;
                else
                    trans = (int) remain;

                int this_time_received = is.read(buffer, 0, trans);

                if (this_time_received < 0) {
                    throw new IOException("Remote scp terminated connection unexpectedly");
                }

                fop.write(buffer, 0, this_time_received);

                remain -= this_time_received;
            }
//            readResponse(is);
            is.close();
            fop.close();

            os.write(0x0);
            os.flush();

            // 读取文件给前端页面
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
            String str;
            Integer index = 0;
            while ((str = bufferedReader.readLine()) != null) {
                if (index > 10000){
                    break;
                }
                targets[i].println(new StringBuffer(str).append("|").toString());
                index++;
            }
            bufferedReader.close();
            targets[i].flush();
            targets[i].close();
            f.delete();

        }
    }


    private LenNamePair parseCLine(String line) throws IOException {
        /* Minimum line: "xxxx y z" ---> 8 chars */

        long len;

        if (line.length() < 8)
            throw new IOException("Malformed C line sent by remote SCP binary, line too short.");

        if ((line.charAt(4) != ' ') || (line.charAt(5) == ' '))
            throw new IOException("Malformed C line sent by remote SCP binary.");

        int length_name_sep = line.indexOf(' ', 5);

        if (length_name_sep == -1)
            throw new IOException("Malformed C line sent by remote SCP binary.");

        String length_substring = line.substring(5, length_name_sep);
        String name_substring = line.substring(length_name_sep + 1);

        if ((length_substring.length() <= 0) || (name_substring.length() <= 0))
            throw new IOException("Malformed C line sent by remote SCP binary.");

        if ((6 + length_substring.length() + name_substring.length()) != line.length())
            throw new IOException("Malformed C line sent by remote SCP binary.");

        try {
            len = Long.parseLong(length_substring);
        } catch (NumberFormatException e) {
            throw new IOException("Malformed C line sent by remote SCP binary, cannot parse file length.");
        }

        if (len < 0)
            throw new IOException("Malformed C line sent by remote SCP binary, illegal file length.");

        LenNamePair lnp = new LenNamePair();
        lnp.length = len;
        lnp.filename = name_substring;

        return lnp;
    }


    private String receiveLine(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer(30);

        while (true) {
            /* This is a random limit - if your path names are longer, then adjust it */

            if (sb.length() > 8192)
                throw new IOException("Remote scp sent a too long line");

            int c = is.read();

            if (c < 0)
                throw new IOException("Remote scp terminated unexpectedly.");

            if (c == '\n')
                break;

            sb.append((char) c);

        }
        return sb.toString();
    }

    private void readResponse(InputStream is) throws IOException {
        int c = is.read();

        if (c == 0)
            return;

        if (c == -1)
            throw new IOException("Remote scp terminated unexpectedly.");

        if ((c != 1) && (c != 2))
            throw new IOException("Remote scp sent illegal error code.");

        if (c == 2)
            throw new IOException("Remote scp terminated with error.");

        String err = receiveLine(is);
        throw new IOException("Remote scp terminated with error (" + err + ").");
    }
}
