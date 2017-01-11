package com.server.extensions.user;

import flygame.extensions.db.DbManager;
import flygame.extensions.db.ResultObjectBuilder;

/**
 * Created by wuyingtan on 2016/12/19.
 */
public class UserDao {
    private static final UserDao instance = new UserDao();

    private UserDao() {
    }

    public static UserDao getInstance() {
        return instance;
    }

    private static final String ADD_USER = "INSERT INTO Users(userId,userName,userPwd) VALUES(?,?,?)";
    private static final String GET_USER_BY_ID = "SELECT * FROM Users WHERE userId = ?";
    private static final String SET_USER_NAME = "UPDATE Users SET userName = ? WHERE userId = ?";

    public int addUser(long userId, String userName, String userPwd) {
        return DbManager.getWorkDb().executeCommand(ADD_USER, new Object[]{userId, userName, userPwd});
    }

    public User getUserById(long userId) {
        return DbManager.getWorkDb().executeScalarObject(GET_USER_BY_ID, new Object[]{userId}, USER_BUILDER); //.executeCommand(new Object[]{userId},USER_BUILDER);
    }

    public int setUserName(long userId,String userName){
        return DbManager.getWorkDb().executeCommand(SET_USER_NAME,new Object[]{userName,userId});
    }

    public static final ResultObjectBuilder<User> USER_BUILDER = rs -> {
        long userId = rs.getLong("userId");
        String name = rs.getString("userName");
        String userPwd = rs.getString("userPwd");
        return new User(userId, name, userPwd);
    };
}
