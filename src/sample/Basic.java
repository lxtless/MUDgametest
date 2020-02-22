package sample;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import static java.lang.Math.random;

class Describe extends ScrollPane {
    private Group words;
    private Text[] text = new Text[200];
    private int textnum;
    private Client client;
    private boolean running;

    public Describe(Client c) {
        words = new Group();
        setBackground(new Background(new BackgroundFill(Color.rgb(50,50,50),null,null)));
        getStylesheets().add(getClass().getResource("Describe.css").toExternalForm());
        setPrefSize(500,300);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.NEVER);

        textnum = 0;

        text[textnum] = new Text("欢迎登录Survive or Perish！");
        text[textnum].setFill(Color.color(random(),random(),random()));
        text[textnum].setLayoutY(textnum*20);
        words.getChildren().add(text[textnum]);
        textnum++;

        for(int i=1;i<100;i++){
            text[i] = new Text();
            text[i].setLayoutY(i*20);
            words.getChildren().add(text[i]);
        }

        setContent(words);
        client = c;

        running = true;
        thread.start();
    }

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(running) {
                try {
                    text[textnum].setText(client.lock.getSharetext());
                    text[textnum].setFill(Color.color(random(), random(), random()));
                    //text[textnum].setLayoutY(textnum * 20);

                    textnum++;
                    Platform.runLater(() -> {
                        setVvalue((textnum-20)*0.01);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public void stop() {
        running = false;
    }
}

class Battlefield extends Canvas {
    private Client client;
    private GraphicsContext gc;
    private long sleep = 200;
    private boolean running = false;

    public Battlefield(double width,double height,Client c) {
        super(width,height);
        gc = getGraphicsContext2D();
        client = c;

        running = true;
        thread.start();
    }

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(running) {
                updateAndDraw();
                try {
                    Thread.sleep(sleep);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    });

    public void updateAndDraw() {
        gc.setFill(Color.rgb(55,55,55));
        gc.fillOval(10,40,400,400);
        gc.setStroke(Color.color(0.8,0.6,0.4));
        gc.strokeOval(10,40,400,400);

        int playernum = client.getPlayernum();
        Player player = null;
        for(int i=0;i<playernum;i++) {
            player = client.getPlayers(i);
            player.draw(gc,205,220);
        }
    }

    public void stop() {
        running = false;
    }
}

class Skillboard extends Pane {
    private MyButton[] buttons = new MyButton[20];
    private Client client;

    private int sleep = 50;
    private boolean running;
    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(running) {
                for(int i=0;i<20;i++){
                    buttons[i].setWait(1);
                    if(buttons[i].getWait() >= MyForm.getTime(buttons[i].getWhich()))
                        buttons[i].release();
                }
                try {
                    Thread.sleep(sleep);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    });

    public Skillboard(int[] skillid,Client c) {
        client = c;

        int temp = 0;
        for(int i : skillid){
            buttons[temp] = new MyButton(i,temp);
            buttons[temp].setText(MyForm.getName(i));
            buttons[temp].setLayoutX(MyForm.getPlaceX(temp));
            buttons[temp].setLayoutY(MyForm.getPlaceY(temp));
            getChildren().add(buttons[temp]);
            temp++;
        }

        setFocusTraversable(true);
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                int which = MyForm.getWhich(event.getCode());
                if(which != -1){
                    if(which >= 0) {
                        buttons[which].hit();
                    }
                    client.sendmessage(MyForm.getName(which));
                    System.out.println("按下"+MyForm.getName(which)+"键。");
                }
            }
        });

        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }
}

class MyButton extends Button {
    private int which;
    private int number;
    private int wait;
    private boolean used;

    public MyButton(int num,int id) {
        number = num;
        wait = 0;
        used = false;
        which = id;
        setBackground(new Background(new BackgroundFill(Color.rgb(69,69,69),null,null)));

        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hit();
            }
        });
    }

    public void release() {
        wait = 0;
        used = false;
        changecolor(Color.rgb(69,69,69));
    }
    public void hit() {
        if(used)    return ;
        used = true;
        changecolor(Color.rgb(100,20,20));

        //client.sendmessage(MyForm.getName(which));
        //System.out.println("按下"+MyForm.getName(which)+"键。");
    }
    private void changecolor(Color color) {
        setBackground(new Background(new BackgroundFill(color,null,null)));
    }

    public void setWait(int plus) {
        wait += plus;
    }
    public int getWait() {
        return wait;
    }
    public int getWhich() {
        return which;
    }
}

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
        if(which == -2) return "W";
        if(which == -3) return "S";
        if(which == -4) return "A";
        if(which == -5) return "D";
        return name[which];
    }
    public static int getWhich(KeyCode k) {
        int re = 0;
        for(KeyCode i : keycode){
            if(i == k)  return re;
            re++;
        }
        if(k == KeyCode.W)  return -2;
        if(k == KeyCode.S)  return -3;
        if(k == KeyCode.A)  return -4;
        if(k == KeyCode.D)  return -5;
        return -1;
    }
    public static int getWhich(String operate) {
        int re = 0;
        for(String i : name){
            if(i.equals(operate))   return re;
            re++;
        }
        if(operate.equals("W"))  return -2;
        if(operate.equals("S"))  return -3;
        if(operate.equals("A"))  return -4;
        if(operate.equals("D"))  return -5;
        return 0;
    }
    public static int getPlaceX(int which) {
        return placeX[which];
    }
    public static int getPlaceY(int which) {
        return placeY[which];
    }
}

class Sharedtext {
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
