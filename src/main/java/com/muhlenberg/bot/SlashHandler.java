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
import java.util.ArrayList;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

@Component
public class SlashHandler {

  private final MessageService messageService;
  private Template template;
  private final TemplateLoader loader;
  private final Handlebars handlebars;

  public SlashHandler(MessageService messageService) throws IOException {
    this.messageService = messageService;
    this.loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    this.handlebars = new Handlebars(loader);
    this.template = handlebars.compile("help");
  }

  @Slash(value = "/help", mentionBot = true)
  public void onSlashHelp(CommandContext context) throws IOException {
    final String userEmail = context.getInitiator().getUser().getEmail();
    this.messageService.send(context.getStreamId(), this.template.apply(userEmail));
  }

  @Slash(value = "/buy", mentionBot = true)
  public void onSlashBuy(CommandContext context) throws IOException {
    long userID = context.getInitiator().getUser().getUserId();
    String commandParts[] = context.getTextContent().trim().split(" ");

    if (commandParts.length == 5) {
      final String userEmail = context.getInitiator().getUser().getEmail();
      this.template = handlebars.compile("buy1");
      // this.template =
      // messageService.templates().newTemplateFromClasspath("/templates/help.ftl");
      // this.messageService.send(context.getStreamId(),
      // Message.builder().template(this.template, singletonMap("name",
      // userEmail)).build());
    } else {

    }

  }

  @Slash(value = "/create", mentionBot = true)
  public void onSlashCreate(CommandContext context) {
    V4User user = context.getInitiator().getUser();

    try {
      this.template = handlebars.compile("createPortfolio");
      System.out.println("tada");
      this.messageService.send(context.getStreamId(), template.apply(user));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  @Slash(value = "/portfolio", mentionBot = true)
  public void onSlashPortfolio(CommandContext context) {
    V4User user = context.getInitiator().getUser();
    String commandParts[] = context.getTextContent().trim().split(" ");
    if (commandParts.length == 5) {
      ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
      try {
        this.template = handlebars.compile("selectPortfolio");
        this.messageService.send(context.getStreamId(), template.apply(portfolioList));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else {

    }

  }

  public Context objectToContext(Object object) throws JsonProcessingException {
    ObjectMapper obj = new ObjectMapper();
    String jsonString = obj.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    JsonNode jsonNode = new ObjectMapper().readValue(jsonString, JsonNode.class);
    Context c = Context.newBuilder(jsonNode).resolver(JsonNodeValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE,
        FieldValueResolver.INSTANCE, MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE).build();

    return c;
  }
}
