package app.carburo.loader.config.parser.deserialize;

import com.google.gson.*;
import app.carburo.loader.application.rest.dto.ESParserDTO;
import app.carburo.loader.application.rest.dto.PreciosCombustibleParserDTO;

import java.lang.reflect.Type;

public class ESParserDTODeserializer extends BasePropertyDeserializer<ESParserDTO> {

	@Override
	public ESParserDTO deserialize(JsonElement json, Type typeOfT,
								   JsonDeserializationContext context)
			throws JsonParseException {

		JsonObject obj = json.getAsJsonObject();

		// Gson normal → respeta @SerializedName
		ESParserDTO es = new Gson().fromJson(obj, ESParserDTO.class);
		// Sacamos los precios DESDE EL MISMO JSON
		PreciosCombustibleParserDTO precios = context.deserialize(obj,
																  PreciosCombustibleParserDTO.class);
		// Los inyectamos
		es.setPrecios(precios);
		return es;
	}
}
