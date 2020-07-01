package gov.cfpb.keycloak.authenticator.hmda;

import java.util.Set;

public class Institution {

  private Integer activityYear;
  private String lei;
  private Integer agency;
  private Integer institutionType;
  private String institutionId2017;
  private String taxId;
  private Integer rssd;
  private Set<String> emailDomains;
  private Respondent respondent;
  private Parent parent;
  private Long assets;
  private Integer otherLenderCode;
  private TopHolder topHolder;
  private boolean hmdaFiler;


  public Institution(){}


  public Integer getActivityYear() {
    return activityYear;
  }

  public void setActivityYear(Integer activityYear) {
    this.activityYear = activityYear;
  }

  public String getLei() {
    return lei;
  }

  public void setLei(String lei) {
    this.lei = lei;
  }

  public Integer getAgency() {
    return agency;
  }

  public void setAgency(Integer agency) {
    this.agency = agency;
  }

  public Integer getInstitutionType() {
    return institutionType;
  }

  public void setInstitutionType(Integer institutionType) {
    this.institutionType = institutionType;
  }

  public String getInstitutionId2017() {
    return institutionId2017;
  }

  public void setInstitutionId2017(String institutionId2017) {
    this.institutionId2017 = institutionId2017;
  }

  public String getTaxId() {
    return taxId;
  }

  public void setTaxId(String taxId) {
    this.taxId = taxId;
  }

  public Integer getRssd() {
    return rssd;
  }

  public void setRssd(Integer rssd) {
    this.rssd = rssd;
  }

  public Set<String> getEmailDomains() {
    return emailDomains;
  }

  public void setEmailDomains(Set<String> emailDomains) {
    this.emailDomains = emailDomains;
  }

  public Respondent getRespondent() {
    return respondent;
  }

  public void setRespondent(Respondent respondent) {
    this.respondent = respondent;
  }

  public Parent getParent() {
    return parent;
  }

  public void setParent(Parent parent) {
    this.parent = parent;
  }

  public Long getAssets() {
    return assets;
  }

  public void setAssets(Long assets) {
    this.assets = assets;
  }

  public Integer getOtherLenderCode() {
    return otherLenderCode;
  }

  public void setOtherLenderCode(Integer otherLenderCode) {
    this.otherLenderCode = otherLenderCode;
  }

  public TopHolder getTopHolder() {
    return topHolder;
  }

  public void setTopHolder(TopHolder topHolder) {
    this.topHolder = topHolder;
  }

  public boolean isHmdaFiler() {
    return hmdaFiler;
  }

  public void setHmdaFiler(boolean hmdaFiler) {
    this.hmdaFiler = hmdaFiler;
  }

    @Override
    public String toString() {
        return "Institution{" +
                "activityYear=" + activityYear +
                ", lei='" + lei + '\'' +
                ", agency=" + agency +
                ", institutionType=" + institutionType +
                ", institutionId2017='" + institutionId2017 + '\'' +
                ", taxId='" + taxId + '\'' +
                ", rssd=" + rssd +
                ", emailDomains=" + emailDomains +
                ", respondent=" + respondent +
                ", parent=" + parent +
                ", assets=" + assets +
                ", otherLenderCode=" + otherLenderCode +
                ", topHolder=" + topHolder +
                ", hmdaFiler=" + hmdaFiler +
                '}';
    }
}
