package view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ClientObject;
import model.ServerConnection;
import view.util.ValidationChecks;
import server.interfaces.ServerObj;

public class WelcomeController implements Initializable {
    
    @FXML private Label errorLabel;
    @FXML private TextField serverIpField1;
    @FXML private TextField serverIpField2;
    @FXML private TextField serverIpField3;
    @FXML private TextField serverIpField4;
    @FXML private Button loginButton;

    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public boolean validateIp(String ipAddress)
    {
        ValidationChecks validationChecks = new ValidationChecks();
        return validationChecks.isIP(ipAddress);
    }
    public void connect(ActionEvent actionEvent) {
        String ipAddress = serverIpField1.getText() + "." + serverIpField2.getText() + "." + serverIpField4.getText() + "." + serverIpField4.getText();
        if(!validateIp(ipAddress))
            errorLabel.setText("IP is not a valid IP, please re-write a valid one..");
        else
        {
            new Thread(()->{
                try {
                    if(connectToServer(ipAddress))
                        openLogin();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    public boolean connectToServer(String ipAddress) throws IOException {
        ServerConnection serverConnection = ServerConnection.getInstance();
        serverConnection.setHost(ipAddress);
        connectionEstablishingMode(true);
        boolean flag = false;
        if (serverConnection.establiseConnection())
        {
            Platform.runLater(()->{errorLabel.setText("Connection Establised !");});
            ServerObj serverObj = ServerConnection.getInstance().getRegisteryObject();
            serverObj.getClientServerRegister().registerUser(new ClientObject());
            flag =  true;
        }
        else
        {
            Platform.runLater(()->{errorLabel.setText("Server is Not Reachable");});
            flag =  false;
        }
        connectionEstablishingMode(false);
        return flag;

    }

    public void connectionEstablishingMode(boolean status)
    {
        if(status)
            Platform.runLater(()-> {
                errorLabel.setText("");
                loginButton.setText("Connecting ... ");
                loginButton.setDisable(true);
                serverIpField1.setDisable(true);
                serverIpField2.setDisable(true);
                serverIpField3.setDisable(true);
                serverIpField4.setDisable(true);
            });
        else
            Platform.runLater(()-> {
                loginButton.setDisable(false);
                loginButton.setText("Connect");
                serverIpField1.setDisable(false);
                serverIpField2.setDisable(false);
                serverIpField3.setDisable(false);
                serverIpField4.setDisable(false);
            });
    }


    public void openLogin()
    {
        Platform.runLater(()->{

            Parent root = null;
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                root = fxmlLoader.load(getClass().getResource("/fxml/LoginScene.fxml"));
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("BFDA Chat | Login");
                stage.setScene(scene);
                Stage currentStage = (Stage)loginButton.getScene().getWindow();
                ControllerManager.getInstance().setLoginController(fxmlLoader.getController());
                currentStage.close();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }


        });


    }

}


