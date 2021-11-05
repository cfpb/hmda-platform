package gov.cfpb.keycloak.authenticator.hmda;

public class TopHolder {
    private Integer idRssd;
    private String name;

    public TopHolder(){}

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
        return "TopHolder{" +
                "idRssd=" + idRssd +
                ", name='" + name + '\'' +
                '}';
    }
}
