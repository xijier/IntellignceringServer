package models;

import javax.persistence.Entity;

import play.data.validation.Constraints;

/**
 * Company User managed by Ebean
 */
@Entity
public class User extends BaseModel{

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    public String username;
    
    public String password;
    
    public String phone;
    
    public String email;
    
    public String register_area;
    
}
