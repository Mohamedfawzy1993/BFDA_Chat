package model.database;

import beans.Message;
import utilities.SqlParser;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class DatabaseChatOperation  {

    private PreparedStatement preparedStatement;
    private Connection connection;
    private ResultSet resultSet;

    public DatabaseChatOperation() throws SQLException, ClassNotFoundException  {

        Database database = Database.getInstance();
        connection = database.getConnection();

    }

    public String getChatRoomOfClient(String myName , String clientName) throws SQLException {

        String query = "SELECT * FROM Chatdb.FullChatRoomData where userName = ? and ChatRoom_id in " +
                "(select ChatRoom_id from Chatdb.FullChatRoomData where userName = ?) and type = 0;";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1 , clientName);
        preparedStatement.setString(2 , myName);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.next())
            return resultSet.getString(1);
        else
            return createChatRoomWithUser("" , clientName , myName);


    }

    public String createChatRoomWithUser(String chatRoomName , String clientName , String myName) throws SQLException {
        String chatRoomID = getChatRoomOfClient(myName , clientName);
        if(chatRoomID == null)
        {
            chatRoomID = createChatRoom(chatRoomName , "0");
            addClientToChatRoom(chatRoomID , myName);
            addClientToChatRoom(chatRoomID , clientName);
        }
        return chatRoomID;

    }

    public String createChatRoomWithUsers(String chatRoomName , Vector<String> clients) throws SQLException {

        if(isAvailableChatRoomName(chatRoomName))
        {
            String chatRoomID = createChatRoom(chatRoomName , "1");
            for(String client : clients)
                addClientToChatRoom(chatRoomID , client);
            return chatRoomID;
        }
        return null;

    }

    public String sendMsgtoDatabase(String chatMemberID , Message message) throws SQLException {
        String query = "INSERT INTO `Chatdb`.`ChatMsg` " +
                "(`ChatMember_id`, `MsgBody`, `MsgFont`, `MsgSize`, `MsgIsBold`, `MsgIsItalic`, `MsgColor`, `DateStamp`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, '?);";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1 , chatMemberID);
        preparedStatement.setString(2 , message.getMessageContent());
        preparedStatement.setString(3 , message.getMessageFontFamily());
        preparedStatement.setString(4 , message.getMessageFontSize());
        preparedStatement.setBoolean(5 , message.isBold());
        preparedStatement.setBoolean(6 , message.isItalic());
        preparedStatement.setString(7 , message.getMessageFontColor());
        preparedStatement.setTimestamp(8 , SqlParser.fromLocalDateTimeToSql(message.getMessageDate()));
        preparedStatement.executeUpdate();
        connection.commit();

        ResultSet rs = preparedStatement.getGeneratedKeys();
        if (rs.next())
            return rs.getString(1);
        return null;
    }

    private boolean isAvailableChatRoomName(String chatRoomName) throws SQLException {
        String query = "SELECT * FROM Chatdb.ChatRoom where ChatName = ?";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1 , chatRoomName);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.next())
            return false;
        else
            return true;
    }

    private String createChatRoom(String chatRoomName , String type) throws SQLException {

        String query = "INSERT INTO `Chatdb`.`ChatRoom` (`ChatName`, `Type`, `Active`) VALUES (?, ?, '1');";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1 , chatRoomName);
        preparedStatement.setString(2 , type);
        preparedStatement.executeUpdate();
        connection.commit();
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if (rs.next())
            return rs.getString(1);
        return null;

    }

    private void addClientToChatRoom(String chatRoomID , String users) throws SQLException {
        String query = "INSERT INTO `Chatdb`.`ChatMember` (`ChatRoom_id`, `User_id`, `isAdmin`) VALUES (?, (select User.id from User where username = ?), '0');";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1 , chatRoomID);
        preparedStatement.setString(2 , users);
        preparedStatement.executeUpdate();
        connection.commit();

    }

    public String getChatMemberID(String userName , String chatRoomID) throws SQLException {
        String query = "select ChatMember.id from ChatMember , User where User.username = ? " +
                "and ChatMember.User_id = User.id " +
                "and ChatMember.ChatRoom_id = ? ";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1 , userName);
        preparedStatement.setString(2 , chatRoomID);

        resultSet = preparedStatement.executeQuery();
        if(resultSet.next())
            return resultSet.getString(1);
        else
            return null;
    }

    public Vector<Message> getAllRoomMessages(String chatRoomID)
    {
      return null;
    }

    public Message getMessage(String chatMsgID)
    {
        return null;
    }

    public Vector<String> chatMembers(String chatMsgID) throws SQLException {
        String query = "select User.username from User , ChatMember where ChatRoom_id =" +
                "    (select ChatRoom.id from ChatRoom , ChatMsg , ChatMember " +
                "    where ChatMsg.id = ?" +
                "    and ChatMember.id = ChatMsg.ChatMember_id " +
                "    and ChatRoom.id = ChatMember.ChatRoom_id ) " +
                "    and ChatMember.ChatRoom_id = ChatMember.ChatRoom_id " +
                "    and User.id = ChatMember.User_id ;";
        preparedStatement.setString(1,chatMsgID);
        resultSet = preparedStatement.executeQuery();
        Vector<String> members = new Vector<>();
        while(resultSet.next())
            members.add(resultSet.getString(1));
        return members;

    }


}