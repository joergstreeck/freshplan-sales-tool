package de.freshplan.ai.providers;

import de.freshplan.ai.AiRouterInterceptor;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

@ApplicationScoped
public class OpenAiClient implements AiRouterInterceptor.AiClient {

  @Inject ObjectMapper mapper;

  String apiKey() {
    String k = System.getenv("OPENAI_API_KEY");
    if (k == null || k.isBlank()) throw new IllegalStateException("OPENAI_API_KEY missing");
    return k;
  }

  String modelForUsecase(AiRouterInterceptor.AiUseCase useCase){
    return switch (useCase){
      case LEAD_CLASSIFY, EMAIL_CATEGORIZE, HELP_DRAFT, CONTACT_ROLE_DETECT -> System.getenv().getOrDefault("OPENAI_MODEL_SMALL","gpt-4o-mini");
      default -> System.getenv().getOrDefault("OPENAI_MODEL_LARGE","gpt-4o");
    };
  }

  @Override
  public AiRouterInterceptor.AiResponse call(AiRouterInterceptor.AiUseCase useCase, AiRouterInterceptor.AiRequest req) {
    try {
      String model = modelForUsecase(useCase);
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
      ObjectNode body = mapper.createObjectNode();
      body.put("model", model);
      var messages = mapper.createArrayNode();
      messages.add(mapper.createObjectNode().put("role","system").put("content","Return strict JSON with fields content (string) and confidence (0..1). No prose."));
      messages.add(mapper.createObjectNode().put("role","user").put("content", req.prompt()));
      body.set("messages", messages);
      body.put("temperature", 0.2);

      HttpRequest httpReq = HttpRequest.newBuilder()
        .uri(URI.create("https://api.openai.com/v1/chat/completions"))
        .header("Authorization","Bearer "+apiKey())
        .header("Content-Type","application/json")
        .timeout(Duration.ofSeconds(30))
        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
        .build();

      HttpResponse<String> resp = client.send(httpReq, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() >= 400) throw new RuntimeException("OpenAI error: "+resp.statusCode()+" "+resp.body());
      JsonNode root = mapper.readTree(resp.body());
      String content = root.at("/choices/0/message/content").asText("{}");
      JsonNode out = mapper.readTree(content);
      String text = out.path("content").asText("");
      double conf = out.path("confidence").asDouble(0.5);
      return new AiRouterInterceptor.AiResponse(text, conf);
    } catch (Exception e){
      // graceful degrade
      return new AiRouterInterceptor.AiResponse("", 0.0);
    }
  }
}
