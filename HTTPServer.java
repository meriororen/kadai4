import java.io.*;
import java.net.*;

/**
 * �ȒP��Web�T�[�o.
 */
public class HTTPServer
{
    /**
     * �T�[�o�\�P�b�g
     */
    private ServerSocket server;

    /**
     * Web�T�C�g�̃g�b�v�f�B���N�g��
     */
    private String docRoot;

    /**
     * �T�[�o�[�\�P�b�g���쐬����B
     * @param port ���̃T�[�o���󂯕t����|�[�g�ԍ�
     * @param docRoot Web�T�C�g�̃g�b�v�f�B���N�g��
     */
    private HTTPServer(int port, String docRoot) throws java.io.IOException {
        server = new ServerSocket(port);
        this.docRoot = docRoot;
    }

    /**
     *  �T�[�o�̃T�[�r�X���s�����\�b�h.
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
                // �X�^�b�N�g���[�X�̕\��
                e.printStackTrace();
            }
        }
    }
	
    /**
     *  �T�[�o�N���̂��߂� main���\�b�h.
     *  <pre>
     *  % java HTTPServer �|�[�g�ԍ�
     *  </pre>
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: java HTTPServer port_number");
            System.exit(1);
        }

        int port = -1;
        try {
            port = Integer.parseInt(args[0]); // ������𐮐��֕ϊ�
        }
        catch (Exception e) {
            System.err.println("Invalid port number.");
            System.exit(1);
        }

        // Web�T�C�g�̃g�b�v�f�B���N�g��(�J�����g�f�B���N�g����docroot)�����߂�.
        String docRoot = System.getProperty("user.dir") + File.separator + "docroot";

        try {
            HTTPServer server = new HTTPServer(port, docRoot);
            server.doService();
        }
        catch (Exception e) {
            // �X�^�b�N�g���[�X�̕\��
            e.printStackTrace();
        }
    }
}
