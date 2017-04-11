package mmcontrol.uicontrol.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcontrol.uicontrol.exceptions.UserNotFoundException;
import mmcontrol.uicontrol.model.User;

/**
 * This class stores all users who have been registered as long as the application is running.
 */
public class StoredUsers implements Serializable {
    
    private HashMap<User, LoginCtrl> users;
    
    /**
     * The constructor initializes the ArrayList.
     */
    public StoredUsers() {
        this.users = new HashMap<>();
        
        //TODO: read list of existing users from database
        /**for(int i = 1; i < 100; i++) {
            User user = new User(i+"@manejo", "test");
            user.setId(i);
            user.setFirstname("John");
            user.setLastname("Doe");
            user.setTitle("PhD.");
            this.addUser(user);
        }*/
        
        User user = new User("john@doe.com", "test");
        user.setId(1);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setTitle("PhD.");
        this.addUser(user);

    }
    
    /**
     * Adds a user to the list.
     * @param user The user to be added.
     */
    public void addUser(User user) {
        this.users.put(user, null);
    }
    
    /**
     * Finds and returns a User-object by the email-address.
     * @param email The email-address to be found.
     * @return User having the parameter-given email-address
     * @throws UserNotFoundException 
     */
    public User getUser(String email) throws UserNotFoundException {
        if(this.users != null) {
            for(User user : this.users.keySet()) {
                if(user.getEmail().equals(email)) {
                    return user;
                }
            }
        }
        throw new UserNotFoundException();
    }
    
    /**
     * Finds and returns a User-object by the email-address.
     * @param userId The userId to be found.
     * @return User having the parameter-given userId
     * @throws UserNotFoundException 
     */
    public User getUser(long userId) throws UserNotFoundException {
        if(this.users != null) {
            for(User user : this.users.keySet()) {
                if(user.getId() == userId) {
                    return user;
                }
            }
        }
        throw new UserNotFoundException();
    }

    public void setUsers(HashMap<User, LoginCtrl> users) {
        this.users = users;
    }
    
    public HashMap<User, LoginCtrl> getUsers() {
        return this.users;
    }
    
    /**
     * Returns the SessionBean of a logged-in User connected to the given Machine.
     * 
     * @param machineId
     * @return user
     * @throws mmcontrol.uicontrol.exceptions.UserNotFoundException
     */
    public LoginCtrl getUserHTTPSessionObject(long machineId) throws UserNotFoundException {
        for(User user : this.users.keySet()) {
            LoginCtrl loginObj = this.users.get(user);
            if(loginObj != null && loginObj.getSelectedMachine() != null 
                    && loginObj.getSelectedMachine().getId() == machineId) {
                return loginObj;
            }
        }
        throw new UserNotFoundException();
    }
    
    public void setUserHTTPSessionObject(LoginCtrl sessionBean) throws UserNotFoundException {
        if(this.users.containsKey(sessionBean.getUser())) {
            this.users.put(sessionBean.getUser(), sessionBean);
        }
        else throw new UserNotFoundException();
    }
    
}
