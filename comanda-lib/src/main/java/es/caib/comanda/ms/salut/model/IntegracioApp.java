package es.caib.comanda.ms.salut.model;

public enum IntegracioApp {
    ACH ("Archium"),
    AFI ("Afirma"),
    ARX ("Arxiu"),
    CAR ("Carpeta"),
    CIE ("Cie"),
    DIB ("DigitalIB"),
    DIR ("Dir3Caib"),
    DIS ("Distribucio"),
    EML ("E-mail"),
    EMS ("Emiserv"),
    EVI ("EvidenciesIB"),
    GDC ("Gestor documental"),
    HEL ("Helium"),
    ITD ("InterDoc"),
    LGI ("LoginIB"),
    NOT ("Notib"),
    NTF ("Notifica"),
    PAE ("PaymentIB"),
    PBL ("Pinbal"),
    PFI ("Portafirmes"),
    REG ("Registre"),
    RIP ("Ripea"),
    RSC ("Rolsac"),
    RS2 ("Rolsac2"),
    SIG ("Signatura"),
    SIS ("Sistra"),
    SI2 ("Sistra2"),
    TIN ("TinyCaib"),
    TRA ("TranslatorIB"),
    USR ("Usuaris"),
    VFI ("Validació firma");

    private final String nom;

    IntegracioApp(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }
}
