 it.finanze.sanita.fse2.ms.gtw.statusmanager.service;

public interface IConfigSRV {
	Integer getExpirationDate();
	Boolean isCfOnIssuerNotAllowed();
	Boolean isSubjectNotAllowed();
	long getRefreshRate();
}
