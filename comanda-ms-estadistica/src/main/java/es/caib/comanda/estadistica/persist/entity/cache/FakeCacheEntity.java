package es.caib.comanda.estadistica.persist.entity.cache;

import es.caib.comanda.estadistica.logic.intf.model.cache.ComandaCache;
import es.caib.comanda.ms.persist.entity.ResourceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Subselect;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Immutable
@Entity(name = "FakeCache")
@Table(name = "FakeCache")
@Getter
@Setter
@NoArgsConstructor
@Subselect("select codi AS id from COM_ENTORN")
public class FakeCacheEntity  implements ResourceEntity<ComandaCache, String> {

    @Id
    @Column(name = "id")
    private String id;

    @Override
    public void setId(String id) { }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
