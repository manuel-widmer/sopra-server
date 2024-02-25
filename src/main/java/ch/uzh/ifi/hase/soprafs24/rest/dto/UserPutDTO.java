package ch.uzh.ifi.hase.soprafs24.rest.dto;
import java.util.Date;
public class UserPutDTO {

    private Date birthDate;

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
