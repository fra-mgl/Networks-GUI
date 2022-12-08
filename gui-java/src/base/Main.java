package base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {

    private static final double networkDim = 750.0;
    private static final double textWidth = 350.0;
    private static final double buttonsHeight = 50.0;
    private static final double windowWidth = networkDim +  textWidth;
    private static final double windowHeight = networkDim + buttonsHeight;
    private static final int HBoxPadding = 20;


    public void addGuiElement(Pane pane, NetItem item){
        pane.getChildren().add((item));
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Button bAddHost = new Button("Add host");
        Button bAddSwitch = new Button("Add switch");
        Button bRefresh = new Button("Refresh");
//        button.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                System.out.println("Hello GUI!");
//            }
//        });
//        BorderPane layout = new BorderPane();

        /* BUTTONS */
        HBox buttonsBox = new HBox(bRefresh, bAddHost, bAddSwitch);
        buttonsBox.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setSpacing(50);
        buttonsBox.setMinHeight(buttonsHeight);
        buttonsBox.setMaxHeight(buttonsHeight);

        /* SPECS TEXT BOX */

        Text field0 = new Text();
        Text field1 = new Text();
        Text field2 = new Text();
        Text field3 = new Text();
        Text field4 = new Text();
        Text field5 = new Text();
        VBox title = new VBox();
        VBox texts = new VBox();
        title.getChildren().add(field0);
        texts.getChildren().addAll(field1, field2, field3, field4, field5);
//        texts.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        title.setAlignment(Pos.CENTER);
        texts.setAlignment(Pos.CENTER_LEFT);
//        field0.setFont(new Font());
        texts.setPadding(new Insets(HBoxPadding, 0, HBoxPadding, 0));
        texts.setSpacing(50);
        VBox specs = new VBox(field0, texts);

        /* LAYOUT */
        GridPane layout = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMinWidth(networkDim);
        col1.setMaxWidth(networkDim);
        col2.setMinWidth(textWidth);
        col2.setMaxWidth(textWidth);
        layout.getColumnConstraints().addAll(col1, col2);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row1.setMinHeight(networkDim);
        row1.setMaxHeight(networkDim);
        row2.setMinHeight(buttonsHeight);
        row2.setMaxHeight(buttonsHeight);
        layout.getRowConstraints().addAll(row1, row2);
        Network network = new Network(networkDim, networkDim);
        layout.add(network.netStack, 0,0);
        layout.add(texts, 1,0);
        layout.add(buttonsBox, 0,1);



//        prova.setCoordinates(100.0, 50.0);
//        network.addHost(prova);

//        List<Router> p = new ArrayList<>();
//        p.add(new Router("1", 10.0));
//        p.add(new Router("2", 20.0));
//        p.get(0).setAngle(30.0);
//        System.out.println(p.get(0).getAngle());

//        String s = "{'dpid': '0000000000000007', 'ports': [{'dpid': '0000000000000007', 'port_no': '00000001', 'hw_addr': '96:10:5e:db:0c:d3', 'name': 's7-eth1'}, {'dpid': '0000000000000007', 'port_no': '00000002', 'hw_addr': 'c6:e5:b1:06:38:85', 'name': 's7-eth2'}, {'dpid': '0000000000000007', 'port_no': '00000003', 'hw_addr': 'ae:99:21:7c:e0:bb', 'name': 's7-eth3'}]}";
//
//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .create();
//        Router r0 = gson.fromJson(s, Router.class);
//        Router r0 = RestAPI.getRouter();
//        List<Router> listRouter = Arrays.asList(RestAPI.getRouter());
//        if (listRouter != null) {
//            for (int i = 0; i < listRouter.size(); i++) {
//                network.addRouter(listRouter.get(i));
//            }
//        }else{
//            return 1;
//        }

//        System.out.println(network.routerList);

//        Router r0 = list.get(0);
//        Router r1 = new Router();
//        Router r2 = new Router();
//        Router r3 = new Router();
//        System.out.println(r0);
//
//        network.addRouter(r0);
//        network.addRouter(r1);
//        network.addRouter(r2);
//        network.addRouter(r3);
//
//        network.addHost(new Host("H1"), r0);
//        network.addHost(new Host("H1"), r1);
//        network.addHost(new Host("H1"), r2);
//        network.addHost(new Host("H1"), r2);
//        network.addHost(new Host("H1"), r3);
//        network.addHost(new Host("H1"), r3);
//        network.addHost(new Host("H1"), r3);
//
//        r0.addRouterLink(r1);
//        r1.addRouterLink(r2);
//        r1.addRouterLink(r3);
//        r2.addRouterLink(r3);

//        Router r0 = network.routerList.get(0);
//        Router r1 = network.routerList.get(1);
//        network.addHost(new Host("H1"), r0);
//        r0.addRouterLink(r1);

        /* NETWORK INITIALIZATION */
        try{
            network.routerList.addAll(Arrays.asList(RestAPI.getRouter()));
        } catch (Exception e){
            e.printStackTrace();
        }
        try{
            network.hostList.addAll(Arrays.asList(RestAPI.getHosts()));
        } catch (Exception e){
            e.printStackTrace();
        }

        for(int i=0;i< network.routerList.size(); i++){
            network.routerList.get(i).setName();

            /* set eventHandler to dispaly stats */
            int finalI = i;
            network.routerList.get(i).setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    field0.setText("ROUTER");
                    field1.setText("NAME:\t" + network.routerList.get(finalI).getName());
                    field2.setText("DPID:\t" + network.routerList.get(finalI).getDpid());
                    StringBuilder str = new StringBuilder("PORTS:\n\n");

                    for (Port p: network.routerList.get(finalI).getPorts()) {
                        str.append("\t"+p.toString()+"\n");
                    }
                    field3.setText(str.toString());
                }
            });
        }
        int index;
        for(int i=0;i< network.hostList.size(); i++){
            network.hostList.get(i).setRouter();
            /* add each host to its router */
            index = network.routerList.indexOf(new Router(network.hostList.get(i).getRouter()));
            network.routerList.get(index).addHostLink(network.hostList.get(i));

            /* set eventHandler to dispaly stats */
            int finalI = i;
            network.hostList.get(i).setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    field0.setText("HOST");
                    field1.setText("MAC:\t" + network.hostList.get(finalI).getMac());
                    StringBuilder str;
                    if (network.hostList.get(finalI).getIpv4().size() == 0){
                        str = new StringBuilder("IPv4:\n");
                    }else{
                        str = new StringBuilder("IPv4:\n\n");
                        for (String p: network.hostList.get(finalI).getIpv4()) {
                            str.append("\t"+p+"\n");
                        }

                    }
                    field2.setText(str.toString());

                    if (network.hostList.get(finalI).getIpv6().size() == 0){
                        str = new StringBuilder("IPv6:\n");
                    }else{
                        str = new StringBuilder("IPv6:\n\n");
                        for (String p: network.hostList.get(finalI).getIpv6()) {
                            str.append("\t"+p+"\n");
                        }

                    }
                    field3.setText(str.toString());
                    field4.setText("PORT:\t" + network.hostList.get(finalI).getPort().toString()+"\n");
                }
            });
        }

        /* PRINT NETITEM */
        for(int i=0;i< network.routerList.size(); i++){
            System.out.println(network.routerList.get(i));
        }
        for(int i=0;i< network.hostList.size(); i++){
            System.out.println(network.hostList.get(i));
        }
//        System.out.println(network.routerList.contains(new Router(network.hostList.get(1).getRouter())));
        network.displayAlgorithm();


        /* STAGE */
        primaryStage.setResizable(false);
        primaryStage.setTitle("Networks GUI");
        primaryStage.setScene(new Scene(layout, windowWidth, windowHeight));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
//        String s = "{'dpid': '0000000000000007', 'ports': [{'dpid': '0000000000000007', 'port_no': '00000001', 'hw_addr': '96:10:5e:db:0c:d3', 'name': 's7-eth1'}, {'dpid': '0000000000000007', 'port_no': '00000002', 'hw_addr': 'c6:e5:b1:06:38:85', 'name': 's7-eth2'}, {'dpid': '0000000000000007', 'port_no': '00000003', 'hw_addr': 'ae:99:21:7c:e0:bb', 'name': 's7-eth3'}]}";
//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .create();

//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithModifiers(Modifier.STATIC,
//                        Modifier.TRANSIENT,
//                        Modifier.VOLATILE)
//                .create();
//        Gson gson = new Gson();
//        SwitchJson switchJson = new SwitchJson();
//        switchJson.setCiaoo(12);
//        System.out.println(switchJson);
//        SwitchJson switchJson = gson.fromJson(s, SwitchJson.class);
//        System.out.println(switchJson);
    }
}
