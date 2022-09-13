package it.finanze.sanita.fse2.ms.gtw.statusmanager.utility;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import lombok.extern.slf4j.Slf4j;
import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.ValidationException;
import java.text.ParseException;

@Slf4j
public final class StringUtility {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private StringUtility() {
		// Constructor intentionally empty.
	}

	/**
	 * Returns {@code true} if the String passed as parameter is null or empty.
	 * 
	 * @param str	String to validate.
	 * @return		{@code true} if the String passed as parameter is null or empty.
	 */
	public static boolean isNullOrEmpty(final String str) {
	    boolean out = false;
		if (str == null || str.isEmpty()) {
			out = true;
		}
		return out;
	}

	/**
	 * Returns the encoded String of the SHA-256 algorithm represented in base 64.
	 * 
	 * @param objectToEncode String to encode.
	 * @return String Encoded.
	 */
	public static String encodeSHA256B64(String objectToEncode) {
		try {
		    final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    final byte[] hash = digest.digest(objectToEncode.getBytes());
		    return encodeBase64(hash);
		} catch (Exception e) {
			log.error("Error calculating sha", e);
			throw new BusinessException("Error calculating SHA-256", e);
		}
	}
	
	/**
	 * Returns the encoded String of the SHA-256 algorithm encoded represented in base hex.
	 * 
	 * @param objectToEncode String to encode.
	 * @return String Encoded.
	 */
	public static String encodeSHA256Hex(String objectToEncode) {
		try {
		    final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    final byte[] hash = digest.digest(objectToEncode.getBytes());
		    return encodeHex(hash);
		} catch (Exception e) {
			log.error("Error calculating sha", e);
			throw new BusinessException("Error calculating SHA-256", e);
		}
	}

	/**
	 * Encode in Base64 the byte array passed as parameter.
	 * 
	 * @param input	The byte array to encode.
	 * @return		The encoded byte array to String.
	 */
	public static String encodeBase64(final byte[] input) {
		return Base64.getEncoder().encodeToString(input);
	}

	/**
	 * Encodes the byte array passed as parameter in hexadecimal.
	 * 
	 * @param input	The byte array to encode.
	 * @return		The encoded byte array to String.
	 */
	public static String encodeHex(final byte[] input) {
		return Hex.encodeHexString(input);
	}

	public static String generateUUID() {
	    return UUID.randomUUID().toString();
	}

	/**
	 * Transformation from Json to Object.
	 * 
	 * @param <T>	Generic type of return
	 * @param json	json
	 * @param cls	Object class to return
	 * @return		object
	 */
	public static <T> T fromJSON(final String json, final Class<T> cls) {
		return new Gson().fromJson(json, cls);
	}

	/**
	 * Transformation from Object to Json.
	 * 
	 * @param obj	object to transform
	 * @return		json
	 */
	public static String toJSON(final Object obj) {
		return new Gson().toJson(obj);
	}

	/**
	 * Method for date format validation
	 * @param data
	 */
	public static void dateFormatValid(String data) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if (data == null || data.trim().equals("")) {
			throw new ValidationException("Attenzione la data fornita risulta essere null o vuota");
		}

		try {
			sdf.parse(data);
		}
		/* Date format is invalid */
		catch (ParseException e) {
			throw new ValidationException("Attenzione la data fornita risulta essere non essere nel formato corretto (yyyy-MM-dd)");
		}

	}
	
	/**
	 * Metodo che permette data l'uri definita nelle prop di avere il nome del db
	 * 
	 * @param uri
	 * @return string
	 */
	public static String getDatabaseName(final String uri) { 
		int indexDBName = uri.lastIndexOf("/");
		String nameWithReplica = uri.substring(indexDBName+1, uri.length()).trim();
		if(nameWithReplica.contains("?")) {
			nameWithReplica = nameWithReplica.substring(0, nameWithReplica.indexOf('?')).trim();
		}
		return nameWithReplica;
	}

}
