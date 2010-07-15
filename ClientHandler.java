import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Webクライアントと通信を行うクラス.
 */
class ClientHandler implements Runnable
{
    /**
     * Webクライアントと通信するためのソケット.
     */
    private Socket client;

    /**
     * Webクライアントからの読み込みに使う文字ストリーム.
     */
    private BufferedReader iReader;

    /**
     * Webクライアントへの出力に使うバイトストリーム.
     */
    private OutputStream oStream;

    /**
     * Webサイトのトップディレクトリ.
     */
    private String docRoot;

    /**
     * HTTPレスポンス中の行末コード.
     */
    private final String CRLF = "\r\n";

    /**
     * socket は ServerSocket の accept() メソッドから取得したソケット。
     * @param socket クライアントとの間で保持されているソケット
     * @param docRoot   ドキュメントが置いてあるトップのディレクトリ
     */
    ClientHandler(Socket socket, String docRoot) throws java.io.IOException {
        client = socket;
        InputStream iStream = socket.getInputStream();
        iReader = new BufferedReader(new InputStreamReader(iStream));
        oStream = socket.getOutputStream();
        this.docRoot = docRoot;
    }

    /**
     * 1行分の文字列をクライアントへ送信する. 行末コードも同時に出力される.
     * @param line CRLFを含まない文字列.
     */
    private void sendln(String line) throws java.io.IOException {
        oStream.write(line.getBytes());
        oStream.write(CRLF.getBytes());
    }

    /**
     * 行末コードだけをクライアントへ送信する.
     */
    private void sendln() throws java.io.IOException {
        oStream.write(CRLF.getBytes());
    }

    /**
     * ステータスコード400とエラーページを送信する.
     * エラーページが表示されるかどうかは, Webブラウザに依存する.
     */
    private void sendStatusCode400() throws java.io.IOException {
        String body = "<html><head><title>400</title></head><body>400 Bad Request</body></html>";
        sendln("HTTP/1.0 400 Bad Request"); // ステータス行
        sendln("Content-Type: text/html");
        sendln("Content-Length: " + body.getBytes().length);
        sendln(); // ヘッダの終り
        sendln(body); // Webページ
    }

    /**
     * ステータスコード500とエラーページを送信する.
     * エラーページが表示されるかどうかは, Webブラウザに依存する.
     */
    private void sendStatusCode500() throws java.io.IOException {
        String body = "<html><head><title>500</title></head><body>500 Internal Server Error</body></html>";
        sendln("HTTP/1.0 500 Internal Server Error"); // ステータス行
        sendln("Content-Type: text/html");
        sendln("Content-Length: " + body.getBytes().length);
        sendln(); // ヘッダの終り
        sendln(body); // Webページ
    }

	private void sendStatusCode404() throws java.io.IOException {
        String body = "<html><head><title>404</title></head><body><h1>404 Error</h1> There's no such file.</body></html>";
        sendln("HTTP/1.0 404 File Not Found."); // ステータス行
        sendln("Content-Type: text/html");
        sendln("Content-Length: " + body.getBytes().length);
        sendln(); // ヘッダの終り
        sendln(body); // Webページ
    }
	
	private int getFileLength(String localPath) throws java.io.IOException {
		FileInputStream fis = new FileInputStream(localPath);
		byte[] b = new byte[1];
		int len = 0;
		while(fis.read(b) != -1){
			len++;
		}
		return len;
	}
			
	private void sendFile(String localPath, int size) throws java.io.IOException {
		FileInputStream fis = new FileInputStream(localPath);
		byte[] b = new byte[size];
		while((fis.read(b)) != -1){
			oStream.write(b);
		}
	}
	
	private String getContentType(String localPath) throws java.io.IOException {
		String contentType = null;
		
		if(localPath.matches(".*.html")){
			contentType = "text/html";
		}else if(localPath.matches(".*.pdf")){
			contentType = "application/pdf";
		}else if(localPath.matches(".*.jpg")){
			contentType = "image/jpeg";
		}else if(localPath.matches(".*.txt")){
			contentType = "text/plain";
		}else{
			//TODO
		}
		return contentType;
	}
	
	/**
	 * status OK
	 *
	 */
    private void sendStatusCode200(String localPath) throws java.io.IOException {
		int contentLength = getFileLength(localPath);
		String contentType = getContentType(localPath);
				
		sendln("HTTP/1.0 200 OK"); // ステータス行
        sendln("Content-Type: " + contentType);
        sendln("Content-Length: " + contentLength);
        sendln(); // ヘッダの終り
		sendFile(localPath, contentLength); // Webページ
    }
	
	private String directoryPath(String path) throws java.io.IOException {
		if((new File(docRoot + path)).isDirectory()){
			return path + "index.html";
		}
		return path;
	}
	
	public void run(){
		try{
			doService();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    /**
     * クライアントとの通信を行う.
     */
    public void doService() {
        try {
            // リクエスト行を読む
            String requestLine = iReader.readLine();

            // ログを出力する
            System.out.println("log: " + requestLine);

            // リクエスト行を解析する.
            //  -- リクエスト行を空白" "で区切る.
            StringTokenizer st = new StringTokenizer(requestLine, " ");
            String method = null;  // メソッド
            String path = null;    // パス
            String version = null; // HTTPのバージョン

            try {
                method = st.nextToken();
                path = st.nextToken();
                version  = st.nextToken();
			}catch (NoSuchElementException e) {
                // リクエスト行が「メソッド パス HTTPのバージョン」の形式ではなかった.
                sendStatusCode400();

                // 入出力の後始末
                oStream.close();
                client.close();
                return;
            }
			
			path = directoryPath(path);
			
            // ファイルシステム上でのパスを求める.
            String localPath = null;
            localPath = docRoot + path.replace('/', File.separatorChar);
			
            // ヘッダを読み飛ばす
            while (!"".equals(iReader.readLine())) {
            }

            // メソッドの処理
            if (method.equals("GET")) {
                // GETメソッドの処理
                // 本来は, ヘッダとlocalPathで指されるファイルを出力する.
				try{
					sendStatusCode200(localPath);
				}catch(FileNotFoundException e){
					sendStatusCode404();
				}
				
				// 入出力の後始末
                oStream.close();
                client.close();
                return;
            }else {
                // サポートしていないメソッドへの処理
                sendStatusCode500();
                // 入出力の後始末
                oStream.close();
                client.close();
                return;
            }
        }
        catch (Exception e) {
            // スタックトレースの表示
            e.printStackTrace();
        }
    }
}
