package sample;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.Math.random;

public class Client{
    private static Player[] players = new Player[10];
    private static int playernum;
    private Socket socket;
    private OutputStream os;
    private PrintWriter pw;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private InputStream is;
    private BufferedReader br;
    private boolean running;

    public static Sharedtext lock = new Sharedtext();

    /*
        用于转移client实例的空构造
     */
    public Client() { }

    /*
        用一个玩家实例创建一个客户端类
     */
    public Client(Player me) {
        try {
            //socket = new Socket("47.94.140.223",6918);
            socket = new Socket("127.0.0.1",6918);

            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);   //对象输出
            pw = new PrintWriter(os);           //字符流输出

            is = socket.getInputStream();
            ois = new ObjectInputStream(is);    //对象读入
            br = new BufferedReader(new InputStreamReader(is)); //字符流读入

            /*
                与服务器连接后先发送玩家实例
             */
            oos.writeObject(me);
            oos.flush();
            players[0] = me;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
        playernum = 1;
    }

    public void work() {
        playerthread.start();
    }

    public void sendmessage(String message) {
        pw.print(message+"\n");
        pw.flush();
        System.out.println(message+"已发送至服务器。");
    }

    public void stop() {
        running = false;
        try {
            socket.shutdownInput();
            oos.close();
            os.close();
            is.close();
            br.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
         接收服务器发送的玩家消息和文字消息
     */
    private Thread playerthread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Player temp = null;
                while (((temp = (Player) ois.readObject()) != null) && running) {
                    System.out.println("接收玩家"+temp.getName()+"的坐标为：("+temp.getX()+","+temp.getY()+")");

                    if(temp.getName().equals("null")) {
                        lock.addSharetext(br.readLine());
                        continue;
                    }

                    int idnum = temp.getID();
                    int i;
                    for (i = 0; i < playernum; i++) {
                        if (players[i].getID() == idnum) {
                            flag = true;
                            players[i] = temp;
                            System.out.println("更新了玩家"+temp.getName()+"的信息。");
                            System.out.println("玩家"+temp.getName()+"的坐标已更新为("+temp.getX()+","+temp.getY()+")");
                            flag = false;
                            break;
                        }
                    }
                    if (i == playernum) {
                        flag = true;
                        players[playernum] = temp;
                        playernum++;
                        System.out.println("有新玩家加入，当前玩家总数为："+playernum);
                        flag = false;
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    });

    private boolean flag = false;

    public int getPlayernum() {
        return playernum;
    }

    public Player getPlayers(int which) {
        while(flag);
        return players[which];
    }
}
