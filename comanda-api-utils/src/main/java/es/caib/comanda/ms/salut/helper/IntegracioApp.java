package es.caib.comanda.ms.salut.helper;

public enum IntegracioApp {
    ACH ("Archium"),
    AFI ("Afirma"),
    ARX ("Arxiu"),
    CAR ("Carpeta"),
    CSV ("ConCSV"),
    CDO ("Conversió de documents"),
    CIE ("Cie"),
    CUS ("Custòdia"),
    DIB ("DigitalIB"),
    DIR ("Dir3Caib"),
    DIS ("Distribucio"),
    EML ("E-mail"),
    EMS ("Emiserv"),
    EVI ("EvidenciesIB"),
    GDC ("Gestor documental"),
    HEL ("Helium"),
    IPA ("Ripea"),
    ITD ("InterDoc"),
    LGI ("LoginIB"),
    NOT ("Notib"),
    NTF ("Notifica"),
    PAE ("PaymentIB"),
    PBL ("Pinbal"),
    PFI ("Portafirmes"),
    REG ("Registre"),
    RSC ("Rolsac"),
    RS2 ("Rolsac2"),
    SIG ("Signatura"),
    SIS ("Sistra"),
    SI2 ("Sistra2"),
    TIN ("TinyCaib"),
    TRA ("TranslatorIB"),
    USR ("Usuaris"),
    VIF ("ViaFirma"),
    VFI ("Validació firma");

    private final String nom;

    IntegracioApp(String nom) {
        this.nom = nom;
    }

    public String getCodi() {
        return this.name();
    }
    public String getNom() {
        return nom;
    }
}
