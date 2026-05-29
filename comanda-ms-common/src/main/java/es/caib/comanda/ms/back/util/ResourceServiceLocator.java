package es.caib.comanda.ms.back.util;

import es.caib.comanda.ms.logic.intf.exception.ComponentNotFoundException;
import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.ms.logic.intf.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * Localitzador de serveis de tipus ResourceService donat un recurs.
 * 
 * @author Límit Tecnologies
 */
@Component
public class ResourceServiceLocator {

	@Autowired(required = false)
	private Collection<ReadonlyResourceService<?, ?>> readonlyResourceServices;

	public <R extends Resource<ID>, ID extends Serializable> ReadonlyResourceService<R, ID> getReadOnlyEntityResourceServiceForResourceClass(
			Class<? extends Serializable> resourceClass) throws ComponentNotFoundException {
		ReadonlyResourceService<R, ID> resourceServiceFound = null;
		if (readonlyResourceServices != null) {
			for (ReadonlyResourceService<?, ?> resourceService: readonlyResourceServices) {
				Class<?> serviceResourceClass = TypeUtil.getArgumentClassFromGenericSuperclass(
						resourceService.getClass(),
						ReadonlyResourceService.class,
						0);
				if (resourceClass.equals(serviceResourceClass)) {
					resourceServiceFound = (ReadonlyResourceService<R, ID>)resourceService;
					break;
				}
			}
		}
		if (resourceServiceFound != null) {
			return resourceServiceFound;
		} else {
			throw new ComponentNotFoundException(resourceClass, "ReadonlyResourceService for resource class " + resourceClass.getName());
		}
	}

	public <R extends Resource<ID>, ID extends Serializable> MutableResourceService<R, ID> getMutableEntityResourceServiceForResourceClass(
			Class<? extends Serializable> resourceClass) throws ComponentNotFoundException {
		ReadonlyResourceService<R, ID> readOnlyService = getReadOnlyEntityResourceServiceForResourceClass(resourceClass);
		if (readOnlyService instanceof MutableResourceService) {
			return (MutableResourceService<R, ID>)readOnlyService;
		} else {
			throw new ComponentNotFoundException(resourceClass, "MutableResourceService for resource class " + resourceClass.getName());
		}
	}

}
