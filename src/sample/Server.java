package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import static java.lang.Math.random;


public class Server {
    public static SharedexText lock = new SharedexText();
    public static MyThread[] thread = new MyThread[10];
    public static PrintWriter[] pw = new PrintWriter[10];
    public static ObjectOutputStream[] oos = new ObjectOutputStream[10];
    public static int threadNum = 0;

    public static void main(String[] args) {
        sendthread.start();         //服务器端发送消息线程开始运行

        try {
            ServerSocket serverSocket = new ServerSocket(6918);

            /*
                循环创建玩家线程池
             */
            for (threadNum = 0; threadNum < 10; threadNum++) {
                Socket socket = serverSocket.accept();              //等待玩家连接
                pw[threadNum] = new PrintWriter(socket.getOutputStream());  //记录该线程的输出流端口
                oos[threadNum] = new ObjectOutputStream(socket.getOutputStream());
                thread[threadNum] = new MyThread(socket, threadNum);         //创建并开启玩家线程
                thread[threadNum].start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        服务端主线程：发送消息
     */
    public static Thread sendthread = new Thread(new Runnable() {
        @Override
        public void run() {
            String message = null;
            Work work = new Work();     //为了使用work的静态变量
            while (true) {
                message = Server.lock.getSharetext();   //循环接收互斥锁中发来的消息
                /*
                    事实证明，socket底层只有一个数据流，分开写是不现实的QAQ
                 */
                try {
                    /*
                        更新玩家位置
                    */
                    for (int i = 0; i < threadNum; i++) {   //枚举玩家
                        for (int j = 0; j < threadNum; j++) {

                            Player test = new Player(work.getPlayer(j));
                            //oos[i].writeObject(work.getPlayer(j));
                            //oos[i].flush();
                            System.out.println("确认玩家"+test.getName()+"的坐标为：("+test.getX()+","+test.getY()+")");

                            oos[i].writeObject(test);
                            oos[i].flush();
                        }
                    }

                    System.out.println("玩家对象传输完毕。");

                    if (!message.equals("0")) {
                        Player zero = new Player("null");

                        System.out.println(message + "开始回传。");

                        /*
                            处理文字
                         */
                        String[] piece = message.split("\\|");

                        for (int i = 0; i < threadNum; i++) {       //枚举玩家
                            String name = work.getPlayer(i).getName();
                            StringBuilder sb = new StringBuilder("");
                            for (String j : piece) {
                                if (j.equals(name)) sb.append("你"); //对于某玩家将其自己的名字换成“你”
                                else sb.append(j);
                            }
                            oos[i].writeObject(zero);
                            oos[i].flush();
                            pw[i].print(sb.toString() + "\n");        //把处理过得消息传回去
                            pw[i].flush();
                            System.out.println(sb.toString() + "已回传。");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}

/*
    接收玩家消息线程类
 */
class MyThread extends Thread {
    private Player player = null;   //该玩家实例
    private Socket socket = null;
    private InputStream is;
    private BufferedReader br;      //读字符
    private ObjectInputStream ois;  //读对象
    private OutputStream os = null;
    private Work work;              //work类的某个实例，与其他work实例共享同样的静态属性

    public MyThread(Socket fromserver,int number) {
        work = new Work();
        try {
            socket = fromserver;
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
            br = new BufferedReader(new InputStreamReader(is));
            os = socket.getOutputStream();

            player = (Player) ois.readObject(); //读入客户端最先发送的玩家实例
            System.out.print("玩家"+player.getName()+"的实例已经被线程接收。");

        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        player.setID(number);
        work.addPlayer(player,number);     //将玩家实例加入work类的静态属性里
        System.out.println("新玩家已成功连接。");
        System.out.println("新玩家的坐标为("+player.getX()+","+player.getY()+")");
    }

    public void run() {
        System.out.println("新玩家对应线程已开始运行。");
        try {
            String recieve = null;
            while ((recieve = br.readLine()) != null) {
                System.out.println(recieve+"已收到。");
                if(recieve.length() > 1){
                    work.send(new StringBuilder(player.getName()).append("|说道：").append(recieve).toString());
                }
                else    work.handle(player.getID(),MyForm.getWhich(recieve));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

/*
    处理游戏逻辑的共享类
 */
class Work {
    private static Player[] players = new Player[10];   //对应线程池的十个玩家
    private static int playerNum = 0;   //已连接玩家数

    public Work() { }

    /*
        向静态属性中添加一个玩家
     */
    public void addPlayer(Player player,int id) {
        players[playerNum] = player;
        players[playerNum].setID(id);    //此处ID和线程池中玩家位置编号相同
        playerNum++;
        send("0");  //通知其他玩家更新信息
    }

    /*
        从静态属性中查询一个玩家，给出线程池位置编号
     */
    public Player getPlayer(int which) {
        for(Player i : players) {
            if(i.getID() == which)  return i;
        }
        return null;
    }
    private int getPlayeri(int which) {
        for(int i=0;i<playerNum;i++) {
            if(players[i].getID() == which)  return i;
        }
        return 0;
    }

    /*
        将玩家发送来的操作处理成回传信息
        which 是线程位置编号
     */
    public void handle(int which,int operate) {
        String tosend = new StringBuilder(getPlayer(which).getName()).append("|使用了未定义的技能。").toString();
        StringBuilder doname = new StringBuilder(getPlayer(which).getName());
        int i;
        double px,py,pface;
        switch(operate) {
            case -2:        //玩家向前移动
                i = getPlayeri(which);
                px = players[i].getX();
                py = players[i].getY();
                pface = players[i].getFace();
                px += 2.0*Math.cos(pface);
                py += 2.0*Math.sin(pface);
                players[i].setXandY(px,py);
                System.out.println("玩家"+players[i].getName()+"的坐标已更新为("+px+","+py+")");
                break;
            case -3:        //玩家向后移动
                i = getPlayeri(which);
                px = players[i].getX();
                py = players[i].getY();
                pface = players[i].getFace();
                px -= 2.0*Math.cos(pface);
                py -= 2.0*Math.sin(pface);
                players[i].setXandY(px,py);
                System.out.println("玩家"+players[i].getName()+"的坐标已更新为("+px+","+py+")");
                break;
            case -4:        //玩家向左转
                i = getPlayeri(which);
                pface = players[i].getFace();
                pface -= 0.5;
                players[i].setFace(pface);
                break;
            case -5:        //玩家向右转
                i = getPlayeri(which);
                pface = players[i].getFace();
                pface += 0.5;
                players[i].setFace(pface);
                break;
            case 0:
                tosend = doname.append("|出拳。").toString();
                break;
            case 1:
                tosend = doname.append("|出腿。").toString();
                break;
            case 2:
                tosend = doname.append("|发出无敌大招。").toString();
                break;
            case 3:
                tosend = doname.append("|自己把自己摔了一跤。").toString();
                break;
            case 4:
                tosend = doname.append("|抛了个媚眼。").toString();
                break;
            case 5:
                tosend = doname.append("|这句话用来测试长度，看看文字区能否换行。如果不能，那我就要哭瞎了，绝对。绝对哭瞎。现在应该够长了吧。").toString();
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 11:
                break;
            case 12:
                break;
            case 13:
                break;
            case 14:
                break;
            case 15:
                break;
            case 16:
                break;
            case 17:
                break;
            case 18:
                break;
            case 19:
                break;
            default:
                //tosend = new String("...");
        }
        if(operate >= 0)    send(tosend);
        else    send("0");
    }

    /*
        通过互斥锁与发送消息线程通信
     */
    public void send(String sendthing) {
        Server.lock.addSharetext(sendthing);
    }
}
/*
class Player implements Serializable {
    private String name;
    private int ID;
    private double red,green,blue;
    private int HP;
    private int MP;
    private double face;
    private int x,y;

    public Player(int id) {
        super();
        name = new String("haha");
        ID = id;
        red = 1;green = 0;blue = 0;
        HP = 100;
        MP = 100;
        x = 0;
        y = 0;
        face = random()*6.28;
    }

    public Player(String name) {
        super();
        this.name = name;
        red = 1;green = 0;blue = 0;
        HP = 100;
        MP = 100;
        x = 0;
        y = 0;
        face = random()*6.28;
    }

    public void draw(GraphicsContext gc, int ox, int oy) {
        gc.setFill(Color.color(red,green,blue));
        gc.fillOval(ox+x-6,oy+y-6,12,12);
        gc.setFill(Color.WHITE);
        gc.fillOval(ox+x+6*Math.cos(face)-3,oy+y+6*Math.sin(face)-3,6,6);

        gc.setFill(Color.RED);
        gc.fillRect(ox+x-20,oy+y-20,HP/100*40,2);
        gc.setFill(Color.BLUE);
        gc.fillRect(ox+x-21,oy+y-16,MP/100*40,2);

        gc.fillText(name,ox+x-20,oy+y-30);
    }

    public Color getColor() {
        return Color.color(red,green,blue);
    }
    public int getHP() {
        return HP;
    }
    public int getMP() {
        return MP;
    }
    public int getY() {
        return y;
    }
    public int getX() {
        return x;
    }
    public double getFace() {
        return face;
    }
    public int getID() {
        return ID;
    }
    public String getName() {
        return name;
    }

    public void setColor(Color color) {
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }
    public void setHP(int HP) {
        this.HP = HP;
    }
    public void setMP(int MP) {
        this.MP = MP;
    }
    public void setXandY(int x,int y) {
        int dis = x*x+y*y;
        while(dis > 39000) {
            x--;
            y--;
            dis = x*x+y*y;
        }
        this.x = x;
        this.y = y;
    }
    public void setFace(double face) {
        while(face > 6.28)  face -= 6.28;
        this.face = face;
    }
    public void setID(int id) {
        ID = id;
    }
}
*/
/*
class MyForm {
    public static int[] time = new int[]{10,10,10,10,10,10,10,10,10,10,
            10,10,10,10,10,10,10,10,10,10,
            10,10,10,10,10,10,10,10,10,10,
            10,10,10,10,10,10,10,10,10,10,
            10,10,10,10,10,10,10,10,10,10};

    public static String[] name = new String[]{"T","Y","U","I","O","P","[","G","H","J","K","L",";","V","B","N","M",",",".","/"};

    public static KeyCode[] keycode = new KeyCode[]{KeyCode.T,KeyCode.Y,KeyCode.U,KeyCode.I,KeyCode.O,KeyCode.P,KeyCode.OPEN_BRACKET,
            KeyCode.G,KeyCode.H,KeyCode.J,KeyCode.K,KeyCode.L,KeyCode.SEMICOLON,
            KeyCode.V,KeyCode.B,KeyCode.N,KeyCode.M,KeyCode.COMMA,KeyCode.PERIOD,KeyCode.SLASH};

    public static int[] placeX = new int[]{0,20,40,60,80,100,120,
            10,30,50,70,90,110,
            0,20,40,60,80,100,120};

    public static int[] placeY = new int[]{0,0,0,0,0,0,0,
            30,30,30,30,30,30,
            60,60,60,60,60,60,60};

    public MyForm() { }

    public static int getTime(int which) {
        return time[which];
    }
    public static String getName(int which) {
        return name[which];
    }
    public static int getWhich(KeyCode k) {
        int re = 0;
        for(KeyCode i : keycode){
            if(i == k)  return re;
            re++;
        }
        return -1;
    }
    public static int getWhich(String operate) {
        int re = 0;
        for(String i : name){
            if(i.equals(operate))   return re;
            re++;
        }
        return 0;
    }
    public static int getPlaceX(int which) {
        return placeX[which];
    }
    public static int getPlaceY(int which) {
        return placeY[which];
    }
}

*/
class SharedexText {
    private String text;
    private boolean flag = false;

    public synchronized void addSharetext(String message){
        if(flag){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Thread Interrupted Exception,"+e.getMessage());
            }
        }
        //System.out.println("送出没卡。");
        text = message;
        flag = true;
        notify();
    }

    public synchronized String getSharetext(){
        if(!flag){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Thread Interrupted Exception,"+e.getMessage());
            }
        }
        //System.out.println("接收没卡。");
        flag = false;
        notify();
        return text;
    }
}

