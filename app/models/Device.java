package models;

import java.util.Date;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.data.format.Formats;
import play.data.validation.Constraints;

public class Device extends BaseModel{

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    public String deviceId;
    
    @Formats.DateTime(pattern="yyyy-MM-dd")
    public Date registDate;
    
    @ManyToMany
    public User user;
}
