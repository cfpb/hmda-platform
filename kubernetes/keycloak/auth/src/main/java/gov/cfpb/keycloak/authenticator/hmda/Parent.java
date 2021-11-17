package gov.cfpb.keycloak.authenticator.hmda;

public class Parent {

    private Integer idRssd;
    private String name;

    public Parent() {
    }

    public Integer getIdRssd() {
        return idRssd;
    }

    public void setIdRssd(Integer idRssd) {
        this.idRssd = idRssd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Parent{" +
                "idRssd=" + idRssd +
                ", name='" + name + '\'' +
                '}';
    }
}
