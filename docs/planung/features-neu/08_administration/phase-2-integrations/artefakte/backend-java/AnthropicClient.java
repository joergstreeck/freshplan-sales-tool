package de.freshplan.ai.providers;

import de.freshplan.ai.AiRouterInterceptor;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;

@ApplicationScoped
public class AnthropicClient implements AiRouterInterceptor.AiClient {

  @Inject ObjectMapper mapper;

  String apiKey() {
    String k = System.getenv("ANTHROPIC_API_KEY");
    if (k == null || k.isBlank()) throw new IllegalStateException("ANTHROPIC_API_KEY missing");
    return k;
  }

  String modelForUsecase(AiRouterInterceptor.AiUseCase useCase){
    return switch (useCase){
      case LEAD_CLASSIFY, EMAIL_CATEGORIZE, HELP_DRAFT, CONTACT_ROLE_DETECT -> System.getenv().getOrDefault("ANTHROPIC_MODEL_SMALL","haiku");
      default -> System.getenv().getOrDefault("ANTHROPIC_MODEL_LARGE","sonnet");
    };
  }

  @Override
  public AiRouterInterceptor.AiResponse call(AiRouterInterceptor.AiUseCase useCase, AiRouterInterceptor.AiRequest req) {
    try {
      String model = modelForUsecase(useCase);
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
      ObjectNode body = mapper.createObjectNode();
      body.put("model", model);
      var msgs = mapper.createArrayNode();
      msgs.add(mapper.createObjectNode().put("role","user").put("content",
        "Return strict JSON with fields content (string) and confidence (0..1). No prose.\n"+req.prompt()));
      body.set("messages", msgs);
      body.put("max_tokens", 512);

      HttpRequest httpReq = HttpRequest.newBuilder()
        .uri(URI.create("https://api.anthropic.com/v1/messages"))
        .header("x-api-key", apiKey())
        .header("anthropic-version","2023-06-01")
        .header("Content-Type","application/json")
        .timeout(Duration.ofSeconds(30))
        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
        .build();

      HttpResponse<String> resp = client.send(httpReq, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() >= 400) throw new RuntimeException("Anthropic error: "+resp.statusCode()+" "+resp.body());
      JsonNode root = mapper.readTree(resp.body());
      String content = root.at("/content/0/text").asText("{}");
      JsonNode out = mapper.readTree(content);
      String text = out.path("content").asText("");
      double conf = out.path("confidence").asDouble(0.5);
      return new AiRouterInterceptor.AiResponse(text, conf);
    } catch (Exception e){
      return new AiRouterInterceptor.AiResponse("", 0.0);
    }
  }
}
