package com.muhlenberg.bot;

import com.muhlenberg.models.*;

import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.gen.api.model.V4User;
import com.symphony.bdk.spring.annotation.Slash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.HashMap;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;




@Component
public class SummarySlashHandler {
      
  private final MessageService messageService;
  private final Template template;
  private final Handlebars handlebars;

  public SummarySlashHandler(MessageService messageService) throws IOException {
    this.messageService = messageService;
    //Initialize handlebars service
    TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".hbs");
    //Load correct template
    this.handlebars = new Handlebars(loader);
    //Register 
    this.handlebars.registerHelpers(new HelperSource());
    this.handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
    this.template = handlebars.compile("summary");
  }

  @Slash(value = "/summarize", mentionBot = false)
  public void onSlashSummarize(CommandContext context) {

    
    Stock s = new Stock("AAPL", "Apple", 125.02, 0.0, true);
    Stock s2 = new Stock("XOM", "Exxon Mobil", 15.21, -.15, true);
    Stock s3 = new Stock("TMUS", "T-Mobile", 156, 1.15, true);
    HashMap<V4User, Double> h = new HashMap<V4User, Double>();
    h.put(context.getInitiator().getUser(), .215);
    
    
    HashMap<Stock, Double> h2 = new HashMap<Stock, Double>();
    h2.put(s, 120.00);
    h2.put(s2, 127d);
    h2.put(s3, 17d);
    
    Portfolio p = new Portfolio("PortTester", 1000, 1.00, h, h2);
    
    try {

      Context c = objectToContext(new Summary(p));

      this.messageService.send(context.getStreamId(), template.apply(c));

    } catch (JsonProcessingException e1) {
      System.out.println("Json Issue");
      e1.printStackTrace();
    } catch (IOException e) {

      System.out.println("IOException");
      e.printStackTrace();
    }   
  }

  public Context objectToContext(Object object) throws JsonProcessingException {
    ObjectMapper obj = new ObjectMapper();
    String jsonString = obj.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    JsonNode jsonNode = new ObjectMapper().readValue(jsonString, JsonNode.class);
    Context c = Context
              .newBuilder(jsonNode)
              .resolver(JsonNodeValueResolver.INSTANCE,
                      JavaBeanValueResolver.INSTANCE,
                      FieldValueResolver.INSTANCE,
                      MapValueResolver.INSTANCE,
                      MethodValueResolver.INSTANCE
              )
              .build();

    return c;
  }
}

