package com.inggarciabaldo.carburo.config.parser.deserialize;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.inggarciabaldo.carburo.application.rest.dto.ESParserDTO;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

/**
 * Clase base para deserializadores que leen los nombres de los campos
 * desde un archivo jsonKey.properties cargado por PropertyLoader.
 *
 * @param <T> Tipo del DTO destino.
 */
public abstract class BasePropertyDeserializer<T> implements JsonDeserializer<T> {

	protected static final Type LIST_ES_PARSER_TYPE = new TypeToken<List<ESParserDTO>>() {}.getType();

	/**
	 * Implementa este método en el deserializer concreto para definir cómo se
	 * construye el DTO a partir del JsonObject.
	 */
	@Override
	public abstract T deserialize(JsonElement json, Type typeOfT,
								  JsonDeserializationContext context)
			throws JsonParseException;

	// ===== Métodos auxiliares reutilizables =====

	protected String getAsString(JsonObject obj, String key) {
		JsonElement el = obj.get(key);
		return el != null && !el.isJsonNull() ? el.getAsString() : null;
	}

	protected JsonObject getAsObject(JsonObject obj, String key) {
		JsonElement el = obj.get(key);
		return (el != null && el.isJsonObject()) ? el.getAsJsonObject() : null;
	}

	protected JsonArray getAsArray(JsonObject obj, String key) {
		JsonElement el = obj.get(key);
		return (el != null && el.isJsonArray()) ? el.getAsJsonArray() : null;
	}
}
