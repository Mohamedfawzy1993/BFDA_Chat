package model.chat;

import beans.Group;
import beans.Message;
import client.interfaces.ChatHandler;
import javafx.application.Platform;
import view.controller.ControllerManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;


public class ChatImpl extends UnicastRemoteObject implements ChatHandler {

    private ControllerManager mainController;
    public ChatImpl() throws RemoteException {
        this.mainController = ControllerManager.getInstance();
    }




    /**
    * 
    * @param chatID
    * @param message
    **/
    @Override
    public synchronized void updateChat(String chatID, Message message)
    {
    //    System.out.println(mainController.getWelcomeController() == null);
        if(mainController.getMainController().getCurrentChatID().equals(chatID))
        {
            mainController.getMainController().getMsgMap().get(chatID).add(message);
            Platform.runLater(()->{mainController.getMainController().getCurrentChatObserver().add(message);});
        }
        else
        {
            if(mainController.getMainController().getMsgMap().containsKey(chatID))
            {
                mainController.getMainController().getMsgMap().get(chatID).add(message);
            }
            else
            {
                Vector<Message> msgs = new Vector<Message>();
                msgs.add(message);
                mainController.getMainController().getMsgMap().put(chatID , msgs );
            }

        }
    }

    /**
    * 
    * @param chatID
    * @param users
    * @throws RemoteException 
    **/
    @Override
    public void registerChat(String chatID, Vector<String> users) throws RemoteException {
    }

    /**
    * 
    * @param msg
    **/
    @Override
    public void updateAnnouncement(String msg)
    {
        mainController.getMainController().updateAnnounce(msg);
        System.out.print(msg);
    }

    @Override
    public boolean updateConnection() {
        return true;
    }

    /**
    * 
    * @param group
    * @throws RemoteException 
    **/
    @Override
    public void notifyGroupChat(Group group) throws RemoteException {
        Platform.runLater(()->{
            ControllerManager.getInstance().getMainController().getChatGroupsList().getItems().add(group);
        });
    }


}
