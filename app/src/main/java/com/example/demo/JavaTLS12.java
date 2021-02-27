package com.example.demo;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

// Java 11
public class JavaTLS12 {

    private static final String[] protocols = new String[]{"TLSv1.2"};
    private static final String[] cipher_suites = new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"};

    public static void main(String[] args) throws Exception {

        SSLSocket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            SSLSocketFactory factory =
                    (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket =
                    (SSLSocket) factory.createSocket("www.sogou.com", 443);

            socket.setEnabledProtocols(protocols);
//            socket.setEnabledCipherSuites(cipher_suites);

            socket.startHandshake();

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())));

            out.println("GET / HTTP/1.1");
            out.println("User-Agent: PostmanRuntime/7.26.8");
            out.println("Accept: */*");
            out.println("Cache-Control: no-cache");
            out.println("Postman-Token: 04443a9b-c771-40eb-8746-5745996caad3");
            out.println("Accept-Encoding: gzip, deflate, br");
            out.println("Connection: keep-alive");
            out.println();
            out.flush();

            if (out.checkError())
                System.out.println("SSLSocketClient:  java.io.PrintWriter error");

            /* read response */
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
            if (out != null)
                out.close();
            if (in != null)
                in.close();
        }
    }

}