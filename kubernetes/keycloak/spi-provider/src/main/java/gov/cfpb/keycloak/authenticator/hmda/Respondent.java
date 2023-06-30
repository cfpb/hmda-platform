package gov.cfpb.keycloak.authenticator.hmda;

public class Respondent {

    private String name;

    public Respondent(){
    }

    @Override
    public String toString() {
        return "Respondent{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
