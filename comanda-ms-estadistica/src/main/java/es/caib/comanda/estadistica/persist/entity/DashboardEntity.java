package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.ms.persist.entity.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.List;

public class DashboardEntity extends BaseEntity<Long> {

    @Column(name = "titol", length = 64, nullable = false)
    private String titol;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;

    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<DashboardItemEntity> items;

}
