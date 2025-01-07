package es.caib.comanda.configuracio.logic.intf.util;

/**
 * Helper encarregat de gestionar la sessió de l'usuari.
 * 
 * @author Limit Tecnologies
 */
public class RequestSessionUtil {

	public static void setRequestSession(Object requestSession) {
		ThreadLocalUtil.clear();
		ThreadLocalUtil.setAttribute(ThreadLocalUtil.SESSION_KEY, requestSession);
	}
	public static Object getRequestSession() {
		return ThreadLocalUtil.getAttribute(ThreadLocalUtil.SESSION_KEY, Object.class);
	}

}