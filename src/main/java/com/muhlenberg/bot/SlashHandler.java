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

import java.io.IOException;

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

    if (commandParts.length == 2) {
      final String userEmail = context.getInitiator().getUser().getEmail();
      this.template = handlebars.compile("buy1");
      //this.template = messageService.templates().newTemplateFromClasspath("/templates/help.ftl");
      //this.messageService.send(context.getStreamId(),
          //Message.builder().template(this.template, singletonMap("name", userEmail)).build());
    } else {

    }

  }

  @Slash(value = "/portfolio", mentionBot = true)
  public void onSlashPortfolio(CommandContext context) {
    long userID = context.getInitiator().getUser().getUserId();
    String commandParts[] = context.getTextContent().trim().split(" ");

    if (commandParts.length == 2) {
      final String userEmail = context.getInitiator().getUser().getEmail();
      //this.template = messageService.templates().newTemplateFromClasspath("/templates/help.ftl");
      //this.messageService.send(context.getStreamId(),
          //Message.builder().template(this.template, singletonMap("name", userEmail)).build());
    } else {

    }

  }
}
