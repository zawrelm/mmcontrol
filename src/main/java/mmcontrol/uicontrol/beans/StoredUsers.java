package mmcontrol.uicontrol.beans;

import java.io.Serializable;
import java.util.ArrayList;
import mmcontrol.uicontrol.exceptions.UserNotFoundException;
import mmcontrol.uicontrol.model.User;

/**
 * This class stores all users who have been registered as long as the application is running.
 */
public class StoredUsers implements Serializable {
    
    private ArrayList<User> userlist;
    
    /**
     * The constructor initializes the ArrayList.
     */
    public StoredUsers() {
        this.userlist = new ArrayList<>();
        
        //TODO: read list of existing users from database
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
        this.userlist.add(user);
    }
    
    /**
     * Finds and returns a User-object by the email-address.
     * @param email The email-address to be found.
     * @return 
     * @throws UserNotFoundException 
     */
    public User getUser(String email) throws UserNotFoundException {
        if(this.userlist != null) {
            for(int i = 0; i<= this.userlist.size(); i++) {
                if(this.userlist.get(i).getEmail().equals(email)) {
                    return this.userlist.get(i);
                }
            }
        }
        throw new UserNotFoundException();
    }
    
    public void setUserlist(ArrayList<User> userlist) {
        this.userlist = userlist;
    }
    
    public ArrayList<User> getUserlist() {
        return this.userlist;
    }
    
    /**
     * Finds the User with an open UserMachineSession on the given Machine.
     * 
     * @param machineId
     * @return user
     * @throws mmcontrol.uicontrol.exceptions.UserNotFoundException
     */
    public User getOperator(long machineId) throws UserNotFoundException {
        for(int i = 0; i < this.userlist.size(); i++) {
            if(this.userlist.get(i).getCurrentSession() != null && this.userlist.get(i).getCurrentSession().getCurrentSession() != null) {
                if(this.userlist.get(i).getCurrentSession().getCurrentSession().getMachineId() == machineId) {
                    return this.userlist.get(i);
                }
            }
        }
        throw new UserNotFoundException();
    }
    
}
