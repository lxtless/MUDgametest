package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;

import static java.lang.Math.random;

public class Player implements Serializable {
    private String name;
    private int ID;
    private double red,green,blue;
    private int HP;
    private int MP;
    private double face;
    private double x,y;

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
        x = random()*20;
        y = random()*20;
        face = random()*6.28;
    }

    public Player(Player copy) {
        super();
        name = copy.getName();
        ID = copy.getID();
        red = copy.getColor().getRed();
        green = copy.getColor().getGreen();
        blue = copy.getColor().getBlue();
        HP = copy.getHP();
        MP = copy.getMP();
        x = copy.getX();
        y = copy.getY();
        face = copy.getFace();
    }

    public void draw(GraphicsContext gc,int ox,int oy) {
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
    public double getY() {
        return y;
    }
    public double getX() {
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
    public void setXandY(double x,double y) {
        double dis = x*x+y*y;
        while(dis > 39000) {
            x -= 1;
            y -= 1;
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
