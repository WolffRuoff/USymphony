package com.muhlenberg.bot.listeners;

import com.muhlenberg.bot.HelperSource;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.*;

import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;
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
import com.github.jknack.handlebars.Context;
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

    
    Stock s = new Stock("AAPL", "Apple", 125.0, 125.02, 0.0, true);
    Stock s2 = new Stock("XOM", "Exxon Mobil", 125.0, 15.21, -.15, true);
    Stock s3 = new Stock("TMUS", "T-Mobile", 125.0, 156, 1.15, true);
    HashMap<Long, Double> h = new HashMap<Long, Double>();
    h.put(context.getInitiator().getUser().getUserId(), .215);
    
    
    HashMap<Stock, Double> h2 = new HashMap<Stock, Double>();
    h2.put(s, 120.00);
    h2.put(s2, 127d);
    h2.put(s3, 17d);
    
    Portfolio p = new Portfolio("PortTester", 1000, .62, h, h2,"^GSPC");
    
    try {

      Context c = ObjectToContext.Convert(new Summary(p));
      //System.out.println(template.apply(c));
      this.messageService.send(context.getStreamId(), template.apply(c));

    } catch (JsonProcessingException e1) {
      System.out.println("Json Issue");
      e1.printStackTrace();
    } catch (IOException e) {

      System.out.println("IOException");
      e.printStackTrace();
    }   
  }

}

