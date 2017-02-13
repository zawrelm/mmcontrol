package mmcontrol.uicontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 * Represents a User of the control software.
 * 
 * @author Michael Zawrel
 */
@Entity
@Table(name="users",
       uniqueConstraints=@UniqueConstraint(columnNames={"email"}))
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    protected long id;
    
    @Column
    private String email;
    
    @Column
    private String password; //encrypt?

    @Column
    private String firstname;
    
    @Column
    private String lastname;
    
    @Column
    private String title; //academic title
    
    //private organization (enum? TU Wien, UTN Buenos Aires, ...)
    
    //position (enum? professor, laboratory worker, student)
    
    //private HashMap<Machine, Permission> machinePermissions; (enum?)
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private ArrayList<UserSession> sessions;
    
    @ManyToMany(mappedBy = "observers")
    private List<UserMachineSession> sessionsObserved;

    protected User() { }
    
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.sessions = new ArrayList<>();
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<UserSession> getSessions() {
        return sessions;
    }
    
    public UserSession getCurrentSession() {
        if(this.sessions.size() > 0 && this.sessions.get(sessions.size()-1).getEndTime() == null) {
            return this.sessions.get(this.sessions.size()-1);
        }
        else return null;
    }

    public void startSession() {
        UserSession us = new UserSession();
        us.setId(this.sessions.size()+1);
        us.setBeginTime(new java.sql.Date(new Date().getTime()));
        this.sessions.add(us);
    }

    public void endSession() {
        if(this.getCurrentSession() != null) {
            this.getCurrentSession().endUserMachineSession();
            this.getCurrentSession().setEndTime(new java.sql.Date(new Date().getTime()));
        }
    }

    public List<UserMachineSession> getSessionsObserved() {
        return sessionsObserved;
    }

    public void addSessionObserved(UserMachineSession session) {
        this.sessionsObserved.add(session);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return this.id == other.id;
    }
    
}
