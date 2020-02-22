package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static java.lang.Math.random;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Group root = new Group();
        Scene scene = new Scene(root, 1024, 576);
        scene.setFill(Color.rgb(50,50,50));

        /*
            玩家创建一个自己的实例
         */
        Player me = new Player(String.valueOf(Math.random()));
        Client client = new Client(me);

        /*
            战场
         */
        Battlefield battlefield = new Battlefield(500,500,client);
        battlefield.setLayoutX(25);

        /*
            技能板
         */
        Skillboard skill = new Skillboard(new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19},client);
        skill.setLayoutX(500);
        skill.setLayoutY(400);

        /*
            文字描述区
         */
        Describe describe = new Describe(client);
        describe.setLayoutX(480);
        describe.setLayoutY(25);

        /*
            用于设置音量等的按钮
         */
        Image setimage = new Image(getClass().getResourceAsStream("media\\setting.png"));
        Button setting = new Button();
        setting.setGraphic(new ImageView(setimage));
        setting.setBackground(new Background(new BackgroundFill(Color.rgb(50,50,50),null,null)));
        setting.setLayoutX(20);
        setting.setLayoutY(500);
        setting.setTooltip(new Tooltip("Setting"));

        /*
            用于退出的按钮
         */
        Image quitimage = new Image(getClass().getResourceAsStream("media\\quit.png"));
        Button quit = new Button();
        quit.setGraphic(new ImageView(quitimage));
        quit.setBackground(new Background(new BackgroundFill(Color.rgb(50,50,50),null,null)));
        quit.setLayoutX(20);
        quit.setLayoutY(20);
        quit.setTooltip(new Tooltip("Quit"));
        quit.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                battlefield.stop();
                skill.stop();
                describe.stop();
                primaryStage.close();
                System.exit(0);
            }
        });


        root.getChildren().add(battlefield);
        root.getChildren().add(describe);
        root.getChildren().add(skill);
        root.getChildren().add(setting);
        root.getChildren().add(quit);


        primaryStage.setTitle("Survive or Perish");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                battlefield.stop();
                skill.stop();
                describe.stop();
                System.exit(0);
            }
        });

        client.work();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
