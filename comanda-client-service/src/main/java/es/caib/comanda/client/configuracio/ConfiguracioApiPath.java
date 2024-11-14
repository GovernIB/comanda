package es.caib.comanda.client.configuracio;

public class ConfiguracioApiPath {

	public static final String NOM_SERVEI = "comanda-configuracio";

	public static final String API_PATH = "/api/v1/configuracions";

	public static final String LIST_CONFIGURACIONS = API_PATH + "";
	public static final String CREATE_CONFIGURACIO = API_PATH + "";
	public static final String GET_CONFIGURACIO = API_PATH + "/{configuracioId}";
	public static final String UPDATE_CONFIGURACIO = API_PATH + "/{configuracioId}";
	public static final String PATCH_CONFIGURACIO = API_PATH + "/{configuracioId}";
	public static final String DELETE_CONFIGURACIO = API_PATH + "/{configuracioId}";

}
