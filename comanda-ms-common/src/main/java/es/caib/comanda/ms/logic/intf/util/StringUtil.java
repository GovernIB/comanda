package es.caib.comanda.ms.logic.intf.util;

/**
 * Utilitats per strings.
 * 
 * @author Límit Tecnologies
 */
public class StringUtil {

	public static String capitalize(String str) {
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	public static String decapitalize(String str) {
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
	}

}
