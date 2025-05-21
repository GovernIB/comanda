package es.caib.comanda.ms.logic.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Mètodes per a desar arxius a la carpeta files de l'aplicació.
 *
 * @author Límit Tecnologies
 */
public abstract class BaseFilesHelper {

	/**
	 * Desa un fitxer donat el seu contingut en un forma d'array de bytes.
	 *
	 * @param folder
	 *            la carpeta pel nou arxiu (pot ser null).
	 * @param name
	 *            el nom de l'arxiu.
	 * @param content
	 *            el contingut de l'arxiu.
	 * @throws IOException si hi ha algun problema desant l'arxiu.
	 */
	public void save(
			String folder,
			String name,
			byte[] content) throws IOException {
		File fitxer = new File(newFolderFile(folder), name);
		try (FileOutputStream fos = new FileOutputStream(fitxer)) {
			fos.write(content);
		}
	}

	/**
	 * Desa un fitxer donat el seu contingut en forma de ByteArrayOutputStream.
	 *
	 * @param folder
	 *            la carpeta pel nou arxiu (pot ser null).
	 * @param name
	 *            el nom de l'arxiu.
	 * @param content
	 *            el contingut de l'arxiu.
	 * @throws IOException si hi ha algun problema desant l'arxiu.
	 */
	public void save(
			String folder,
			String name,
			ByteArrayOutputStream content) throws IOException {
		File fitxer = new File(newFolderFile(folder), name);
		try (FileOutputStream fos = new FileOutputStream(fitxer)) {
			content.writeTo(fos);
		}
	}

	/**
	 * Indica si la funcionalitat està activa (si el mètode getFilesPath retorna alguna cosa).
	 *
	 * @return true si està activa o false en cas contrari.
	 */
	public boolean isFilesActive() {
		String files = getFilesPath();
		return files != null && !files.isEmpty();
	}

	private File newFolderFile(String folder) {
		String files = getFilesPath();
		String path;
		if (folder != null) {
			if (files.endsWith("/")) {
				path = files + folder;
			} else {
				path = files + "/" + folder;
			}
		} else {
			path = files;
		}
		File target = new File(path);
		target.mkdirs();
		return target;
	}

	protected abstract String getFilesPath();

}
