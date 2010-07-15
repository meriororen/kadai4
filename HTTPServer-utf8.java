import java.io.*;
import java.net.*;

/**
 * 簡単なWebサーバ.
 */
public class HTTPServer
{
    /**
     * サーバソケット
     */
    private ServerSocket server;

    /**
     * Webサイトのトップディレクトリ
     */
    private String docRoot;

    /**
     * サーバーソケットを作成する。
     * @param port このサーバが受け付けるポート番号
     * @param docRoot Webサイトのトップディレクトリ
     */
    private HTTPServer(int port, String docRoot) throws java.io.IOException {
        server = new ServerSocket(port);
        this.docRoot = docRoot;
    }

    /**
     *  サーバのサービスを行うメソッド.
     */
    private void doService() {
        System.out.println("Starting Server Service!!");
        while (true) {
            try {
                Socket client = server.accept();
                ClientHandler clientHandler = new ClientHandler(client, docRoot);
				Thread thread = new Thread(clientHandler);
				thread.start();	
                //clientHandler.doService();
            }
            catch (Exception e) {
                // スタックトレースの表示
                e.printStackTrace();
            }
        }
    }
	
    /**
     *  サーバ起動のための mainメソッド.
     *  <pre>
     *  % java HTTPServer ポート番号
     *  </pre>
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: java HTTPServer port_number");
            System.exit(1);
        }

        int port = -1;
        try {
            port = Integer.parseInt(args[0]); // 文字列を整数へ変換
        }
        catch (Exception e) {
            System.err.println("Invalid port number.");
            System.exit(1);
        }

        // Webサイトのトップディレクトリ(カレントディレクトリのdocroot)を求める.
        String docRoot = System.getProperty("user.dir") + File.separator + "docroot";

        try {
            HTTPServer server = new HTTPServer(port, docRoot);
            server.doService();
        }
        catch (Exception e) {
            // スタックトレースの表示
            e.printStackTrace();
        }
    }
}
