package com.muhlenberg.bot;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;
//import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.spring.annotation.Slash;
//import com.symphony.bdk.template.api.Template;

import org.springframework.stereotype.Component;
//import static java.util.Collections.singletonMap;

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
    loader.setSuffix(".handlebars");
    this.handlebars = new Handlebars(loader);
    this.template = handlebars.compile("help");
    //this.template = messageService.templates().newTemplateFromClasspath("/templates/help.ftl");
  }

  @Slash(value = "/help", mentionBot = true)
  public void onSlashHelp(CommandContext context) throws IOException {
    final String userEmail = context.getInitiator().getUser().getEmail();
    this.template = handlebars.compile("help");
    this.messageService.send(context.getStreamId(), this.template.apply(userEmail));
    //this.messageService.send(context.getStreamId(),
    //    Message.builder().template(this.template, singletonMap("name", userEmail)).build());
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
