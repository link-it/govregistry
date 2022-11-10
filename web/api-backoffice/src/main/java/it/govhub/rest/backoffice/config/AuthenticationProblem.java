package it.govhub.rest.backoffice.config;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Questa classe è identica a it.govhub.rest.backoffice.beans.Problem eccetto per il fatto che non 
 * eredita da RepresentationModel.
 * E' necessaria perchè se nel 'commence' di un AuthenticationEntrypoint restituiamo il Problem di sopra,
 * ci troviamo un array vuoto chiamato "links".
 * 
 *
 */
public class AuthenticationProblem {

	 @JsonProperty("detail")
	  public String detail;

	  @JsonProperty("instance")
	  public URI instance;

	  @JsonProperty("status")
	  public Integer status;

	  @JsonProperty("title")
	  public String title;

	  @JsonProperty("type")
	  public URI type = URI.create("about:blank");

}
